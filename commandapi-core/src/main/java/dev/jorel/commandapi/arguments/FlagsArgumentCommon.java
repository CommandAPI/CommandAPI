package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.RegisteredCommand;
import dev.jorel.commandapi.arguments.AbstractArgument.NodeInformation;
import dev.jorel.commandapi.arguments.AbstractArgument.TerminalNodeModifier;
import dev.jorel.commandapi.commandnodes.FlagsArgumentEndingNode;
import dev.jorel.commandapi.commandnodes.FlagsArgumentRootNode;
import dev.jorel.commandapi.executors.CommandArguments;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface FlagsArgumentCommon<Impl
/// @cond DOX
extends FlagsArgumentCommon<Impl, Argument, CommandSender>
/// @endcond
, Argument
/// @cond DOX
extends AbstractArgument<?, ?, Argument, CommandSender>
/// @endcond
, CommandSender> {
	// Setup information
	Impl loopingBranch(Argument... arguments);

	List<List<Argument>> getLoopingBranches();

	Impl terminalBranch(Argument... arguments);

	List<List<Argument>> getTerminalBranches();

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
	 * Links to {@link AbstractArgument#getCombinedArguments()}.
	 */
	List<Argument> getCombinedArguments();

	/**
	 * Links to {@link AbstractArgument#checkPreconditions(NodeInformation, List, List)}.
	 */
	<Source> void checkPreconditions(NodeInformation<CommandSender, Source> previousNodes, List<Argument> previousArguments, List<String> previousArgumentNames);

	/**
	 * Links to {@link AbstractArgument#finishBuildingNode(ArgumentBuilder, List, TerminalNodeModifier)}.
	 */
	<Source> CommandNode<Source> finishBuildingNode(ArgumentBuilder<Source, ?> rootBuilder, List<Argument> previousArguments, TerminalNodeModifier<Argument, CommandSender, Source> terminalNodeModifier);

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDING METHODS                                                                              //
	//  A FlagsArgument has special logic that should override the implementations in AbstractArgument //
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	record ParseInformation<Argument
	/// @cond DOX
	extends AbstractArgument<?, ?, Argument, CommandSender>
	/// @endcond
	, CommandSender, Source>
		(CommandContext<Source> context, List<Argument> arguments) {
	}

	/**
	 * Overrides {@link AbstractArgument#parseArgument(CommandContext, String, CommandArguments)}
	 */
	default <Source> List<CommandArguments> parseArgument(CommandContext<Source> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
		CommandAPIHandler<Argument, CommandSender, Source> handler = CommandAPIHandler.getInstance();

		List<ParseInformation<Argument, CommandSender, Source>> parseInformations = cmdCtx.getArgument(key, List.class);
		List<CommandArguments> results = new ArrayList<>(parseInformations.size());

		for (ParseInformation<Argument, CommandSender, Source> parseInformation : parseInformations) {
			results.add(handler.argsToCommandArgs(parseInformation.context(), parseInformation.arguments()));
		}

		return results;
	}

	/**
	 * Overrides {@link AbstractArgument#addArgumentNodes(NodeInformation, List, List, TerminalNodeModifier)}.
	 * <p>
	 * A FlagsArgument works completely differently from a typical argument, so we need to completely
	 * override the usual logic.
	 */
	default <Source> NodeInformation<CommandSender, Source> addArgumentNodes(
		NodeInformation<CommandSender, Source> previousNodeInformation,
		List<Argument> previousArguments, List<String> previousArgumentNames,
		TerminalNodeModifier<Argument, CommandSender, Source> terminalNodeModifier
	) {
		// Typical preconditions still apply
		checkPreconditions(previousNodeInformation, previousArguments, previousArgumentNames);

		// Add this argument to the lists
		String nodeName = getNodeName();
		previousArguments.add((Argument) this);
		previousArgumentNames.add(nodeName);

		// Create root node, add to previous
		LiteralArgumentBuilder<Source> rootBuilder = LiteralArgumentBuilder.literal(nodeName);
		finishBuildingNode(rootBuilder, previousArguments, (builder, args) -> builder.build());
		FlagsArgumentRootNode<Source> rootNode = new FlagsArgumentRootNode<>(rootBuilder.build());

		for (CommandNode<Source> previousNode : previousNodeInformation.lastCommandNodes()) {
			previousNode.addChild(rootNode);
		}

		// Setup looping branches
		boolean loopingBranchesTerminal = getTerminalBranches().isEmpty();
		TerminalNodeModifier<Argument, CommandSender, Source> loopingBranchModifier =
			loopingBranchesTerminal ? terminalNodeModifier : (builder, args) -> builder.build();

		for (List<Argument> loopingBranch : getLoopingBranches()) {
			setupBranch(
				loopingBranch, rootNode,
				previousArguments, previousArgumentNames,
				loopingBranchModifier, true
			);
		}

		// Setup terminal branches
		boolean terminalBranchesTerminal = getCombinedArguments().isEmpty();
		TerminalNodeModifier<Argument, CommandSender, Source> terminalBranchModifier =
			terminalBranchesTerminal ? terminalNodeModifier : (builder, args) -> builder.build();

		// The last nodes here will be our final nodes
		List<CommandNode<Source>> newNodes = new ArrayList<>();
		for (List<Argument> terminalBranch : getTerminalBranches()) {
			newNodes.addAll(setupBranch(
				terminalBranch, rootNode,
				previousArguments, previousArgumentNames,
				terminalBranchModifier, false
			));
		}

		// Create information for this node
		NodeInformation<CommandSender, Source> nodeInformation = new NodeInformation<>(
			newNodes, 
			// Create registered node information once children are created
			children -> previousNodeInformation.childrenConsumer().createNodeWithChildren(List.of(
				new RegisteredCommand.Node<>(
					nodeName, getClass().getSimpleName(), "<" + nodeName + ">", 
					loopingBranchesTerminal || terminalBranchesTerminal,
					getArgumentPermission(), getRequirements(),
					children
				)
			))
		);

		// Stack on combined arguments and return last nodes
		return AbstractArgument.stackArguments(
			getCombinedArguments(), nodeInformation,
			previousArguments, previousArgumentNames,
			terminalNodeModifier
		);
	}

	private <Source> List<CommandNode<Source>> setupBranch(
		List<Argument> branchArguments, FlagsArgumentRootNode<Source> rootNode,
		List<Argument> previousArguments, List<String> previousArgumentNames,
		TerminalNodeModifier<Argument, CommandSender, Source> terminalNodeModifier, boolean loop
	) {
		RootCommandNode<Source> branchRoot = new RootCommandNode<>();
		NodeInformation<CommandSender, Source> branchNodeInformation = new NodeInformation<>(List.of(branchRoot), null);

		// Stack branch nodes
		String nodeName = getNodeName();
		branchNodeInformation = AbstractArgument.stackArguments(
			branchArguments, branchNodeInformation,
			// Make clones of our lists to treat this branch independently
			new ArrayList<>(previousArguments), new ArrayList<>(previousArgumentNames),
			// Wrap terminal nodes into `FlagsArgumentEndingNode` to extract flag values
			(builder, branchPreviousArguments) -> {
				if (loop) {
					// Redirect node to flag root
					builder.redirect(rootNode);
				}

				// Finish building the regular node
				CommandNode<Source> rawEnd = terminalNodeModifier.finishTerminalNode(builder, branchPreviousArguments);

				// Wrap node
				CommandNode<Source> flagEnd = FlagsArgumentEndingNode.wrapNode(rawEnd, nodeName, branchPreviousArguments);

				if (loop) {
					// Ensure looping flagEnd has same children as root
					rootNode.addLoopEndNode(flagEnd);
				}

				return flagEnd;
			}
		);

		// Transfer branch nodes to the root node
		for (CommandNode<Source> child : branchRoot.getChildren()) {
			rootNode.addChild(child);
		}

		// Return the last nodes in the tree
		return branchNodeInformation.lastCommandNodes();
	}
}
