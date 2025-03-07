package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.RegisteredCommand;
import dev.jorel.commandapi.arguments.AbstractArgument.NodeInformation;
import dev.jorel.commandapi.arguments.AbstractArgument.TerminalNodeModifier;
import dev.jorel.commandapi.commandnodes.NamedLiteralArgumentBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * An interface representing arguments with multiple literal string definitions
 */
public interface MultiLiteral<Argument
/// @cond DOX
extends AbstractArgument<?, ?, Argument, CommandSender>
/// @endcond
, CommandSender> {
	// MultiLiteral specific information

	/**
	 * Returns the literals that are present in this argument.
	 *
	 * @return the literals that are present in this argument.
	 * @apiNote A MultiLiteral argument must have at least one literal. E.g. the array returned by this method should
	 * never be empty.
	 */
	String[] getLiterals();

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// LINKED METHODS                                                                                    //
	//  These automatically link to methods in AbstractArgument (make sure they have the same signature) //
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Links to {@link AbstractArgument#getNodeName()}.
	 */
	String getNodeName();

	/**
	 * Links to {@link AbstractArgument#getArgumentPermission()}.
	 */
	CommandPermission getArgumentPermission();

	/**
	 * Links to {@link AbstractArgument#getRequirements()}.
	 */
	Predicate<CommandSender> getRequirements();

	/**
	 * Links to {@link AbstractArgument#isListed()}.
	 */
	boolean isListed();

	/**
	 * Links to {@link AbstractArgument#getCombinedArguments()}.
	 */
	List<Argument> getCombinedArguments();

	/**
	 * Links to {@link AbstractArgument#finishBuildingNode(ArgumentBuilder, List, TerminalNodeModifier)}.
	 */
	<Source> CommandNode<Source> finishBuildingNode(ArgumentBuilder<Source, ?> rootBuilder, List<Argument> previousArguments, TerminalNodeModifier<Argument, CommandSender, Source> terminalNodeModifier);

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDING METHODS                                                                             //
	//  A MultiLiteral has special logic that should override the implementations in AbstractArgument //
	////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Overrides {@link AbstractArgument#createArgumentBuilder(List, List)}.
	 * <p>
	 * Normally, Arguments will use Brigadier's RequiredArgumentBuilder. However, MultiLiterals use LiteralArgumentBuilders.
	 */
	default <Source> ArgumentBuilder<Source, ?> createArgumentBuilder(List<Argument> previousArguments, List<String> previousArgumentNames) {
		String nodeName = getNodeName();
		String literal = getLiterals()[0];

		previousArguments.add((Argument) this);
		if(isListed()) previousArgumentNames.add(nodeName);

		return isListed() ?
			NamedLiteralArgumentBuilder.namedLiteral(nodeName, literal) :
			LiteralArgumentBuilder.literal(literal);
	}

	/**
	 * Overrides {@link AbstractArgument#linkNode(NodeInformation, CommandNode, List, List, TerminalNodeModifier)}.
	 * <p>
	 * Normally, Arguments are only represented by a single node, and so only need to link one node. However, a MultiLiteral
	 * represents multiple literal node paths, which also need to be generated and inserted into the node structure.
	 */
	default <Source> NodeInformation<CommandSender, Source> linkNode(
		NodeInformation<CommandSender, Source> previousNodeInformation, CommandNode<Source> rootNode,
		List<Argument> previousArguments, List<String> previousArgumentNames,
		TerminalNodeModifier<Argument, CommandSender, Source> terminalNodeModifier
	) {
		List<CommandNode<Source>> newNodes = new ArrayList<>();
		// Add root node to previous
		for(CommandNode<Source> previousNode : previousNodeInformation.lastCommandNodes()) {
			previousNode.addChild(rootNode);
		}
		newNodes.add(rootNode);

		// Generate nodes for other literals
		String nodeName = getNodeName();
		boolean isListed = isListed();
		Iterator<String> literals = Arrays.asList(getLiterals()).iterator();
		literals.next(); // Skip first literal; that was handled by `#createArgumentBuilder`
		while (literals.hasNext()) {
			// Create node
			LiteralArgumentBuilder<Source> literalBuilder = isListed ?
				NamedLiteralArgumentBuilder.namedLiteral(nodeName, literals.next()) :
				LiteralArgumentBuilder.literal(literals.next());

			// Finish building node
			CommandNode<Source> literalNode = finishBuildingNode(literalBuilder, previousArguments, terminalNodeModifier);

			// Add node to previous
			for(CommandNode<Source> previousNode : previousNodeInformation.lastCommandNodes()) {
				previousNode.addChild(literalNode);
			}
			newNodes.add(literalNode);
		}

		// Create information for this node
		NodeInformation<CommandSender, Source> nodeInformation = new NodeInformation<>(
			newNodes, 
			// Create registered node information once children are created
			children -> previousNodeInformation.childrenConsumer().createNodeWithChildren(List.of(
				new RegisteredCommand.Node<>(
					nodeName, getClass().getSimpleName(), 
					"(" + String.join("|", getLiterals())+ ")",
					rootNode.getCommand() != null,
					getArgumentPermission(), getRequirements(),
					children
				)
			))
		);

		// Stack on combined arguments and return last nodes
		return AbstractArgument.stackArguments(getCombinedArguments(), nodeInformation, previousArguments, previousArgumentNames, terminalNodeModifier);
	}
}
