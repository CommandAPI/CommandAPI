/*******************************************************************************
 * Copyright 2018, 2020 Jorel Ali (Skepter) - MIT License
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
package dev.jorel.commandapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.ExecutionInfo;
import dev.jorel.commandapi.executors.TypedExecutor;

/**
 * CommandAPIExecutor is the main executor implementation for command
 * executors. It contains a list of all executors (normal and resulting
 * executors) and switches its execution implementation based on the provided
 * command executor types.
 *
 * @param <CommandSender> The class for running platform commands
 */
public class CommandAPIExecutor<CommandSender> {

	// Setup executors
	private List<TypedExecutor<CommandSender, ? extends CommandSender, ?>> executors;

	public CommandAPIExecutor() {
		this.executors = new ArrayList<>();
	}

	public void addExecutor(TypedExecutor<CommandSender, ? extends CommandSender, ?> executor) {
		this.executors.add(executor);
	}

	public void setExecutors(List<TypedExecutor<CommandSender, ? extends CommandSender, ?>> executors) {
		this.executors = executors;
	}

	public List<TypedExecutor<CommandSender, ? extends CommandSender, ?>> getExecutors() {
		return this.executors;
	}

	public boolean hasAnyExecutors() {
		return !executors.isEmpty();
	}

	// Use executors
	public int execute(ExecutionInfo<CommandSender, ?> info) throws CommandSyntaxException {
		try {
			for (TypedExecutor<CommandSender, ? extends CommandSender, ?> executor : executors) {
				Optional<Integer> result = tryExecutor(executor, info);
				
				if (result.isPresent()) return result.get();
			}
		} catch (WrapperCommandSyntaxException e) {
			throw e.getException();
		} catch (Throwable ex) {
			CommandAPI.getLogger().severe("Unhandled exception executing '" + info.args().fullInput() + "'", ex);
			if (ex instanceof Exception) {
				throw ex;
			} else {
				throw new RuntimeException(ex);
			}
		}

		// Executor not found
		throw new SimpleCommandExceptionType(new LiteralMessage(
			CommandAPI.getConfiguration().getMissingImplementationMessage()
				.replace("%s", info.sender().getClass().getSimpleName().toLowerCase())
				.replace("%S", info.sender().getClass().getSimpleName())
		)).create();
	}

	// This needs to be extracted into another method to name the `Sender` and `Source` generic, which allows `executeWith` to accept the converted info
	private <Sender extends CommandSender, Source> Optional<Integer> tryExecutor(TypedExecutor<CommandSender, Sender, Source> executor, ExecutionInfo<CommandSender, ?> info) throws WrapperCommandSyntaxException {
		ExecutionInfo<Sender, Source> convertedInfo = executor.tryForSender((ExecutionInfo<CommandSender, Source>) info);

		if (convertedInfo != null) {
			return Optional.of(executor.executeWith(convertedInfo));
		}
		return Optional.empty();
	}
}