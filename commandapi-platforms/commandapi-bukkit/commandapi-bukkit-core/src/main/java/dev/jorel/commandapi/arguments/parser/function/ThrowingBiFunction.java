package dev.jorel.commandapi.arguments.parser.function;

public interface ThrowingBiFunction<T, U, R, E extends Exception> {

	R apply(T t, U u) throws E;

}
