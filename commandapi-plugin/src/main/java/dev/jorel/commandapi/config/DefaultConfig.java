package dev.jorel.commandapi.config;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class DefaultConfig {

	protected final Map<String, CommentedConfigOption<?>> allOptions = new LinkedHashMap<>();
	protected final Map<String, CommentedSection> allSections = new LinkedHashMap<>();

	public static final CommentedConfigOption<Boolean> VERBOSE_OUTPUTS = new CommentedConfigOption<>(
		new String[]{
			"Verbose outputs (default: false)",
			"If \"true\", outputs command registration and unregistration logs in the console"
		}, false
	);

	public static final CommentedConfigOption<Boolean> SILENT_LOGS = new CommentedConfigOption<>(
		new String[] {
			"Silent logs (default: false)",
			"If \"true\", turns off all logging from the CommandAPI, except for errors."
		}, false
	);

	public static final CommentedConfigOption<String> MISSING_EXECUTOR_IMPLEMENTATION = new CommentedConfigOption<>(
		new String[]{
			"Missing executor implementation (default: \"This command has no implementations for %s\")",
			"The message to display to senders when a command has no executor. Available",
			"parameters are:",
			"  %s - the executor class (lowercase)",
			"  %S - the executor class (normal case)"
		}, "This command has no implementations for %s"
	);

	public static final CommentedConfigOption<Boolean> CREATE_DISPATCHER_JSON = new CommentedConfigOption<>(
		new String[]{
			"Create dispatcher JSON (default: false)",
			"If \"true\", the CommandAPI creates a command_registration.json file showing the",
			"mapping of registered commands. This is designed to be used by developers -",
			"setting this to \"false\" will improve command registration performance."
		}, false
	);

	public static final CommentedConfigOption<Boolean> ERROR_ON_FAILED_PACKET_SENDS = new CommentedConfigOption<>(
		new String[]{
			"Throw an error when a packet fails to send (default: true)",
			"If \"true\", the CommandAPI will throw an exception if it tries to send a packet but cannot",
			"(likely due to the receiver not having a new enough CommandAPI version to receive it). If",
			"\"false\", failed attempts to send a packet will be logged as a warning."
		}, true
	);

	public static final CommentedSection SECTION_MESSAGE = new CommentedSection(
		new String[]{
			"Messages",
			"Controls messages that the CommandAPI displays to players"
		}
	);

	public final Map<String, CommentedConfigOption<?>> getAllOptions() {
		return allOptions;
	}

	public final Map<String, CommentedSection> getAllSections() {
		return allSections;
	}

}
