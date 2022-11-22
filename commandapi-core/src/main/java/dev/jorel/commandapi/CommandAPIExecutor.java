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

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.jorel.commandapi.commandsenders.*;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.ExecutorType;
import dev.jorel.commandapi.executors.IExecutorNormal;
import dev.jorel.commandapi.executors.IExecutorResulting;
import dev.jorel.commandapi.executors.IExecutorTyped;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * CommandAPIExecutor is the main executor implementation for command
 * executors. It contains a list of all executors (normal and resulting
 * executors) and switches its execution implementation based on the provided
 * command executor types.
 *
 * @param <CommandSender> The CommandSender for this executor
 * @param <WrapperType> The AbstractCommandSender that wraps the CommandSender
 */
public class CommandAPIExecutor<CommandSender, WrapperType extends AbstractCommandSender<? extends CommandSender>> {

	private List<IExecutorNormal<CommandSender, WrapperType>> normalExecutors;
	private List<IExecutorResulting<CommandSender, WrapperType>> resultingExecutors;

	public CommandAPIExecutor() {
		normalExecutors = new ArrayList<>();
		resultingExecutors = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	public void addNormalExecutor(IExecutorNormal<?, ?> executor) {
		this.normalExecutors.add((IExecutorNormal<CommandSender, WrapperType>) executor);
	}

	@SuppressWarnings("unchecked")
	public void addResultingExecutor(IExecutorResulting<?, ?> executor) {
		this.resultingExecutors.add((IExecutorResulting<CommandSender, WrapperType>) executor);
	}

	public int execute(WrapperType sender, Object[] arguments) throws CommandSyntaxException {

		// Parse executor type
		if (!resultingExecutors.isEmpty()) {
			// Run resulting executor
			try {
				return execute(resultingExecutors, sender, arguments);
			} catch (WrapperCommandSyntaxException e) {
				throw e.getException();
			} catch (Exception e) {
				e.printStackTrace(System.out);
				return 0;
			}
		} else {
			// Run normal executor
			try {
				return execute(normalExecutors, sender, arguments);
			} catch (WrapperCommandSyntaxException e) {
				throw e.getException();
			} catch (Exception e) {
				e.printStackTrace(System.out);
				return 0;
			}
		}
	}

	private int execute(List<? extends IExecutorTyped<WrapperType>> executors, WrapperType sender, Object[] args)
			throws WrapperCommandSyntaxException {
		if (isForceNative()) {
			return execute(executors, sender, args, ExecutorType.NATIVE);
		} else if (sender instanceof AbstractPlayer && matches(executors, ExecutorType.PLAYER)) {
			return execute(executors, sender, args, ExecutorType.PLAYER);
		} else if (sender instanceof AbstractEntity && matches(executors, ExecutorType.ENTITY)) {
			return execute(executors, sender, args, ExecutorType.ENTITY);
		} else if (sender instanceof AbstractConsoleCommandSender && matches(executors, ExecutorType.CONSOLE)) {
			return execute(executors, sender, args, ExecutorType.CONSOLE);
		} else if (sender instanceof AbstractBlockCommandSender && matches(executors, ExecutorType.BLOCK)) {
			return execute(executors, sender, args, ExecutorType.BLOCK);
		} else if (sender instanceof AbstractProxiedCommandSender && matches(executors, ExecutorType.PROXY)) {
			return execute(executors, sender, args, ExecutorType.PROXY);
		} else if (matches(executors, ExecutorType.ALL)) {
			return execute(executors, sender, args, ExecutorType.ALL);
		} else {
			throw new WrapperCommandSyntaxException(new SimpleCommandExceptionType(
					new LiteralMessage(CommandAPI.getConfiguration().getMissingImplementationMessage()
							.replace("%s", sender.getClass().getSimpleName().toLowerCase())
							.replace("%S", sender.getClass().getSimpleName()))).create());
		}
	}

	private int execute(List<? extends IExecutorTyped<WrapperType>> executors, WrapperType sender, Object[] args,
			ExecutorType type) throws WrapperCommandSyntaxException {
		for (IExecutorTyped<WrapperType> executor : executors) {
			if (executor.getType() == type) {
				return executor.executeWith(sender, args);
			}
		}
		throw new NoSuchElementException("Executor had no valid executors for type " + type.toString());
	}

	public List<IExecutorNormal<CommandSender, WrapperType>> getNormalExecutors() {
		return normalExecutors;
	}

	public List<IExecutorResulting<CommandSender, WrapperType>> getResultingExecutors() {
		return resultingExecutors;
	}

	public boolean hasAnyExecutors() {
		return !(normalExecutors.isEmpty() && resultingExecutors.isEmpty());
	}

	public boolean isForceNative() {
		return matches(normalExecutors, ExecutorType.NATIVE) || matches(resultingExecutors, ExecutorType.NATIVE);
	}

	private boolean matches(List<? extends IExecutorTyped<?>> executors, ExecutorType type) {
		for (IExecutorTyped<?> executor : executors) {
			if (executor.getType() == type) {
				return true;
			}
		}
		return false;
	}

	CommandAPIExecutor<CommandSender, WrapperType> mergeExecutor(CommandAPIExecutor<CommandSender, WrapperType> executor) {
		CommandAPIExecutor<CommandSender, WrapperType> result = new CommandAPIExecutor<>();
		result.normalExecutors = new ArrayList<>(normalExecutors);
		result.resultingExecutors = new ArrayList<>(resultingExecutors);
		result.normalExecutors.addAll(executor.normalExecutors);
		result.resultingExecutors.addAll(executor.resultingExecutors);
		return result;
	}

	public void setNormalExecutors(List<IExecutorNormal<CommandSender, WrapperType>> normalExecutors) {
		this.normalExecutors = normalExecutors;
	}

	public void setResultingExecutors(List<IExecutorResulting<CommandSender, WrapperType>> resultingExecutors) {
		this.resultingExecutors = resultingExecutors;
	}
}