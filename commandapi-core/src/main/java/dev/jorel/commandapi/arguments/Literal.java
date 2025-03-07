package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.RegisteredCommand;
import dev.jorel.commandapi.arguments.AbstractArgument.NodeInformation;
import dev.jorel.commandapi.arguments.AbstractArgument.TerminalNodeModifier;
import dev.jorel.commandapi.commandnodes.NamedLiteralArgumentBuilder;

import java.util.List;
import java.util.function.Predicate;

/**
 * An interface representing literal-based arguments
 */
public interface Literal<Argument
/// @cond DOX
extends AbstractArgument<?, ?, Argument, CommandSender>
/// @endcond
, CommandSender> {
	// Literal specific information

	/**
	 * Returns the literal string represented by this argument
	 *
	 * @return the literal string represented by this argument
	 */
	String getLiteral();

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

	///////////////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDING METHODS                                                                        //
	//  A Literal has special logic that should override the implementations in AbstractArgument //
	///////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Overrides {@link AbstractArgument#createArgumentBuilder(List, List)}.
	 * <p>
	 * Normally, Arguments will use Brigadier's RequiredArgumentBuilder. However, Literals use LiteralArgumentBuilders.
	 * Arguments also usually add their name to the {@code previousArgumentNames} list here, but Literals only do
	 * this if they are listed.
	 */
	default <Source> ArgumentBuilder<Source, ?> createArgumentBuilder(List<Argument> previousArguments, List<String> previousArgumentNames) {
		String nodeName = getNodeName();
		String literal = getLiteral();

		previousArguments.add((Argument) this);
		if(isListed()) previousArgumentNames.add(nodeName);

		// If we are listed, use a NamedLiteralArgumentBuilder to put our literal into the CommandContext
		return isListed() ?
			NamedLiteralArgumentBuilder.namedLiteral(nodeName, literal) :
			LiteralArgumentBuilder.literal(literal);
	}

	/**
	 * Overrides {@link AbstractArgument#linkNode(NodeInformation, CommandNode, List, List, TerminalNodeModifier)}.
	 * <p>
	 * Normally, Arguments use their node name as their help string. However, a Literal uses its literal as the help string.
	 */
	default <Source> NodeInformation<CommandSender, Source> linkNode(
		NodeInformation<CommandSender, Source> previousNodeInformation, CommandNode<Source> rootNode,
		List<Argument> previousArguments, List<String> previousArgumentNames,
		TerminalNodeModifier<Argument, CommandSender, Source> terminalNodeModifier
	) {
		// Add rootNode to the previous nodes
		for(CommandNode<Source> previousNode : previousNodeInformation.lastCommandNodes()) {
			previousNode.addChild(rootNode);
		}

		// Create information for this node
		NodeInformation<CommandSender, Source> nodeInformation = new NodeInformation<>(
			List.of(rootNode),
			// Create registered node information once children are created
			children -> previousNodeInformation.childrenConsumer().createNodeWithChildren(List.of(
				new RegisteredCommand.Node<>(
					getNodeName(), getClass().getSimpleName(), getLiteral(), 
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
