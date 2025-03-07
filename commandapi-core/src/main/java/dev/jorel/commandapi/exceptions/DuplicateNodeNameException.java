package dev.jorel.commandapi.exceptions;

import dev.jorel.commandapi.arguments.AbstractArgument;

import java.util.List;

/**
 * An exception caused when two arguments conflict due to sharing the same node name. Note that unlisted arguments are
 * allowed to share node names with other arguments.
 */
public class DuplicateNodeNameException extends CommandRegistrationException {
	/**
	 * Creates a new DuplicateNodeNameException.
	 *
	 * @param previousArguments The arguments that led up to the invalid argument.
	 * @param argument          The argument that was incorrectly given the same name as a previous argument.
	 */
	public <Argument extends AbstractArgument<?, ?, ?, ?>> DuplicateNodeNameException(List<Argument> previousArguments, Argument argument) {
		super(buildMessage(previousArguments, argument));
	}

	private static <Argument extends AbstractArgument<?, ?, ?, ?>> String buildMessage(List<Argument> previousArguments, Argument argument) {
		StringBuilder builder = new StringBuilder();

		builder.append("Duplicate node names for listed arguments are not allowed! Going down the ");
		addArgumentList(builder, previousArguments);
		builder.append(" branch, found ");
		addArgument(builder, argument);
		builder.append(", which had a duplicated node name");

		return builder.toString();
	}
}
