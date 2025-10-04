package dev.jorel.commandapi.arguments.parser.function;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface ThrowingFunctionParser<T, R> {

	R apply(T t) throws CommandSyntaxException;

}
