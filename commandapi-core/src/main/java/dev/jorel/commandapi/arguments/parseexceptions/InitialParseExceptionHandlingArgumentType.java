package dev.jorel.commandapi.arguments.parseexceptions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.wrappers.WrapperStringReader;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * An {@link ArgumentType} that wraps another {@link ArgumentType} and intercepts any
 * {@link CommandSyntaxException} to send to a developer-specified {@link InitialParseExceptionHandler}
 *
 * @param baseType The {@link ArgumentType} this object is wrapping.
 * @param exceptionHandler The {@link InitialParseExceptionHandler} that handles intercepted {@link CommandSyntaxException}.
 * @param exceptionParser A function that parses the information in a {@link CommandSyntaxException} to create an
 * {@link ExceptionInformation} object.
 *
 * @param <T> The object returned when the wrapped {@link ArgumentType} is parsed.
 * @param <ExceptionInformation> The class that holds information about the exception.
 */
public record InitialParseExceptionHandlingArgumentType<T, ExceptionInformation>(
        /**
         * @param baseType The {@link ArgumentType} this object is wrapping
         */
        ArgumentType<T> baseType,
        /**
         * @param exceptionHandler The {@link InitialParseExceptionHandler} that handles intercepted {@link CommandSyntaxException}
         */
        InitialParseExceptionHandler<T, ExceptionInformation> exceptionHandler,
		/**
		 * @param exceptionParser A function that parses the information in a {@link CommandSyntaxException} to create an
		 * {@link ExceptionInformation} object.
		 */
		InitialParseExceptionParser<ExceptionInformation> exceptionParser
) implements ArgumentType<T> {

    @Override
    public T parse(StringReader stringReader) throws CommandSyntaxException {
        int start = stringReader.getCursor();
        try {
            return baseType.parse(stringReader);
        } catch (CommandSyntaxException original) {
            try {
                return exceptionHandler.handleException(new InitialParseExceptionContext<>(
                        new WrapperCommandSyntaxException(original),
						exceptionParser.parse(original, stringReader),
                        new WrapperStringReader(stringReader),
                        start
                ));
            } catch (WrapperCommandSyntaxException newException) {
                throw newException.getException();
            }
        }
    }

    @Override
    public Collection<String> getExamples() {
        return baseType.getExamples();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return baseType.listSuggestions(context, builder);
    }
}