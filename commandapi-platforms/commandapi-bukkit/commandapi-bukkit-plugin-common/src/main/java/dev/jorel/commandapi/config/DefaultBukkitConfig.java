package dev.jorel.commandapi.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("DuplicatedCode")
public class DefaultBukkitConfig extends DefaultConfig {

	public static final CommentedConfigOption<Boolean> USE_LATEST_NMS_VERSION = new CommentedConfigOption<>(
		new String[] {
			"Use latest version (default: false)",
			"If \"true\", the CommandAPI will use the latest available NMS implementation",
			"when the CommandAPI is used. This avoids all checks to see if the latest NMS",
			"implementation is actually compatible with the current Minecraft version."
		}, false
	);

	public static final CommentedConfigOption<Boolean> BE_LENIENT_FOR_MINOR_VERSIONS = new CommentedConfigOption<>(
		new String[] {
			"Be lenient with version checks when loading for new minor Minecraft versions (default: false)",
			"If \"true\", the CommandAPI loads NMS implementations for potentially unsupported Minecraft versions.",
			"For example, this setting may allow updating from 1.21.1 to 1.21.2 as only the minor version is changing",
			"but will not allow an update from 1.21.2 to 1.22.",
			"Keep in mind that implementations may vary and actually updating the CommandAPI might be necessary."
		}, false
	);

	public static final CommentedConfigOption<Boolean> SHOULD_HOOK_PAPER_RELOAD = new CommentedConfigOption<>(
		new String[] {
			"Hook into Paper's ServerResourcesReloadedEvent (default: false)",
			"If \"true\", and the CommandAPI detects it is running on a Paper server, it will",
			"hook into Paper's ServerResourcesReloadedEvent to detect when /minecraft:reload is run.",
			"This allows the CommandAPI to automatically call its custom datapack-reloading",
			"function which allows CommandAPI commands to be used in datapacks.",
			"If you set this to false, CommandAPI commands may not work inside datapacks after",
			"reloading datapacks."
		}, false
	);

	public static final CommentedConfigOption<Boolean> SKIP_RELOAD_DATAPACKS = new CommentedConfigOption<>(
		new String[] {
			"Skips the initial datapack reload when the server loads (default: true)",
			"If \"true\", the CommandAPI will not reload datapacks when the server has finished",
			"loading. Datapacks will still be reloaded if performed manually when \"hook-paper-reload\"",
			"is set to \"true\" and /minecraft:reload is run."
		}, true
	);

	public static final CommentedConfigOption<List<?>> PLUGINS_TO_CONVERT = new CommentedConfigOption<>(
		new String[] {
			"Plugins to convert (default: [])",
			"Controls the list of plugins to process for command conversion."
		}, new ArrayList<>()
	);

	public static final CommentedConfigOption<List<String>> OTHER_COMMANDS_TO_CONVERT = new CommentedConfigOption<>(
		new String[] {
			"Other commands to convert (default: [])",
			"A list of other commands to convert. This should be used for commands which",
			"are not declared in a plugin.yml file."
		}, new ArrayList<>()
	);

	public static final CommentedConfigOption<List<String>> SKIP_SENDER_PROXY = new CommentedConfigOption<>(
		new String[] {
			"Skip sender proxy (default: [])",
			"Determines whether the proxy sender should be skipped when converting a",
			"command. If you are having issues with plugin command conversion, add the",
			"plugin to this list."
		}, new ArrayList<>()
	);

	private DefaultBukkitConfig() {
	}

	public static DefaultBukkitConfig createDefaultPaperConfig() {
		Map<String, CommentedConfigOption<?>> options = new LinkedHashMap<>();
		options.put("verbose-outputs", VERBOSE_OUTPUTS);
		options.put("silent-logs", SILENT_LOGS);
		options.put("messages.missing-executor-implementation", MISSING_EXECUTOR_IMPLEMENTATION);
		options.put("create-dispatcher-json", CREATE_DISPATCHER_JSON);
		options.put("use-latest-nms-version", USE_LATEST_NMS_VERSION);
		options.put("be-lenient-for-minor-versions", BE_LENIENT_FOR_MINOR_VERSIONS);
		options.put("hook-paper-reload", SHOULD_HOOK_PAPER_RELOAD);
		options.put("skip-initial-datapack-reload", SKIP_RELOAD_DATAPACKS);
		options.put("plugins-to-convert", PLUGINS_TO_CONVERT);
		options.put("other-commands-to-convert", OTHER_COMMANDS_TO_CONVERT);
		options.put("skip-sender-proxy", SKIP_SENDER_PROXY);

		Map<String, CommentedSection> sections = new LinkedHashMap<>();
		sections.put("messages", SECTION_MESSAGE);

		return DefaultBukkitConfig.create(options, sections);
	}

	public static DefaultBukkitConfig createDefaultSpigotConfig() {
		Map<String, CommentedConfigOption<?>> options = new LinkedHashMap<>();
		options.put("verbose-outputs", VERBOSE_OUTPUTS);
		options.put("silent-logs", SILENT_LOGS);
		options.put("messages.missing-executor-implementation", MISSING_EXECUTOR_IMPLEMENTATION);
		options.put("create-dispatcher-json", CREATE_DISPATCHER_JSON);
		options.put("use-latest-nms-version", USE_LATEST_NMS_VERSION);
		options.put("be-lenient-for-minor-versions", BE_LENIENT_FOR_MINOR_VERSIONS);
		options.put("skip-initial-datapack-reload", SKIP_RELOAD_DATAPACKS);
		options.put("plugins-to-convert", PLUGINS_TO_CONVERT);
		options.put("other-commands-to-convert", OTHER_COMMANDS_TO_CONVERT);
		options.put("skip-sender-proxy", SKIP_SENDER_PROXY);

		Map<String, CommentedSection> sections = new LinkedHashMap<>();
		sections.put("messages", SECTION_MESSAGE);

		return DefaultBukkitConfig.create(options, sections);
	}

	public static DefaultBukkitConfig create(Map<String, CommentedConfigOption<?>> options, Map<String, CommentedSection> sections) {
		DefaultBukkitConfig config = new DefaultBukkitConfig();

		config.allOptions.putAll(options);
		config.allSections.putAll(sections);

		return config;
	}
}
