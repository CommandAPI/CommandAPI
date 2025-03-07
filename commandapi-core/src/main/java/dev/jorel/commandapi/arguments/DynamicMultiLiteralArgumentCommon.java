package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.jorel.commandapi.commandnodes.DynamicMultiLiteralArgumentBuilder;

import javax.annotation.Nullable;
import java.util.List;

public interface DynamicMultiLiteralArgumentCommon<Argument
/// @cond DOX
extends AbstractArgument<?, ?, ?, ?>
/// @endcond
, CommandSender> {
	// DynamicMultiLiteralArgument info
	@FunctionalInterface
	interface LiteralsCreator<CommandSender> {
		List<String> createLiterals(@Nullable CommandSender sender);
	}

	LiteralsCreator<CommandSender> getLiteralsCreator();

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// LINKED METHODS                                                                                    //
	//  These automatically link to methods in AbstractArgument (make sure they have the same signature) //
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Links to {@link AbstractArgument#getNodeName()}.
	 */
	String getNodeName();

	/**
	 * Links to {@link AbstractArgument#isListed()}.
	 */
	boolean isListed();

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDING METHODS                                                                                            //
	//  A DynamicMultiLiteralArgument has special logic that should override the implementations in AbstractArgument //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Overrides {@link AbstractArgument#createArgumentBuilder(List, List)}.
	 * <p>
	 * We want to use DynamicMultiLiteralArgumentBuilder rather than RequiredArgumentBuilder.
	 */
	default <Source> ArgumentBuilder<Source, ?> createArgumentBuilder(List<Argument> previousArguments, List<String> previousArgumentNames) {
		String name = getNodeName();
		boolean isListed = isListed();
		LiteralsCreator<CommandSender> literalsCreator = getLiteralsCreator();

		previousArguments.add((Argument) this);
		if (isListed()) previousArgumentNames.add(name);

		return DynamicMultiLiteralArgumentBuilder.dynamicMultiLiteral(name, isListed, literalsCreator);
	}
}
