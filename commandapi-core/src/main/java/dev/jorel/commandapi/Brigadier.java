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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.jorel.commandapi.arguments.AbstractArgument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.Literal;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * The Brigadier class is used to access some of the internals of the CommandAPI
 * so you can use the CommandAPI alongside Mojang's com.mojang.brigadier package
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class Brigadier {

	// Cannot be instantiated
	private Brigadier() {
	}

	/**
	 * Returns the Brigadier CommandDispatcher tree that is used internally by the
	 * CommandAPI. Modifying this CommandDispatcher tree before the server finishes
	 * loading will still keep any changes made to it. For example, adding a new
	 * node to this tree will keep the node once the server has finished loading.
	 * 
	 * @return The CommandAPI's internal CommandDispatcher instance
	 */
	public static CommandDispatcher getCommandDispatcher() {
		return CommandAPIHandler.getInstance().getPlatform().getBrigadierDispatcher();
	}

	/**
	 * Returns the root node of the current CommandDispatcher. This is the
	 * equivalent of running
	 * 
	 * <code>
	 * Brigadier.getCommandDispatcher().getRoot();
	 * </code>
	 * 
	 * @return The Brigadier CommandDispatcher's root node
	 */
	public static RootCommandNode getRootNode() {
		return getCommandDispatcher().getRoot();
	}

	/**
	 * Creates a new literal argument builder from a CommandAPI LiteralArgument
	 * 
	 * @param literalArgument the LiteralArgument to convert from
	 * @return a LiteralArgumentBuilder that represents the literal
	 */
	public static <CommandSender, Argument extends AbstractArgument<?, ?, Argument, CommandSender>>
	LiteralArgumentBuilder fromLiteralArgument(Literal<Argument, CommandSender> literalArgument) {
		Argument argument = (Argument) literalArgument;
		LiteralArgumentBuilder rootBuilder = (LiteralArgumentBuilder) argument.createArgumentBuilder(List.of(), List.of());
		argument.finishBuildingNode(rootBuilder, List.of(), null);

		return rootBuilder;
	}

	/**
	 * Constructs a RedirectModifier from a predicate that uses a command sender and
	 * some arguments. RedirectModifiers can be used with Brigadier's
	 * <code>fork()</code> method to invoke other nodes in the CommandDispatcher
	 * tree. You would use this method as shown:
	 * 
	 * <pre>
	 * Brigadier.fromPredicate((sender, args) -&gt; {
	 *     ...
	 * }, arguments);
	 * </pre>
	 * 
	 * @param predicate the predicate to test
	 * @param args      the arguments that the sender has filled in
	 * @return a RedirectModifier that encapsulates the provided predicate
	 */
	public static <CommandSender, Argument extends AbstractArgument<?, ?, Argument, CommandSender>>
	RedirectModifier fromPredicate(BiPredicate<CommandSender, Object[]> predicate, List<Argument> args) {
		return cmdCtx -> {
			if (predicate.test(getCommandSenderFromContext(cmdCtx), parseArguments(cmdCtx, args))) {
				return Collections.singleton(cmdCtx.getSource());
			} else {
				return Collections.emptyList();
			}
		};
	}

	/**
	 * Converts a CommandAPICommand into a Brigadier Command
	 * 
	 * @param command the command to convert
	 * @return a Brigadier Command object that represents the provided command
	 */
	public static <Argument extends AbstractArgument<?, ?, Argument, CommandSender>, CommandSender>
	Command fromCommand(AbstractCommandAPICommand<?, Argument, CommandSender> command) {
		// Need to cast base handler to make it realize we're using the same CommandSender class
		CommandAPIHandler<Argument, CommandSender, ?> handler = CommandAPIHandler.getInstance();
		return handler.generateBrigadierCommand(command.getArguments(), command.getExecutor());
	}

	/**
	 * Constructs a RequiredArgumentBuilder from a given argument within a command
	 * declaration. For example:
	 * 
	 * <pre>
	 * List&lt;Argument&gt; arguments = new ArrayList&lt;&gt;();
	 * arguments.add(new IntegerArgument("hello"));
	 * 
	 * RequiredArgumentBuilder argBuilder = Brigadier.fromArguments(arguments, "hello");
	 * </pre>
	 *
	 * @param previousArguments the List of arguments which you typically declare for
	 *                          commands
	 * @param argument          the argument you want to specify
	 * @return a RequiredArgumentBuilder that represents the provided argument
	 */
	public static <Argument extends AbstractArgument<?, ?, Argument, ?>> RequiredArgumentBuilder fromArgument(List<Argument> previousArguments, Argument argument) {
		RequiredArgumentBuilder rootBuilder = (RequiredArgumentBuilder) argument.createArgumentBuilder(previousArguments, List.of());
		argument.finishBuildingNode(rootBuilder, previousArguments, null);

		return rootBuilder;
	}

	/**
	 * Constructs a RequiredArgumentBuilder from a given argument
	 * 
	 * @param argument the argument to create a RequiredArgumentBuilder from
	 * @return a RequiredArgumentBuilder that represents the provided argument
	 */
	public static <Argument extends AbstractArgument<?, ?, Argument, ?>> RequiredArgumentBuilder fromArgument(Argument argument) {
		return fromArgument(List.of(), argument);
	}

	/**
	 * Converts an argument and a list of arguments to a Brigadier
	 * SuggestionProvider
	 *
	 * @param argument          the argument to convert to suggestions
	 * @param previousArguments the list of arguments that came before the given argument
	 * @return a SuggestionProvider that suggests the overridden suggestions for the
	 * specified argument
	 */
	public static <Argument extends AbstractArgument<?, ?, Argument, CommandSender>, CommandSender> SuggestionProvider toSuggestions(Argument argument, List<Argument> previousArguments) {
		CommandAPIHandler<Argument, CommandSender, ?> handler = CommandAPIHandler.getInstance();
		return handler.generateBrigadierSuggestions(previousArguments, argument.getOverriddenSuggestions().orElse(ArgumentSuggestions.empty()));
	}

	/**
	 * Parses arguments into their respective objects with a given command context.
	 * This method effectively performs the "parse" step in an argument's class and
	 * returns an Object[] which maps directly to the input List with the values
	 * generated via parsing.
	 * 
	 * @param cmdCtx the command context used to parse the command arguments
	 * @param args   the list of arguments to parse
	 * @return an array of Objects which hold the results of the argument parsing
	 * step
	 * @throws CommandSyntaxException if there was an error during parsing
	 */
	public static <Argument extends AbstractArgument<?, ?, Argument, ?>> Object[] parseArguments(CommandContext cmdCtx, List<Argument> args) throws CommandSyntaxException {
		CommandAPIHandler<Argument, ?, ?> handler = (CommandAPIHandler<Argument, ?, ?>) CommandAPIHandler.getInstance();
		return handler.argsToCommandArgs(cmdCtx, args).args();
	}

	/**
	 * Gets a Brigadier source object (e.g. CommandListenerWrapper or
	 * CommandSourceStack) from a Bukkit CommandSender. This source object is the
	 * same object you would get from a command context.
	 * 
	 * @param sender the Bukkit CommandSender to convert into a Brigadier source
	 *               object
	 * @return a Brigadier source object representing the provided Bukkit
	 * CommandSender
	 */
	public static <CommandSender> Object getBrigadierSourceFromCommandSender(CommandSender sender) {
		CommandAPIPlatform<?, CommandSender, ?> platform = (CommandAPIPlatform<?, CommandSender, ?>) CommandAPIHandler.getInstance().getPlatform();
		return platform.getBrigadierSourceFromCommandSender(sender);
	}

	/**
	 * Returns the current platform's command sender from a Brigadier CommandContext
	 * 
	 * @param cmdCtx the command context to get the CommandSender from
	 * @return a command sender from the provided Brigadier CommandContext
	 */
	public static <CommandSender, Source> CommandSender getCommandSenderFromContext(CommandContext cmdCtx) {
		CommandAPIPlatform<?, CommandSender, Source> platform = (CommandAPIPlatform<?, CommandSender, Source>) CommandAPIHandler.getInstance().getPlatform();
		return platform.getCommandSenderFromCommandSource((Source) cmdCtx.getSource());
	}
}
