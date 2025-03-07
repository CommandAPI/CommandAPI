package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DynamicMultiLiteralArgument extends Argument<String> implements DynamicMultiLiteralArgumentCommon<Argument<?>, CommandSender> {
	// Setup information
	private final LiteralsCreator<CommandSender> literalsCreator;

	public DynamicMultiLiteralArgument(String nodeName, LiteralsCreator<CommandSender> literalsCreator) {
		super(nodeName, null);

		this.literalsCreator = literalsCreator;
	}

	@Override
	public LiteralsCreator<CommandSender> getLiteralsCreator() {
		return literalsCreator;
	}

	// Normal Argument stuff
	@Override
	public Class<String> getPrimitiveType() {
		return String.class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.DYNAMIC_MULTI_LITERAL;
	}

	@Override
	public <Source> String parseArgument(CommandContext<Source> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
		return cmdCtx.getArgument(key, String.class);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// DynamicMultiLiteralArgumentCommon interface overrides                                                          //
	// When a method in a parent class and interface have the same signature, Java will call the class version of the //
	//  method by default. However, we want to use the implementations found in the DynamicMultiLiteralArgumentCommon //
	//  interface.                                                                                                    //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public <Source> ArgumentBuilder<Source, ?> createArgumentBuilder(List<Argument<?>> previousArguments, List<String> previousArgumentNames) {
		return DynamicMultiLiteralArgumentCommon.super.createArgumentBuilder(previousArguments, previousArgumentNames);
	}
}
