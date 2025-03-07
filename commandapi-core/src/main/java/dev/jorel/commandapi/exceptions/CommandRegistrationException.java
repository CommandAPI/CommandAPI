package dev.jorel.commandapi.exceptions;

import dev.jorel.commandapi.arguments.AbstractArgument;

import java.util.List;

/**
 * An exception that happens while registering a command
 */
public abstract class CommandRegistrationException extends RuntimeException {
	protected CommandRegistrationException(String message) {
		super(message);
	}

	protected static <Argument extends AbstractArgument<?, ?, ?, ?>> void addArgumentList(StringBuilder builder, List<Argument> arguments) {
		builder.append("[");
		for (Argument argument : arguments) {
			addArgument(builder, argument);
			builder.append(" ");
		}
		builder.setCharAt(builder.length() - 1, ']');
	}

	protected static <Argument extends AbstractArgument<?, ?, ?, ?>> void addArgument(StringBuilder builder, Argument argument) {
		builder.append(argument.getNodeName()).append("<").append(argument.getClass().getSimpleName()).append(">");
	}
}
