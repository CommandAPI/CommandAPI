package dev.jorel.commandapi.exceptions;

import java.util.List;

/**
 * An exception caused when a command path that is already executable is registered again.
 */
public class CommandConflictException extends CommandRegistrationException {
	/**
	 * Creates a new CommandConflictException.
	 *
	 * @param path The path of Brigadier node names that was re-registered.
	 */
	public CommandConflictException(List<String> path) {
		super(buildMessage(path));
	}

	private static String buildMessage(List<String> path) {
		StringBuilder builder = new StringBuilder();

		builder.append("The command path \"/");

		for (String node : path) {
			builder.append(node).append(" ");
		}
		builder.setCharAt(builder.length() - 1, '\"');

		builder.append(" could not be registered because it conflicts with a previously registered command.");

		return builder.toString();
	}
}
