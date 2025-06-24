package dev.jorel.commandapi.config;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

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

	default void saveDefaultConfig(DefaultConfig defaultConfig, File directory, Logger logger) {
		ConfigGenerator generator = ConfigGenerator.createNew(defaultConfig);
		ConfigurationAdapter<Configuration> existingConfig;
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				logger.severe("Failed to create directory for the CommandAPI's config.yml file!");
			}
			existingConfig = createNew();
		} else {
			try {
				existingConfig = loadFromFile();
			} catch (IOException e) {
				logger.severe("Failed to load the config file!");
				logger.severe("Error message: " + e.getMessage());
				logger.severe("Stacktrace:");
				for (StackTraceElement element : e.getStackTrace()) {
					logger.severe(element.toString());
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
			logger.severe("Failed to save the config file!");
			logger.severe("Error message: " + e.getMessage());
			logger.severe("Stacktrace:");
			for (StackTraceElement element : e.getStackTrace()) {
				logger.severe(element.toString());
			}
		}
	}

}
