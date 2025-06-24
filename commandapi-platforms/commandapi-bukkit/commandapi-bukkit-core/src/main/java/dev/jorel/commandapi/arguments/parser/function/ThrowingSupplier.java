package dev.jorel.commandapi.arguments.parser.function;

@FunctionalInterface
public interface ThrowingSupplier<T> {

	T get() throws Exception;

}
