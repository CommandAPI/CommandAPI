package dev.jorel.commandapi.test;

import dev.jorel.commandapi.config.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigGenerationTests {

	private CommentedConfigOption<Boolean> silentLogs;
	private CommentedConfigOption<Boolean> verboseOutputs;
	private CommentedConfigOption<String> missingExecutorImplementation;

	private CommentedSection messages;

	private ConfigGenerator generator;
	private DefaultBukkitConfig bukkitConfig;
	private BukkitConfigurationAdapter adapter;

	@BeforeEach
	public void setup() {
		silentLogs = new CommentedConfigOption<>(new String[]{
			"Silent logs (default: false)",
			"If \"true\", turns off all logging from the CommandAPI, except for errors."
		}, false);
		verboseOutputs = new CommentedConfigOption<>(new String[]{
			"Verbose outputs (default: false)",
			"If \"true\", outputs command registration and unregistration logs in the console"
		}, false);
		missingExecutorImplementation = new CommentedConfigOption<>(new String[]{
			"Missing executor implementation (default: \"This command has no implementations for %s\")",
			"The message to display to senders when a command has no executor. Available",
			"parameters are:",
			"  %s - the executor class (lowercase)",
			"  %S - the executor class (normal case)"
		}, "This command has no implementations for %s");

		messages = new CommentedSection(new String[]{
			"Messages",
			"Controls messages that the CommandAPI displays to players"
		});

		Map<String, CommentedConfigOption<?>> options = new LinkedHashMap<>();
		options.put("silent-logs", silentLogs);
		options.put("verbose-outputs", verboseOutputs);
		options.put("messages.missing-executor-implementation", missingExecutorImplementation);

		Map<String, CommentedSection> sections = new LinkedHashMap<>();
		sections.put("messages", messages);

		ConfigurationAdapter<YamlConfiguration> adapter = new BukkitConfigurationAdapter(new YamlConfiguration(), null);
		bukkitConfig = DefaultBukkitConfig.create(options, sections);
		generator = ConfigGenerator.createNew(bukkitConfig);
		this.adapter = (BukkitConfigurationAdapter) generator.generate(adapter);
	}

	@AfterEach
	public void reset() {
		this.silentLogs = null;
		this.verboseOutputs = null;
		this.missingExecutorImplementation = null;
		this.messages = null;
		this.generator = null;
		this.bukkitConfig = null;
		this.adapter = null;
	}

	// Test methods
	private void validateConfigOptions(Set<String> options, BukkitConfigurationAdapter adapter) {
		for (String option : options) {
			assertTrue(adapter.contains(option), "Config option '" + option + "' does not exist!");
		}
	}

	private void validateConfigOptionComments(Map<String, String[]> comments, BukkitConfigurationAdapter adapter) {
		for (String option : comments.keySet()) {
			String[] expectedComment = comments.get(option);
			String[] generatedComment = adapter.getComment(option);
			assertArrayEquals(expectedComment, generatedComment, "Config option comment for option '" + option + "' does not exist or was incorrect!");
		}
	}

	private void validateConfigOptionsAbsent(Set<String> options, BukkitConfigurationAdapter adapter) {
		for (String option : options) {
			assertFalse(adapter.contains(option), "Config option '" + option + "' does still exist!");
		}
	}

	private void validateSections(List<String> sections, String expectedOption, YamlConfiguration config) {
		ConfigurationSection root = config;

		for (String section : sections) {
			root = root.getConfigurationSection(section);
			assertNotNull(root, "Section '" + section + "' does not exist!");
		}
		Object expectedValue = root.get(expectedOption);
		assertNotNull(expectedValue, "Expected option '" + expectedOption + "' was not found in section '" + root.getName() + "'!");
	}

	@Test
	void testDefaultConfigOptionGeneration() {
		validateConfigOptions(Set.of(
			"silent-logs", "verbose-outputs", "messages.missing-executor-implementation"
		), adapter);
	}

	@Test
	void testDefaultConfigOptionCommentGeneration() {
		validateConfigOptionComments(Map.ofEntries(
			Map.entry("silent-logs", silentLogs.comment()),
			Map.entry("verbose-outputs", verboseOutputs.comment()),
			Map.entry("messages.missing-executor-implementation", missingExecutorImplementation.comment()),
			Map.entry("messages", messages.comment())
		), adapter);
	}

	@Test
	void testConfigOptionAddition() {
		CommentedConfigOption<Boolean> createDispatcherJson = new CommentedConfigOption<>(new String[]{
			"Create dispatcher JSON (default: false)",
			"If \"true\", the CommandAPI creates a command_registration.json file showing the",
			"mapping of registered commands. This is designed to be used by developers -",
			"setting this to \"false\" will improve command registration performance."
		}, false);

		bukkitConfig.getAllOptions().put("create-dispatcher-json", createDispatcherJson);
		generator = ConfigGenerator.createNew(bukkitConfig);
		BukkitConfigurationAdapter updatedAdapter = (BukkitConfigurationAdapter) generator.generate(adapter);

		validateConfigOptions(Set.of(
			"silent-logs", "verbose-outputs", "messages.missing-executor-implementation", "create-dispatcher-json"
		), updatedAdapter);

		validateConfigOptionComments(Map.ofEntries(
			Map.entry("silent-logs", silentLogs.comment()),
			Map.entry("verbose-outputs", verboseOutputs.comment()),
			Map.entry("messages.missing-executor-implementation", missingExecutorImplementation.comment()),
			Map.entry("create-dispatcher-json", createDispatcherJson.comment()),
			Map.entry("messages", messages.comment())
		), updatedAdapter);
	}

	@Test
	void testConfigOptionDeletion() {
		bukkitConfig.getAllOptions().remove("silent-logs");
		generator = ConfigGenerator.createNew(bukkitConfig);
		BukkitConfigurationAdapter updatedAdapter = (BukkitConfigurationAdapter) generator.generate(adapter);

		validateConfigOptionsAbsent(Set.of("silent-logs"), updatedAdapter);
	}

	@Test
	void testConfigOptionCommentUpdate() {
		silentLogs = new CommentedConfigOption<>(new String[]{
			"Defines if silent logs should happen"
		}, false);

		bukkitConfig.getAllOptions().put("silent-logs", silentLogs);
		generator = ConfigGenerator.createNew(bukkitConfig);
		BukkitConfigurationAdapter updatedAdapter = (BukkitConfigurationAdapter) generator.generate(adapter);

		validateConfigOptionComments(Map.ofEntries(
			Map.entry("silent-logs", silentLogs.comment())
		), updatedAdapter);
	}

	@Test
	void testNestedSections() {
		CommentedConfigOption<Boolean> subSubOption = new CommentedConfigOption<>(new String[0], false);

		bukkitConfig.getAllOptions().put("root.nested.option", subSubOption);
		generator = ConfigGenerator.createNew(bukkitConfig);
		BukkitConfigurationAdapter updatedAdapter = (BukkitConfigurationAdapter) generator.generate(adapter);

		validateSections(List.of("root", "nested"), "option", updatedAdapter.config());
	}

	@Test
	void testConfigUpdateNotNeeded() {
		assertNull(generator.generate(adapter));
	}
}
