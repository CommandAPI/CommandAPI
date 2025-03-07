package dev.jorel.commandapi;

import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.arguments.AbstractArgument;
import dev.jorel.commandapi.arguments.AbstractArgument.NodeInformation;
import dev.jorel.commandapi.exceptions.MissingCommandExecutorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is the root node for creating a command as a tree
 * @param <Impl> The class extending this class, used as the return type for chain calls
 * @param <Argument> The implementation of AbstractArgument being used by this class
 * @param <CommandSender> The CommandSender class used by the class extending this class
 */
public abstract class AbstractCommandTree<Impl
/// @cond DOX
extends AbstractCommandTree<Impl, Argument, CommandSender>
/// @endcond
, Argument
/// @cond DOX
extends AbstractArgument<?, ?, Argument, CommandSender>
/// @endcond
, CommandSender> extends ExecutableCommand<Impl, CommandSender> {

	private List<AbstractArgumentTree<?, Argument, CommandSender>> arguments = new ArrayList<>();

	/**
	 * Creates a main root node for a command tree with a given command name
	 *
	 * @param commandName The name of the command to create
	 */
	protected AbstractCommandTree(final String commandName) {
		super(commandName);
	}

	/////////////////////
	// Builder methods //
	/////////////////////

	/**
	 * Create a child branch on the tree
	 *
	 * @param tree the child node
	 * @return this root node
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
	 * Sets the child branches that this command has
	 *
	 * @param arguments A new list of branches for this command
	 */
	public void setArguments(List<AbstractArgumentTree<?, Argument, CommandSender>> arguments) {
		this.arguments = arguments;
	}

	//////////////////
	// Registration //
	//////////////////
	@Override
	protected void checkPreconditions() {
		if (!executor.hasAnyExecutors() && arguments.isEmpty()) {
			// If we don't have any executors then no branches is bad because this path can't be run at all
			throw new MissingCommandExecutorException(name);
		}
	}

	@Override
	protected boolean isRootExecutable() {
		return executor.hasAnyExecutors();
	}

	@Override
	protected <Source> List<RegisteredCommand.Node<CommandSender>> createArgumentNodes(LiteralCommandNode<Source> rootNode) {
		CommandAPIHandler<Argument, CommandSender, Source> handler = CommandAPIHandler.getInstance();

		List<RegisteredCommand.Node<CommandSender>> childrenNodes = new ArrayList<>();

		// The previous arguments include an unlisted MultiLiteral representing the command name and aliases
		//  This doesn't affect how the command acts, but it helps represent the command path in exceptions
		String[] literals = new String[aliases.length + 1];
		literals[0] = name;
		System.arraycopy(aliases, 0, literals, 1, aliases.length);
		Argument commandNames = handler.getPlatform().newConcreteMultiLiteralArgument(name, literals);
		commandNames.setListed(false);

		// Build branches
		for (AbstractArgumentTree<?, Argument, CommandSender> argument : arguments) {
			// We need new previousArguments lists for each branch so they don't interfere
			NodeInformation<CommandSender, Source> previousNodeInformation = new NodeInformation<>(List.of(rootNode), childrenNodes::addAll);
			List<Argument> previousArguments = new ArrayList<>();
			List<String> previousArgumentNames = new ArrayList<>();

			previousArguments.add(commandNames);

			argument.buildBrigadierNode(previousNodeInformation, previousArguments, previousArgumentNames);
		}

		// Return children
		return childrenNodes;
	}
}
