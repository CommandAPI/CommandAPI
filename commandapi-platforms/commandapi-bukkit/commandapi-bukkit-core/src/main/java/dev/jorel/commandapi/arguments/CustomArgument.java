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
package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.jorel.commandapi.BukkitTooltip;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * An argument that represents any custom object
 *
 * @param <T> the return type of this custom argument when it is used
 * @param <B> the return type of the underlying base argument {@code base}. For
 *            example, this would be {@code Integer} for an
 *            {@link IntegerArgument}
 * 
 * @since 2.0
 * @apiNote Returns a {@link T} object
 */
public class CustomArgument<T, B> extends Argument<T> {

	private final CustomArgumentInfoParser<T, B> infoParser;
	private final Argument<B> base;

	private static final String INPUT = "%input%";
	private static final String FULL_INPUT = "%finput%";

	/**
	 * Creates a CustomArgument with a valid parser, with an underlying base
	 * argument as its parsing implementation.
	 * 
	 * @param base   the base argument to use for this custom argument. This base
	 *               argument will represent the parsing implementation for client
	 *               side and server side parsing. This base argument cannot be a
	 *               {@link LiteralArgument} or {@link MultiLiteralArgument}
	 * @param parser A {@link CustomArgumentInfo} parser object which includes
	 *               information such as the command sender, previously declared
	 *               arguments and current input. This parser should return an
	 *               object of your choice.
	 *               <p>
	 *               <b>{@code <T>}</b> the return type of this custom argument when
	 *               it is used <br>
	 *               <b>{@code <B>}</b> the return type of the underlying base
	 *               argument {@code base}. For example, this would be
	 *               {@code Integer} for an {@link IntegerArgument}
	 *               </p>
	 */
	public CustomArgument(Argument<B> base, CustomArgumentInfoParser<T, B> parser) {
		super(base.getNodeName(), base.getRawType());
		if (base instanceof LiteralArgument || base instanceof MultiLiteralArgument) {
			throw new IllegalArgumentException(base.getClass().getSimpleName() + " is not a suitable base argument type for a CustomArgument");
		}
		this.base = base;
		this.infoParser = parser;
	}

	@Override
	public Class<T> getPrimitiveType() {
		return null;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.CUSTOM;
	}

	@Override
	public <CommandSourceStack> T parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs)
		throws CommandSyntaxException {
		// Get the raw input and parsed input
		final String customresult = CommandAPIHandler.getRawArgumentInput(cmdCtx, key);
		final B parsedInput = base.parseArgument(cmdCtx, key, previousArgs);

		try {
			return infoParser.apply(new CustomArgumentInfo<>(CommandAPIBukkit.<CommandSourceStack>get().getCommandSenderFromCommandSource(cmdCtx.getSource()),
				previousArgs, customresult, parsedInput));
		} catch (CustomArgumentException e) {
			throw e.toCommandSyntax(customresult, cmdCtx);
		} catch (Exception e) {
			String errorMsg = new MessageBuilder("Error in executing command ").appendFullInput().append(" - ")
				.appendArgInput().appendHere().toString().replace(INPUT, customresult)
				.replace(FULL_INPUT, cmdCtx.getInput());
			throw new SimpleCommandExceptionType(() -> errorMsg).create();
		}
	}

	// All `AbstractArgument` builder method calls should be handled by the base argument
	@Override
	public Argument<T> includeSuggestions(ArgumentSuggestions<CommandSender> suggestions) {
		base.includeSuggestions(suggestions);
		return this;
	}

	@Override
	public Optional<ArgumentSuggestions<CommandSender>> getIncludedSuggestions() {
		return base.getIncludedSuggestions();
	}

	@Override
	public Argument<T> replaceSuggestions(ArgumentSuggestions<CommandSender> suggestions) {
		base.replaceSuggestions(suggestions);
		return this;
	}

	@Override
	public Optional<ArgumentSuggestions<CommandSender>> getOverriddenSuggestions() {
		return base.getOverriddenSuggestions();
	}

	@Override
	public Argument<T> withPermission(CommandPermission permission) {
		base.withPermission(permission);
		return this;
	}

	@Override
	public Argument<T> withPermission(String permission) {
		base.withPermission(permission);
		return this;
	}

	@Override
	public CommandPermission getArgumentPermission() {
		return base.getArgumentPermission();
	}

	@Override
	public Predicate<CommandSender> getRequirements() {
		return base.getRequirements();
	}

	@Override
	public Argument<T> withRequirement(Predicate<CommandSender> requirement) {
		base.withRequirement(requirement);
		return this;
	}

	@Override
	public boolean isListed() {
		return base.isListed();
	}

	@Override
	public Argument<T> setListed(boolean listed) {
		base.setListed(listed);
		return this;
	}

	@Override
	public List<Argument<?>> getCombinedArguments() {
		return base.getCombinedArguments();
	}

	@Override
	public boolean hasCombinedArguments() {
		return base.hasCombinedArguments();
	}

	@Override
	public Argument<T> combineWith(List<Argument<?>> combinedArguments) {
		base.combineWith(combinedArguments);
		return this;
	}

	@Override
	public <Source> NodeInformation<CommandSender, Source> addArgumentNodes(
		NodeInformation<CommandSender, Source> previousNodeInformation,
		List<Argument<?>> previousArguments, List<String> previousArgumentNames,
		TerminalNodeModifier<Argument<?>, CommandSender, Source> terminalNodeModifier
	) {
		// Node structure is determined by the base argument
		previousNodeInformation = base.addArgumentNodes(previousNodeInformation, previousArguments, previousArgumentNames,
			// Replace the base argument with this argument when executing the command
			(builder, args) -> {
				List<Argument<?>> newArgs = new ArrayList<>(args);
				newArgs.set(args.indexOf(base), this);
				return terminalNodeModifier.finishTerminalNode(builder, newArgs);
			}
		);

		// Replace the base argument with this argument when executing the command
		previousArguments.set(previousArguments.indexOf(base), this);

		return previousNodeInformation;
	}

	/**
	 * MessageBuilder is used to create error messages for invalid argument inputs
	 */
	public static class MessageBuilder implements Serializable {

		private static final long serialVersionUID = 838497662821791798L;

		StringBuilder builder;

		/**
		 * Create a blank message
		 */
		public MessageBuilder() {
			builder = new StringBuilder();
		}

		/**
		 * Create a message with an input string
		 * 
		 * @param str The string to start the message with
		 */
		public MessageBuilder(String str) {
			builder = new StringBuilder(str);
		}

		/**
		 * Appends the argument input that the CommandSender used in this command.<br>
		 * For example, if <code>/foo bar</code> was executed and an error occurs with
		 * the CustomArgument <code>bar</code>, then the arg input will append
		 * <code>bar</code> to the end of the message.<br>
		 * <br>
		 * This input is determined at runtime, and is stored as <code>%input%</code>
		 * until executed
		 * 
		 * @return A reference to this object
		 */
		public MessageBuilder appendArgInput() {
			builder.append(INPUT);
			return this;
		}

		/**
		 * Appends the whole input that the CommandSender used in this command.<br>
		 * For example, if <code>/foo bar</code> was executed, then <code>foo bar</code>
		 * will be appended to the end of the message.<br>
		 * <br>
		 * This input is determined at runtime, and is stored as <code>%finput%</code>
		 * until executed
		 * 
		 * @return A reference to this object
		 */
		public MessageBuilder appendFullInput() {
			builder.append(FULL_INPUT);
			return this;
		}

		/**
		 * Appends <code>&lt;--[HERE]</code> to the end of the message
		 * 
		 * @return A reference to this object
		 */
		public MessageBuilder appendHere() {
			builder.append("<--[HERE]");
			return this;
		}

		/**
		 * Appends a string to the end of this message
		 * 
		 * @param str The string to append to the end of this message
		 * @return A reference to this object
		 */
		public MessageBuilder append(String str) {
			builder.append(str);
			return this;
		}

		/**
		 * Appends an object to the end of this message
		 * 
		 * @param obj The object to append to the end of this message
		 * @return A reference to this object
		 */
		public MessageBuilder append(Object obj) {
			builder.append(obj);
			return this;
		}

		/**
		 * Returns the String content of this MessageBuilder
		 * 
		 * @return the String content of this MessageBuilder
		 */
		@Override
		public String toString() {
			return builder.toString();
		}
	}

	/**
	 * An exception used to create command-related errors for the CustomArgument
	 */
	@SuppressWarnings("serial")
	public static class CustomArgumentException extends Exception {

		private BaseComponent[] errorBaseComponent = null;
		private Component errorComponent = null;
		private String errorMessage = null;
		private MessageBuilder errorMessageBuilder = null;
		
		/* Prevent instantiation from any other sources */
		private CustomArgumentException() {
			
		}

		/**
		 * Constructs a CustomArgumentException with a given error message
		 * 
		 * @param errorMessage the error message to display to the user when this
		 *                     exception is thrown
		 * @deprecated Use {@link CustomArgumentException#fromBaseComponents(BaseComponent...)} instead
		 */
		@Deprecated(since = "9.0.1", forRemoval = true)
		public CustomArgumentException(BaseComponent[] errorMessage) {
			this.errorBaseComponent = errorMessage;
		}

		/**
		 * Constructs a CustomArgumentException with a given error message
		 * 
		 * @param errorMessage the error message to display to the user when this
		 *                     exception is thrown
		 * @deprecated Use {@link CustomArgumentException#fromString(String)} instead
		 */
		@Deprecated(since = "9.0.1", forRemoval = true)
		public CustomArgumentException(String errorMessage) {
			this.errorMessage = errorMessage;
		}

		/**
		 * Constructs a CustomArgumentException with a given error message
		 * 
		 * @param errorMessage the error message to display to the user when this
		 *                     exception is thrown
		 * @deprecated Use {@link CustomArgumentException#fromMessageBuilder(MessageBuilder)} instead
		 */
		@Deprecated(since = "9.0.1", forRemoval = true)
		public CustomArgumentException(MessageBuilder errorMessage) {
			this.errorMessageBuilder = errorMessage;
		}

		/**
		 * Constructs a CustomArgumentException with a given error message
		 * 
		 * @param errorMessage the error message to display to the user when this
		 *                     exception is thrown
		 */
		public static CustomArgumentException fromBaseComponents(BaseComponent[] errorMessage) {
			CustomArgumentException exception = new CustomArgumentException();
			exception.errorBaseComponent = errorMessage;
			return exception;
		}

		/**
		 * Constructs a CustomArgumentException with a given error message
		 * 
		 * @param errorMessage the error message to display to the user when this
		 *                     exception is thrown
		 */
		public static CustomArgumentException fromString(String errorMessage) {
			CustomArgumentException exception = new CustomArgumentException();
			exception.errorMessage = errorMessage;
			return exception;
		}

		/**
		 * Constructs a CustomArgumentException with a given error message
		 * 
		 * @param errorMessage the error message to display to the user when this
		 *                     exception is thrown
		 */
		public static CustomArgumentException fromAdventureComponent(Component errorMessage) {
			CustomArgumentException exception = new CustomArgumentException();
			exception.errorComponent = errorMessage;
			return exception;
		}

		/**
		 * Constructs a CustomArgumentException with a given error message
		 * 
		 * @param errorMessage the error message to display to the user when this
		 *                     exception is thrown
		 */
		public static CustomArgumentException fromMessageBuilder(MessageBuilder errorMessage) {
			CustomArgumentException exception = new CustomArgumentException();
			exception.errorMessageBuilder = errorMessage;
			return exception;
		}

		/**
		 * Converts this CustomArgumentException into a CommandSyntaxException
		 * 
		 * @param result the argument that the user entered that caused this exception
		 *               to arise
		 * @param cmdCtx the command context that executed this command
		 * @return a Brigadier CommandSyntaxException
		 */
		public CommandSyntaxException toCommandSyntax(String result, CommandContext<?> cmdCtx) {
			if (errorBaseComponent != null) {
				// Deal with BaseComponent[]
				Message brigadierMessage = BukkitTooltip.messageFromBaseComponents(errorBaseComponent);
				return new SimpleCommandExceptionType(brigadierMessage).create();
			}

			if (errorComponent != null) {
				// Deal with Adventure Component
				Message brigadierMessage = BukkitTooltip.messageFromAdventureComponent(errorComponent);
				return new SimpleCommandExceptionType(brigadierMessage).create();
			}

			if (errorMessageBuilder != null) {
				// Deal with MessageBuilder
				String errorMsg = errorMessageBuilder.toString().replace(INPUT, result).replace(FULL_INPUT,
					cmdCtx.getInput());
				return new SimpleCommandExceptionType(new LiteralMessage(errorMsg)).create();
			}

			if (errorMessage != null) {
				// Deal with String
				return CommandAPIBukkit.get().getPaper().getExceptionFromString(errorMessage).getException();
			}

			throw new IllegalStateException("No error component, error message creator or error message specified");
		}

	}

	/**
	 * A record which contains information which can be passed to the custom
	 * argument's parser.
	 * 
	 * @param sender       the sender that types this argument
	 * @param previousArgs previousArgs - a {@link CommandArguments} object holding previously declared (parsed)
	 *                     arguments. This can be used as if it were arguments in a command executor method.
	 * @param input        the current input which the user has typed for this
	 *                     argument
	 * @param currentInput the current input, when parsed with the underlying base
	 *                     argument.
	 */
	public record CustomArgumentInfo<B>(
		/**
		 * sender - the sender that types this argument
		 */
		CommandSender sender,

		/**
		 * previousArgs - a {@link CommandArguments} object holding previously declared (parsed) arguments. This can
		 * be used as if it were arguments in a command executor method.
		 */
		CommandArguments previousArgs,

		/**
		 * input - the current input which the user has typed for this argument
		 */
		String input,

		/**
		 * currentInput - the current input, when parsed with the underlying base
		 * argument.
		 */
		B currentInput) {
	}

	/**
	 * A FunctionalInterface that takes in a {@link CustomArgumentInfo}, returns T
	 * and can throw a {@link CustomArgumentException}
	 * 
	 * @param <T> the type that is returned when applying this parser
	 */
	@FunctionalInterface
	public static interface CustomArgumentInfoParser<T, B> {

		/**
		 * Applies a CustomArgumentInfo input to this custom argument parser
		 * 
		 * @param info the custom argument info to apply to this parser
		 * @return the applied output represented by this FunctionalInterface
		 * @throws CustomArgumentException if an error occurs during parsing
		 */
		public T apply(CustomArgumentInfo<B> info) throws CustomArgumentException;
	}
}
