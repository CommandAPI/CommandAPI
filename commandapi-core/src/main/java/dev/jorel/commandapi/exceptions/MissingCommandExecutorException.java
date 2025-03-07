/*******************************************************************************
 * Copyright 2023 Jorel Ali (Skepter) - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package dev.jorel.commandapi.exceptions;

import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.arguments.AbstractArgument;

import java.util.List;

/**
 * An exception caused when a command does not declare any executors.
 */
public class MissingCommandExecutorException extends CommandRegistrationException {
	/**
	 * Creates a MissingCommandExecutorException.
	 *
	 * @param commandName The name of the command that could not be executed.
	 */
	public MissingCommandExecutorException(String commandName) {
		this(List.of(), CommandAPIHandler.getInstance().getPlatform().newConcreteLiteralArgument(commandName, commandName));
	}

	/**
	 * Creates a MissingCommandExecutorException.
	 *
	 * @param previousArguments The arguments that led up to the un-executable argument.
	 * @param argument          The argument that is missing an executor.
	 */
	public <Argument extends AbstractArgument<?, ?, ?, ?>> MissingCommandExecutorException(
		List<Argument> previousArguments, Argument argument) {
		super(buildMessage(previousArguments, argument));
	}

	private static <Argument extends AbstractArgument<?, ?, ?, ?>> String buildMessage(List<Argument> previousArguments, Argument argument) {
		StringBuilder builder = new StringBuilder();

		builder.append("The command path ");
		if (!previousArguments.isEmpty()) {
			addArgumentList(builder, previousArguments);
			builder.append(" ending with ");
		}
		addArgument(builder, argument);
		builder.append(" is not executable!");

		return builder.toString();
	}
}
