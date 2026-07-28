package dev.jorel.commandapi.config;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;

public interface ConfigurationAdapter<Configuration> {

	void setValue(String key, Object value);

	void setComment(String key, String[] comment);

	Object getValue(String key);

	String[] getComment(String key);

	Set<String> getKeys();

	boolean contains(String key);

	void tryCreateSection(String key);

	ConfigurationAdapter<Configuration> complete();

	Configuration config();

	ConfigurationAdapter<Configuration> createNew();

	ConfigurationAdapter<Configuration> loadFromFile() throws IOException;

	void saveToFile() throws IOException;

	// TODO: It might make sense to use CommandAPILogger as an abstraction above different Logging
	//  classes, but it was easier to just do a Consumer since we currently only log severe messages
	default void saveDefaultConfig(DefaultConfig defaultConfig, File directory, Consumer<String> severeLog) {
		ConfigGenerator generator = ConfigGenerator.createNew(defaultConfig);
		ConfigurationAdapter<Configuration> existingConfig;
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				severeLog.accept("Failed to create directory for the CommandAPI's config.yml file!");
			}
			existingConfig = createNew();
		} else {
			try {
				existingConfig = loadFromFile();
			} catch (IOException e) {
				severeLog.accept("Failed to load the config file!");
				severeLog.accept("Error message: " + e.getMessage());
				severeLog.accept("Stacktrace:");
				for (StackTraceElement element : e.getStackTrace()) {
					severeLog.accept(element.toString());
				}
				return;
			}
		}
		ConfigurationAdapter<Configuration> updatedConfig = generator.generate(existingConfig);
		if (updatedConfig == null) {
			return;
		}
		try {
			updatedConfig.saveToFile();
		} catch (IOException e) {
			severeLog.accept("Failed to save the config file!");
			severeLog.accept("Error message: " + e.getMessage());
			severeLog.accept("Stacktrace:");
			for (StackTraceElement element : e.getStackTrace()) {
				severeLog.accept(element.toString());
			}
		}
	}

}
