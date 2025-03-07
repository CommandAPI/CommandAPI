package dev.jorel.commandapi.exceptions;

import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.arguments.AbstractArgument;

import java.util.ArrayList;
import java.util.List;

/**
 * An exception caused when a required (non-optional) argument follows an optional argument.
 */
public class OptionalArgumentException extends CommandRegistrationException {
	/**
	 * Creates an OptionalArgumentException
	 *
	 * @param commandName       The name of the command that had a required argument after an optional argument.
	 * @param previousArguments The arguments that led up to the invalid required argument.
	 * @param argument          The required argument that incorrectly came after an optional argument.
	 */
	public <C, Argument extends AbstractArgument<?, ?, Argument, C>> OptionalArgumentException(String commandName, List<Argument> previousArguments, Argument argument) {
		this(insertCommandName(commandName, previousArguments), argument);
	}

	private static <C, Argument extends AbstractArgument<?, ?, Argument, C>> List<Argument> insertCommandName(String commandName, List<Argument> previousArguments) {
		CommandAPIHandler<Argument, C, ?> handler = CommandAPIHandler.getInstance();
		Argument commandNameArgument = handler.getPlatform().newConcreteLiteralArgument(commandName, commandName);
		List<Argument> newArguments = new ArrayList<>(List.of(commandNameArgument));
		newArguments.addAll(previousArguments);
		return newArguments;
	}

	/**
	 * Creates an OptionalArgumentException
	 *
	 * @param previousArguments The arguments that led up to the invalid required argument, starting with an Argument representing
	 *                          the initial command node.
	 * @param argument          The required argument that incorrectly came after an optional argument.
	 */
	public <Argument extends AbstractArgument<?, ?, ?, ?>> OptionalArgumentException(List<Argument> previousArguments, Argument argument) {
		super(buildMessage(previousArguments, argument));
	}

	private static <Argument extends AbstractArgument<?, ?, ?, ?>> String buildMessage(List<Argument> previousArguments, Argument argument) {
		StringBuilder builder = new StringBuilder();

		builder.append("Uncombined required arguments following optional arguments are not allowed! Going down the ");
		addArgumentList(builder, previousArguments);
		builder.append(" branch, found a required argument ");
		addArgument(builder, argument);
		builder.append(" after an optional argument");

		return builder.toString();
	}
}
