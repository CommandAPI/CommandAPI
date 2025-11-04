package dev.jorel.commandapi.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultVelocityConfig extends DefaultConfig {

	private DefaultVelocityConfig() {}

	public static DefaultVelocityConfig createDefault() {
		Map<String, CommentedConfigOption<?>> options = new LinkedHashMap<>();
		options.put("verbose-outputs", VERBOSE_OUTPUTS);
		options.put("silent-logs", SILENT_LOGS);
		options.put("messages.missing-executor-implementation", MISSING_EXECUTOR_IMPLEMENTATION);
		options.put("create-dispatcher-json", CREATE_DISPATCHER_JSON);
		options.put("make-networking-exceptions-warnings", MAKE_NETWORKING_EXCEPTIONS_WARNINGS);

		Map<String, CommentedSection> sections = new LinkedHashMap<>();
		sections.put("messages", SECTION_MESSAGE);

		return DefaultVelocityConfig.create(options, sections);
	}

	public static DefaultVelocityConfig create(Map<String, CommentedConfigOption<?>> options, Map<String, CommentedSection> sections) {
		DefaultVelocityConfig config = new DefaultVelocityConfig();

		config.allOptions.putAll(options);
		config.allSections.putAll(sections);

		return config;
	}

}
