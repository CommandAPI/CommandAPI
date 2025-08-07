package dev.jorel.commandapi.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("DuplicatedCode")
public class DefaultBukkitConfig extends DefaultConfig {

	public static CommentedConfigOption<Boolean> FALLBACK_TO_LATEST_NMS(boolean paper) {
		return new CommentedConfigOption<>(new String[]{
				"Fallback to latest version (default: " + (paper ? "true" : "false") + ")",
				"If \"true\", the CommandAPI will fall back to the latest available NMS",
				"implementation when the CommandAPI is used and no implementation for the",
				"current Minecraft version was found."
			}, paper
		);
	}

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
		options.put("fallback-to-latest-nms", FALLBACK_TO_LATEST_NMS(true));
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
		options.put("fallback-to-latest-nms", FALLBACK_TO_LATEST_NMS(false));
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
