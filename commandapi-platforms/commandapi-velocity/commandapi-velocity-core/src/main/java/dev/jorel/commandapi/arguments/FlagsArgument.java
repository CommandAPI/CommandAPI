package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.velocitypowered.api.command.CommandSource;
import dev.jorel.commandapi.executors.CommandArguments;

import java.util.ArrayList;
import java.util.List;

/**
 * @apiNote Yields a {@code List<}{@link CommandArguments}{@code >}
 */
@SuppressWarnings("rawtypes")
public class FlagsArgument extends Argument<List> implements FlagsArgumentCommon<FlagsArgument, Argument<?>, CommandSource> {
	// Setup information
	private final List<List<Argument<?>>> loopingBranches = new ArrayList<>();
	private final List<List<Argument<?>>> terminalBranches = new ArrayList<>();

	/**
	 * Constructs a {@link FlagsArgument}.
	 *
	 * @param nodeName the node name for this argument
	 */
	public FlagsArgument(String nodeName) {
		super(nodeName, null);
	}

	@Override
	public FlagsArgument loopingBranch(Argument<?>... arguments) {
		loopingBranches.add(List.of(arguments));
		return this;
	}

	@Override
	public List<List<Argument<?>>> getLoopingBranches() {
		return loopingBranches;
	}

	@Override
	public FlagsArgument terminalBranch(Argument<?>... arguments) {
		terminalBranches.add(List.of(arguments));
		return this;
	}

	@Override
	public List<List<Argument<?>>> getTerminalBranches() {
		return terminalBranches;
	}

	// Normal Argument stuff
	@Override
	public Class<List> getPrimitiveType() {
		return List.class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.FLAGS_ARGUMENT;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FlagsArgumentCommon interface overrides                                                                        //
	// When a method in a parent class and interface have the same signature, Java will call the class version of the //
	//  method by default. However, we want to use the implementations found in the FlagsArgumentCommon interface.    //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public <Source> List<CommandArguments> parseArgument(CommandContext<Source> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
		return FlagsArgumentCommon.super.parseArgument(cmdCtx, key, previousArgs);
	}

	@Override
	public <Source> NodeInformation<CommandSource, Source> addArgumentNodes(NodeInformation<CommandSource, Source> previousNodeInformation, List<Argument<?>> previousArguments, List<String> previousArgumentNames, TerminalNodeModifier<Argument<?>, CommandSource, Source> terminalNodeModifier) {
		return FlagsArgumentCommon.super.addArgumentNodes(previousNodeInformation, previousArguments, previousArgumentNames, terminalNodeModifier);
	}
}
