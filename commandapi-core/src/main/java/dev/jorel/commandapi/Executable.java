package dev.jorel.commandapi;

import dev.jorel.commandapi.arguments.AbstractArgument;

import java.util.ArrayList;

/**
 * This class represents something that is executable. This is mostly, {@link AbstractCommandAPICommand} instances, or can also be {@link AbstractCommandTree} nodes and even {@link AbstractArgument} nodes in a tree
 *
 * @param <Impl> The class extending this class, used as the return type for chain calls
 * @param <CommandSender> The CommandSender class used by the class extending this class
 */
abstract class Executable<Impl
/// @cond DOX
extends Executable<Impl, CommandSender>
/// @endcond
, CommandSender> implements ChainableBuilder<Impl> {

	/**
	 * The CommandAPIExecutor for this executable implementation
	 */
	protected CommandAPIExecutor<CommandSender> executor = new CommandAPIExecutor<>();

	/**
	 * Returns the executors that this command has
	 * @return the executors that this command has
	 */
	public CommandAPIExecutor<CommandSender> getExecutor() {
		return executor;
	}

	/**
	 * Sets the executors for this command
	 * @param executor the executors for this command
	 */
	public void setExecutor(CommandAPIExecutor<CommandSender> executor) {
		this.executor = executor;
	}

	/**
	 * Clear all executors from the current command builder
	 * @return this command builder
	 */
	public Impl clearExecutors() {
		this.executor.setExecutors(new ArrayList<>());
		return instance();
	}

}
