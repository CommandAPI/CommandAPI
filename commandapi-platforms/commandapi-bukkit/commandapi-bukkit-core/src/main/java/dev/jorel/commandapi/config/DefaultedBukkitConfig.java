package dev.jorel.commandapi.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Default config values for the plugin's config.yml file
 */
class DefaultedBukkitConfig {

	public static final CommentedConfigOption<Boolean> VERBOSE_OUTPUTS = new CommentedConfigOption<>(
		List.of(
			"Verbose outputs (default: false)",
			"If \"true\", outputs command registration and unregistration logs in the console"
		),
		"verbose-outputs", false
	);

	public static final CommentedConfigOption<Boolean> SILENT_LOGS = new CommentedConfigOption<>(
		List.of(
			"Silent logs (default: false)",
			"If \"true\", turns off all logging from the CommandAPI, except for errors."
		),
		"silent-logs", false
	);

	public static final CommentedConfigOption<String> MISSING_EXECUTOR_IMPLEMENTATION = new CommentedConfigOption<>(
		List.of(
			"Missing executor implementation (default: \"This command has no implementations for %s\")",
			"The message to display to senders when a command has no executor. Available",
			"parameters are:",
			"  %s - the executor class (lowercase)",
			"  %S - the executor class (normal case)"
		),
		"messages.missing-executor-implementation", "This command has no implementations for %s"
	);

	public static final CommentedConfigOption<Boolean> CREATE_DISPATCHER_JSON = new CommentedConfigOption<>(
		List.of(
			"Create dispatcher JSON (default: false)",
			"If \"true\", the CommandAPI creates a command_registration.json file showing the",
			"mapping of registered commands. This is designed to be used by developers -",
			"setting this to \"false\" will improve command registration performance."
		),
		"create-dispatcher-json", false
	);

	public static final CommentedConfigOption<Boolean> USE_LATEST_NMS_VERSION = new CommentedConfigOption<>(
		List.of(
			"Use latest version (default: false)",
			"If \"true\", the CommandAPI will use the latest available NMS implementation",
			"when the CommandAPI is used. This avoids all checks to see if the latest NMS",
			"implementation is actually compatible with the current Minecraft version."
		),
		"use-latest-nms-version", false
	);

	public static final CommentedConfigOption<Boolean> BE_LENIENT_FOR_MINOR_VERSIONS = new CommentedConfigOption<>(
		List.of(
			"Be lenient with version checks when loading for new minor Minecraft versions (default: false)",
			"If \"true\", the CommandAPI loads NMS implementations for potentially unsupported Minecraft versions.",
			"For example, this setting may allow updating from 1.21.1 to 1.21.2 as only the minor version is changing",
			"but will not allow an update from 1.21.2 to 1.22.",
			"Keep in mind that implementations may vary and actually updating the CommandAPI might be necessary."
		),
		"be-lenient-for-minor-versions", false
	);

	public static final CommentedConfigOption<Boolean> SHOULD_HOOK_PAPER_RELOAD = new CommentedConfigOption<>(
		List.of(
			"Hook into Paper's ServerResourcesReloadedEvent (default: true)",
			"If \"true\", and the CommandAPI detects it is running on a Paper server, it will",
			"hook into Paper's ServerResourcesReloadedEvent to detect when /minecraft:reload is run.",
			"This allows the CommandAPI to automatically call its custom datapack-reloading",
			"function which allows CommandAPI commands to be used in datapacks.",
			"If you set this to false, CommandAPI commands may not work inside datapacks after",
			"reloading datapacks."
		),
		"hook-paper-reload", false
	);

	public static final CommentedConfigOption<Boolean> SKIP_RELOAD_DATAPACKS = new CommentedConfigOption<>(
		List.of(
			"Skips the initial datapack reload when the server loads (default: false)",
			"If \"true\", the CommandAPI will not reload datapacks when the server has finished",
			"loading. Datapacks will still be reloaded if performed manually when \"hook-paper-reload\"",
			"is set to \"true\" and /minecraft:reload is run."
		),
		"skip-initial-datapack-reload", false
	);

	public static final CommentedConfigOption<List<?>> PLUGINS_TO_CONVERT = new CommentedConfigOption<>(
		List.of(
			"Plugins to convert (default: [])",
			"Controls the list of plugins to process for command conversion."
		),
		"plugins-to-convert", new ArrayList<>()
	);

	public static final CommentedConfigOption<List<String>> OTHER_COMMANDS_TO_CONVERT = new CommentedConfigOption<>(
		List.of(
			"Other commands to convert (default: [])",
			"A list of other commands to convert. This should be used for commands which",
			"are not declared in a plugin.yml file."
		),
		"other-commands-to-convert", new ArrayList<>()
	);

	public static final CommentedConfigOption<List<String>> SKIP_SENDER_PROXY = new CommentedConfigOption<>(
		List.of(
			"Skip sender proxy (default: [])",
			"Determines whether the proxy sender should be skipped when converting a",
			"command. If you are having issues with plugin command conversion, add the",
			"plugin to this list."
		),
		"skip-sender-proxy", new ArrayList<>()
	);

	public static final List<CommentedConfigOption<?>> ALL_OPTIONS = List.of(
		VERBOSE_OUTPUTS,
		SILENT_LOGS,
		MISSING_EXECUTOR_IMPLEMENTATION,
		CREATE_DISPATCHER_JSON,
		USE_LATEST_NMS_VERSION,
		BE_LENIENT_FOR_MINOR_VERSIONS,
		SHOULD_HOOK_PAPER_RELOAD,
		SKIP_RELOAD_DATAPACKS,
		PLUGINS_TO_CONVERT,
		OTHER_COMMANDS_TO_CONVERT,
		SKIP_SENDER_PROXY
	);

	public static final CommentedConfigOption<?> SECTION_MESSAGE = new CommentedConfigOption<>(
		List.of(
			"Messages",
			"Controls messages that the CommandAPI displays to players"
		),
		"messages", null
	);

	public static final List<CommentedConfigOption<?>> ALL_SECTIONS = List.of(
		SECTION_MESSAGE
	);

}
