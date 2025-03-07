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

import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.arguments.AbstractArgument;
import dev.jorel.commandapi.arguments.AbstractArgument.NodeInformation;
import dev.jorel.commandapi.arguments.AbstractArgument.TerminalNodeModifier;
import dev.jorel.commandapi.exceptions.MissingCommandExecutorException;
import dev.jorel.commandapi.exceptions.OptionalArgumentException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A builder used to create commands to be registered by the CommandAPI.
 *
 * @param <Impl>          The class extending this class, used as the return type for chain calls
 * @param <Argument>      The implementation of AbstractArgument used by the class extending this class
 * @param <CommandSender> The CommandSender class used by the class extending this class
 */
public abstract class AbstractCommandAPICommand<Impl
/// @cond DOX
extends AbstractCommandAPICommand<Impl, Argument, CommandSender>
/// @endcond
, Argument
/// @cond DOX
extends AbstractArgument<?, ?, Argument, CommandSender>
/// @endcond
, CommandSender> extends ExecutableCommand<Impl, CommandSender> {

	private List<Argument> requiredArguments = new ArrayList<>();
	private List<Argument> optionalArguments = new ArrayList<>();
	private List<Impl> subcommands = new ArrayList<>();

	/**
	 * Creates a new command builder
	 *
	 * @param commandName The name of the command to create
	 */
	protected AbstractCommandAPICommand(String commandName) {
		super(commandName);
	}

	/////////////////////
	// Builder methods //
	/////////////////////

	/**
	 * Appends the arguments to the current command builder
	 *
	 * @param args A <code>List</code> that represents the arguments that this
	 *             command can accept
	 * @return this command builder
	 * @throws OptionalArgumentException If this method is used to add required arguments after optional arguments.
	 */
	public Impl withArguments(List<Argument> args) {
		if (!args.isEmpty() && !this.optionalArguments.isEmpty()) {
			// Tried to add required arguments after optional arguments
			List<Argument> previousArguments = new ArrayList<>();
			previousArguments.addAll(this.requiredArguments);
			previousArguments.addAll(this.optionalArguments);
			throw new OptionalArgumentException(name, previousArguments, args.get(0));
		}
		this.requiredArguments.addAll(args);
		return instance();
	}

	/**
	 * Appends the argument(s) to the current command builder
	 *
	 * @param args Arguments that this command can accept
	 * @return this command builder
	 * @throws OptionalArgumentException If this method is used to add required arguments after optional arguments.
	 */
	@SafeVarargs
	public final Impl withArguments(Argument... args) {
		return this.withArguments(Arrays.asList(args));
	}

	/**
	 * Appends the optional arguments to the current command builder.
	 *
	 * @param args A <code>List</code> that represents the arguments that this
	 *             command can accept
	 * @return this command builder
	 */
	public Impl withOptionalArguments(List<Argument> args) {
		this.optionalArguments.addAll(args);
		return instance();
	}

	/**
	 * Appends the optional arguments to the current command builder.
	 *
	 * @param args Arguments that this command can accept
	 * @return this command builder
	 */
	@SafeVarargs
	public final Impl withOptionalArguments(Argument... args) {
		return this.withOptionalArguments(Arrays.asList(args));
	}

	/**
	 * Adds a subcommand to this command builder
	 *
	 * @param subcommand the subcommand to add as a child of this command
	 * @return this command builder
	 */
	public Impl withSubcommand(Impl subcommand) {
		this.subcommands.add(subcommand);
		return instance();
	}

	/**
	 * Adds subcommands to this command builder
	 *
	 * @param subcommands the subcommands to add as children of this command
	 * @return this command builder
	 */
	public Impl withSubcommands(List<Impl> subcommands) {
		this.subcommands.addAll(subcommands);
		return instance();
	}

	/**
	 * Adds subcommands to this command builder
	 *
	 * @param subcommands the subcommands to add as children of this command
	 * @return this command builder
	 */
	@SafeVarargs
	public final Impl withSubcommands(Impl... subcommands) {
		return this.withSubcommands(Arrays.asList(subcommands));
	}

	/////////////////////////
	// Getters and setters //
	/////////////////////////

	/**
	 * Returns the list of arguments that this command has
	 *
	 * @return the list of arguments that this command has
	 */
	public List<Argument> getArguments() {
		return requiredArguments;
	}

	/**
	 * Sets the arguments that this command has
	 *
	 * @param args the arguments that this command has
	 */
	public void setArguments(List<Argument> args) {
		this.requiredArguments = args;
	}

	/**
	 * Returns the list of optional arguments that this command has
	 *
	 * @return the list of optional arguments that this command has
	 */
	public List<Argument> getOptionalArguments() {
		return optionalArguments;
	}

	/**
	 * Sets the optional arguments that this command has
	 *
	 * @param args the optional arguments that this command has
	 */
	public void setOptionalArguments(List<Argument> args) {
		this.optionalArguments = args;
	}

	/**
	 * @return True if this command has any required or optional arguments.
	 */
	public boolean hasAnyArguments() {
		return !requiredArguments.isEmpty() || !optionalArguments.isEmpty();
	}

	/**
	 * Returns the list of subcommands that this command has
	 *
	 * @return the list of subcommands that this command has
	 */
	public List<Impl> getSubcommands() {
		return subcommands;
	}

	/**
	 * Sets the list of subcommands that this command has
	 *
	 * @param subcommands the list of subcommands that this command has
	 */
	public void setSubcommands(List<Impl> subcommands) {
		this.subcommands = subcommands;
	}

	//////////////////
	// Registration //
	//////////////////
	@Override
	protected void checkPreconditions() {
		if (!executor.hasAnyExecutors() && (subcommands.isEmpty() || hasAnyArguments())) {
			// If we don't have any executors then:
			//  No subcommands is bad because this path can't be run at all
			//  Having arguments is bad because developer intended this path to be executable with arguments
			throw new MissingCommandExecutorException(name);
		}
	}

	@Override
	protected boolean isRootExecutable() {
		return executor.hasAnyExecutors() && requiredArguments.isEmpty();
	}

	@Override
	protected <Source> List<RegisteredCommand.Node<CommandSender>> createArgumentNodes(LiteralCommandNode<Source> rootNode) {
		CommandAPIHandler<Argument, CommandSender, Source> handler = CommandAPIHandler.getInstance();

		List<RegisteredCommand.Node<CommandSender>> childrenNodes = new ArrayList<>();

		// Create arguments
		if (hasAnyArguments()) {
			NodeInformation<CommandSender, Source> previousNodeInformation = new NodeInformation<>(List.of(rootNode), childrenNodes::addAll);
			List<Argument> previousArguments = new ArrayList<>();
			List<String> previousArgumentNames = new ArrayList<>();

			// The previous arguments include an unlisted MultiLiteral representing the command name and aliases
			//  This doesn't affect how the command acts, but it helps represent the command path in exceptions
			String[] literals = new String[aliases.length + 1];
			literals[0] = name;
			System.arraycopy(aliases, 0, literals, 1, aliases.length);
			Argument commandNames = handler.getPlatform().newConcreteMultiLiteralArgument(name, literals);
			commandNames.setListed(false);

			previousArguments.add(commandNames);

			// Note that `executor#hasAnyExecutors` must be true here
			//  If not, then `checkPreconditions` would have thrown a `MissingCommandExecutorException` 
			TerminalNodeModifier<Argument, CommandSender, Source> executorCreator = (builder, args) ->
				builder.executes(handler.generateBrigadierCommand(args, executor)).build();

			// Stack required arguments
			// Only the last required argument is executable
			previousNodeInformation = AbstractArgument.stackArguments(
				requiredArguments, previousNodeInformation,
				previousArguments, previousArgumentNames,
				executorCreator
			);

			// Add optional arguments
			for (Argument argument : optionalArguments) {
				// All optional arguments are executable
				previousNodeInformation = argument.addArgumentNodes(
					previousNodeInformation,
					previousArguments, previousArgumentNames,
					executorCreator
				);
			}

			// Create registered nodes now that all children are generated
			previousNodeInformation.childrenConsumer().createNodeWithChildren(List.of());
		}

		// Add subcommands
		for (Impl subCommand : subcommands) {
			CommandInformation<CommandSender, Source> nodes = subCommand.createCommandInformation("");

			// Add root node
			rootNode.addChild(nodes.rootNode());

			RegisteredCommand.Node<CommandSender> rootNodeInformation = nodes.command().rootNode();
			childrenNodes.add(rootNodeInformation);

			// Add aliases
			for (LiteralCommandNode<Source> aliasNode : nodes.aliasNodes()) {
				rootNode.addChild(aliasNode);

				// Create node information for the alias
				String aliasName = aliasNode.getLiteral();
				childrenNodes.add(
					new RegisteredCommand.Node<>(
						aliasName, rootNodeInformation.className(), aliasName,
						rootNodeInformation.executable(), rootNodeInformation.permission(), rootNodeInformation.requirements(),
						rootNodeInformation.children()
					)
				);
			}
		}

		// Return node information
		return childrenNodes;
	}
}
