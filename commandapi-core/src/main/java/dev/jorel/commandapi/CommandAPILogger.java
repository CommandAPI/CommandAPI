package dev.jorel.commandapi;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

public interface CommandAPILogger {

	static CommandAPILogger fromJavaLogger(java.util.logging.Logger logger) {
		return bindToMethods(
			logger::info,
			logger::warning,
			logger::severe,
			(message, ex) -> logger.log(Level.SEVERE, message, ex)
		);
	}

	static CommandAPILogger fromApacheLog4jLogger(org.apache.logging.log4j.Logger logger) {
		return bindToMethods(logger::info, logger::warn, logger::error, logger::error);
	}

	static CommandAPILogger fromSlf4jLogger(org.slf4j.Logger logger) {
		return bindToMethods(logger::info, logger::warn, logger::error, logger::error);
	}

	static CommandAPILogger silent() {
		return bindToMethods(msg -> {}, msg -> {}, msg -> {}, (msg, ex) -> {});
	}

	static CommandAPILogger bindToMethods(Consumer<String> info, Consumer<String> warning, Consumer<String> severe, BiConsumer<String, Throwable> severeException) {
		return new CommandAPILogger() {
			@Override
			public void info(String message) {
				info.accept(message);
			}

			@Override
			public void warning(String message) {
				warning.accept(message);
			}

			@Override
			public void severe(String message) {
				severe.accept(message);
			}
			
			@Override
			public void severe(String message, Throwable exception) {
				severeException.accept(message, exception);
			}
		};
	}

	void info(String message);

	void warning(String message);

	void severe(String message);
	
	void severe(String message, Throwable exception);
}
