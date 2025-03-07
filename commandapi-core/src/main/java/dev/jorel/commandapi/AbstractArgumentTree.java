package dev.jorel.commandapi;

import com.mojang.brigadier.tree.CommandNode;
import dev.jorel.commandapi.arguments.AbstractArgument;
import dev.jorel.commandapi.arguments.AbstractArgument.NodeInformation;
import dev.jorel.commandapi.arguments.AbstractArgument.TerminalNodeModifier;
import dev.jorel.commandapi.exceptions.MissingCommandExecutorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a base class for arguments, allowing them to behave as tree nodes in
 * a {@link AbstractCommandTree}
 * @param <Impl>The class extending this class, used as the return type for chain calls
 * @param <CommandSender> The CommandSender class used by the class extending this class
 */
public abstract class AbstractArgumentTree<Impl
/// @cond DOX
extends AbstractArgumentTree<Impl, Argument, CommandSender>
/// @endcond
, Argument
/// @cond DOX
extends AbstractArgument<?, ?, Argument, CommandSender>
/// @endcond
, CommandSender> extends Executable<Impl, CommandSender> {

	private final Argument argument;
	private List<AbstractArgumentTree<?, Argument, CommandSender>> arguments = new ArrayList<>();

	/**
	 * Instantiates an {@link AbstractArgumentTree}. This can only be called if the class
	 * that extends this is an {@link AbstractArgument}
	 */
	@SuppressWarnings("unchecked")
	protected AbstractArgumentTree() {
		if (this instanceof AbstractArgument<?, ?, ?, ?>) {
			this.argument = (Argument) this;
		} else {
			throw new IllegalArgumentException("Implicit inherited constructor must be from AbstractArgument");
		}
	}

	/**
	 * Instantiates an {@link AbstractArgumentTree} with an underlying argument.
	 *
	 * @param argument the argument to use as the underlying argument for this
	 *                 argument tree
	 */
	protected AbstractArgumentTree(final Argument argument) {
		this.argument = argument;
		// Copy the executor in case any executions were defined on the argument
		this.executor = argument.executor;
	}

	/////////////////////
	// Builder methods //
	/////////////////////

	/**
	 * Create a child branch on this node
	 *
	 * @param tree The child branch
	 * @return this tree node
	 */
	public Impl then(final AbstractArgumentTree<?, Argument, CommandSender> tree) {
		this.arguments.add(tree);
		return instance();
	}

	/**
	 * Creates a chain of child branches starting at this node
	 * <p>
	 * {@code thenNested(a, b, c)} is equivalent to {@link #then}{@code (a.then(b.then(c)))}.
	 *
	 * @param trees The child branches to add in a chain.
	 * @return this tree node
	 */
	public final Impl thenNested(List<AbstractArgumentTree<?, Argument, CommandSender>> trees) {
		int length = trees.size();
		if (length == 0) {
			return instance();
		}

		AbstractArgumentTree<?, Argument, CommandSender> combined = trees.get(length - 1);
		for (int i = length - 2; i >= 0; i--) {
			combined = trees.get(i).then(combined);
		}

		return then(combined);
	}

	/**
	 * Creates a chain of child branches starting at this node
	 * <p>
	 * {@code thenNested(a, b, c)} is equivalent to {@link #then}{@code (a.then(b.then(c)))}.
	 *
	 * @param trees The child branches to add in a chain.
	 * @return this tree node
	 */
	@SafeVarargs
	public final Impl thenNested(final AbstractArgumentTree<?, Argument, CommandSender>... trees) {
		return thenNested(Arrays.asList(trees));
	}

	/////////////////////////
	// Getters and setters //
	/////////////////////////

	/**
	 * @return The child branches added to this tree by {@link #then(AbstractArgumentTree)}.
	 */
	public List<AbstractArgumentTree<?, Argument, CommandSender>> getArguments() {
		return arguments;
	}

	/**
	 * Sets the child branches that this node has
	 *
	 * @param arguments A new list of branches for this node
	 */
	public void setArguments(List<AbstractArgumentTree<?, Argument, CommandSender>> arguments) {
		this.arguments = arguments;
	}

	//////////////////
	// Registration //
	//////////////////
	/**
	 * Builds the Brigadier {@link CommandNode} structure for this argument tree.
	 *
	 * @param previousNodeInformation The {@link NodeInformation} of the argument this argument is being added to.
	 * @param previousArguments       A List of CommandAPI arguments that came before this argument tree.
	 * @param previousArgumentNames   A List of Strings containing the node names that came before this argument.
	 * @param <Source>                The Brigadier Source object for running commands.
	 */
	public <Source> void buildBrigadierNode(
		NodeInformation<CommandSender, Source> previousNodeInformation,
		List<Argument> previousArguments, List<String> previousArgumentNames
	) {
		CommandAPIHandler<Argument, CommandSender, Source> handler = CommandAPIHandler.getInstance();

		// Check preconditions
		if (!executor.hasAnyExecutors() && arguments.isEmpty()) {
			// If we don't have any executors then no branches is bad because this path can't be run at all
			throw new MissingCommandExecutorException(previousArguments, argument);
		}

		// Create executor, if it exists
		TerminalNodeModifier<Argument, CommandSender, Source> terminalNodeModifier = (builder, args) -> {
			if (executor.hasAnyExecutors()) {
				builder.executes(handler.generateBrigadierCommand(args, executor));
			}

			return builder.build();
		};

		// Create node for this argument
		previousNodeInformation = argument.addArgumentNodes(
			previousNodeInformation,
			previousArguments, previousArgumentNames,
			terminalNodeModifier
		);

		// Collect children into our own list
		List<RegisteredCommand.Node<CommandSender>> childrenNodeInformation = new ArrayList<>();

		// Add our branches as children to the node
		for (AbstractArgumentTree<?, Argument, CommandSender> child : arguments) {
			// Collect children into our own list
			NodeInformation<CommandSender, Source> newPreviousNodeInformation = new NodeInformation<>(
				previousNodeInformation.lastCommandNodes(), 
				childrenNodeInformation::addAll
			);

			// We need a new list so each branch acts independently
			List<Argument> newPreviousArguments = new ArrayList<>(previousArguments);
			List<String> newPreviousArgumentNames = new ArrayList<>(previousArgumentNames);

			child.buildBrigadierNode(newPreviousNodeInformation, newPreviousArguments, newPreviousArgumentNames);
		}

		// Create registered nodes now that all children are generated
		previousNodeInformation.childrenConsumer().createNodeWithChildren(childrenNodeInformation);
	}
}
