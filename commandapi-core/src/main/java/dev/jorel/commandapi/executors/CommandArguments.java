package dev.jorel.commandapi.executors;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import dev.jorel.commandapi.arguments.AbstractArgument;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * This class stores the arguments for this command
 *
 * @param args      The arguments for this command
 * @param argsMap   The arguments for this command mapped to their node names. This is an ordered map
 * @param rawArgs   The raw arguments for this command.
 * @param rawArgsMap   The raw arguments for this command mapped to their node names. This is an ordered map
 * @param fullInput The command string a player has entered (including the /)
 */
@NullMarked
@SuppressWarnings("unchecked")
public record CommandArguments(

	/**
	 * @param The arguments for this command
	 */
	Object[] args,

	/**
	 *  @param The arguments for this command mapped to their node names. This is an ordered map
	 */
	Map<String, Object> argsMap,

	/**
	 * @param The raw arguments for this command
	 */
	String[] rawArgs,

	/**
	 * @param The raw arguments for this command mapped to their node names. This is an ordered map
	 */
	Map<String, String> rawArgsMap,

	/**
	 * @param The command string a player has entered (including the /)
	 */
	String fullInput
) {

	private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
		boolean.class, Boolean.class,
		char.class, Character.class,
		byte.class, Byte.class,
		short.class, Short.class,
		int.class, Integer.class,
		long.class, Long.class,
		float.class, Float.class,
		double.class, Double.class
	);

	// Access the inner structure directly

	/**
	 * @return An unmodifiable clone of the mapping of node names to argument values
	 */
	public Map<String, Object> argsMap() {
		return Collections.unmodifiableMap(argsMap);
	}

	/**
	 * @return An unmodifiable clone of the mapping of node names to raw arguments
	 */
	public Map<String, String> rawArgsMap() {
		return Collections.unmodifiableMap(rawArgsMap);
	}

	/**
	 * @return The number of arguments for this command
	 */
	public int count() {
		return args.length;
	}

	// Main accessing methods. In Kotlin, methods named get() allows it to
	// access these methods using array notation, as a part of operator overloading.
	// More information about operator overloading in Kotlin can be found here:
	// https://kotlinlang.org/docs/operator-overloading.html

	/**
	 * Returns an argument by its index
	 *
	 * @param index The index of the argument
	 * @param <T> The expected type of the argument
	 * @return The argument at the given index
	 * @throws IllegalArgumentException If no argument is found for the index
	 */
	public <T> T get(int index) {
		if (index < 0 || index >= args.length) {
			throw new IllegalArgumentException("No argument found at index " + index);
		}

		return (T) args[index];
	}

	/**
	 * Returns an argument by its node name
	 *
	 * @param nodeName The node name of the argument
	 * @param <T> The expected type of the argument
	 * @return The argument with given node name
	 * @throws IllegalArgumentException If no argument is found for the given node name
	 */
	public <T> T get(String nodeName) {
		T value = (T) argsMap.get(nodeName);
		if (value == null) {
			throw new IllegalArgumentException("No argument found for name " + nodeName);
		}

		return value;
	}

	/**
	 * Returns an argument by its index
	 *
	 * @param index The index of the argument
	 * @param <T> The expected type of the argument
	 * @param defaultValue The value returned when no argument is found for the index
	 * @return The argument at the given index, or the default value
	 */
	@Contract("_, !null -> !null")
	public <T> @Nullable T getOrDefault(int index, @Nullable T defaultValue) {
		if (index < 0 || index >= args.length) {
			return defaultValue;
		}

		return (T) args[index];
	}

	/**
	 * Returns an argument by its node name
	 *
	 * @param nodeName The node name of the argument
	 * @param <T> The expected type of the argument
	 * @param defaultValue The value returned when no argument is found for the node name
	 * @return The argument with given node name, or the default value
	 */
	@Contract("_, !null -> !null")
	public <T> @Nullable T getOrDefault(String nodeName, @Nullable T defaultValue) {
		return (T) argsMap.getOrDefault(nodeName, defaultValue);
	}

	/**
	 * Returns an argument by its index
	 *
	 * @param index The index of the argument
	 * @param <T> The expected type of the argument
	 * @param defaultValue The value returned when no argument is found for the index
	 * @return The argument at the given index, or the default value
	 */
	public <T> @Nullable T getOrDefault(int index, Supplier<@Nullable T> defaultValue) {
		if (index < 0 || index >= args.length) {
			return defaultValue.get();
		}

		return (T) args[index];
	}

	/**
	 * Returns an argument by its node name
	 *
	 * @param nodeName The node name of the argument
	 * @param <T> The expected type of the argument
	 * @param defaultValue The value returned when no argument is found for the node name
	 * @return The argument with the given node name, or the default value
	 */
	public <T> @Nullable T getOrDefault(String nodeName, Supplier<@Nullable T> defaultValue) {
		T value = (T) argsMap.get(nodeName);
		return value != null ? value : defaultValue.get();
	}

	/**
	 * Returns an {@link Optional} holding the argument by its index
	 *
	 * @param index The index of the argument
	 * @param <T> The expected type of the argument
	 * @return An optional holding the argument at the given index, or an empty optional if no argument was found
	 */
	public <T> Optional<T> getOptional(int index) {
		if (index < 0 || index >= args.length) {
			return Optional.empty();
		}

		return Optional.of((T) args[index]);
	}

	/**
	 * Returns an {@link Optional} holding the argument by its node name
	 *
	 * @param nodeName The node name of the argument
	 * @param <T> The expected type of the argument
	 * @return An optional holding the argument with the given node name, or an empty optional if no argument was found
	 */
	public <T> Optional<T> getOptional(String nodeName) {
		return Optional.ofNullable((T) argsMap.get(nodeName));
	}

	/**
	 * Returns a raw argument by its index
	 *
	 * <p>
	 * A raw argument is the {@link String} form of an argument as written in a command. For example take this command:
	 * <p>
	 * {@code /mycommand @e 15.3}
	 * <p>
	 * When using this method to access these arguments, {@code @e} and {@code 15.3} will be available as {@link String}s and not as a {@link Collection} and {@link Double}
	 *
	 * @return The raw argument at the given index
	 * @throws IllegalArgumentException If no argument is found for the index
	 */
	public String getRaw(int index) {
		if (index < 0 || index >= args.length) {
			throw new IllegalArgumentException("No argument found at index " + index);
		}

		return rawArgs[index];
	}

	/**
	 * Returns a raw argument by its node name
	 *
	 * <p>
	 * A raw argument is the {@link String} form of an argument as written in a command. For example take this command:
	 * <p>
	 * {@code /mycommand @e 15.3}
	 * <p>
	 * When using this method to access these arguments, {@code @e} and {@code 15.3} will be available as {@link String}s and not as a {@link Collection} and {@link Double}
	 *
	 * @param nodeName The node name of the argument
	 * @return The raw argument with given node name
	 * @throws NullPointerException If no argument is found for the given node name
	 */
	public String getRaw(String nodeName) {
		String value = rawArgsMap.get(nodeName);
		if (value == null) {
			throw new IllegalArgumentException("No argument found for name " + nodeName);
		}

		return value;
	}

	/**
	 * Returns a raw argument by its index
	 *
	 * <p>
	 * A raw argument is the {@link String} form of an argument as written in a command. For example take this command:
	 * <p>
	 * {@code /mycommand @e 15.3}
	 * <p>
	 * When using this method to access these arguments, {@code @e} and {@code 15.3} will be available as {@link String}s and not as a {@link Collection} and {@link Double}
	 *
	 * @param index The index of the argument
	 * @param defaultValue The String returned when no argument is found for the index
	 * @return The raw argument at the given index, or the default value
	 */
	@Contract("_, !null -> !null")
	public @Nullable String getOrDefaultRaw(int index, @Nullable String defaultValue) {
		if (index < 0 || index >= rawArgs.length) {
			return defaultValue;
		}

		return rawArgs[index];
	}

	/**
	 * Returns a raw argument by its node name
	 *
	 * <p>
	 * A raw argument is the {@link String} form of an argument as written in a command. For example take this command:
	 * <p>
	 * {@code /mycommand @e 15.3}
	 * <p>
	 * When using this method to access these arguments, {@code @e} and {@code 15.3} will be available as {@link String}s and not as a {@link Collection} and {@link Double}
	 *
	 * @param nodeName The node name of the argument
	 * @param defaultValue The String returned when no argument is found for the node name
	 * @return The raw argument with given node name, or the default value
	 */
	@Contract("_, !null -> !null")
	public @Nullable String getOrDefaultRaw(String nodeName, @Nullable String defaultValue) {
		return rawArgsMap.getOrDefault(nodeName, defaultValue);
	}

	/**
	 * Returns a raw argument by its index
	 *
	 * <p>
	 * A raw argument is the {@link String} form of an argument as written in a command. For example take this command:
	 * <p>
	 * {@code /mycommand @e 15.3}
	 * <p>
	 * When using this method to access these arguments, {@code @e} and {@code 15.3} will be available as {@link String}s and not as a {@link Collection} and {@link Double}
	 *
	 * @param index The index of the argument
	 * @param defaultValue The String returned when no argument is found for the index
	 * @return The raw argument at the given index, or the default value
	 */
	public @Nullable String getOrDefaultRaw(int index, Supplier<@Nullable String> defaultValue) {
		if (index < 0 || index >= rawArgs.length) {
			return defaultValue.get();
		}

		return rawArgs[index];
	}

	/**
	 * Returns a raw argument by its node name
	 *
	 * <p>
	 * A raw argument is the {@link String} form of an argument as written in a command. For example take this command:
	 * <p>
	 * {@code /mycommand @e 15.3}
	 * <p>
	 * When using this method to access these arguments, {@code @e} and {@code 15.3} will be available as {@link String}s and not as a {@link Collection} and {@link Double}
	 *
	 * @param nodeName The node name of the argument
	 * @param defaultValue The String returned when no argument is found for the node name
	 * @return The raw argument with the given node name, or the default value
	 */
	public @Nullable String getOrDefaultRaw(String nodeName, Supplier<@Nullable String> defaultValue) {
		String value = rawArgsMap.get(nodeName);
		return value != null ? value : defaultValue.get();
	}

	/**
	 * Returns an {@link Optional} holding the raw argument by its index
	 *
	 * <p>
	 * A raw argument is the {@link String} form of an argument as written in a command. For example take this command:
	 * <p>
	 * {@code /mycommand @e 15.3}
	 * <p>
	 * When using this method to access these arguments, {@code @e} and {@code 15.3} will be available as {@link String}s and not as a {@link Collection} and {@link Double}
	 *
	 * @param index The index of the argument
	 * @return An optional holding the raw argument at the given index, or an empty optional if no argument was found
	 */
	public Optional<String> getRawOptional(int index) {
		if (index < 0 || index >= args.length) {
			return Optional.empty();
		}

		return Optional.of(rawArgs[index]);
	}

	/**
	 * Returns an {@link Optional} holding the raw argument by its node name
	 *
	 * <p>
	 * A raw argument is the {@link String} form of an argument as written in a command. For example take this command:
	 * <p>
	 * {@code /mycommand @e 15.3}
	 * <p>
	 * When using this method to access these arguments, {@code @e} and {@code 15.3} will be available as {@link String}s and not as a {@link Collection} and {@link Double}
	 *
	 * @param nodeName The node name of the argument
	 * @return An optional holding the raw argument with the given node name, or an empty optional if no argument was found
	 */
	public Optional<String> getRawOptional(String nodeName) {
		return Optional.ofNullable(rawArgsMap.get(nodeName));
	}

	/*****************************************
	 ********** SAFE-CAST ARGUMENTS **********
	 *****************************************/

	/**
	 * Returns an argument by its argument instance
	 *
	 * @param argumentType The argument instance used to create the argument
	 * @param <T> The expected type of the argument
	 * @return The argument represented by the argument instance
	 * @throws NullPointerException If no argument is found for the argument instance
	 */
	public <T> T getByArgument(AbstractArgument<T, ?, ?, ?> argumentType) {
		return castArgument(get(argumentType.getNodeName()), argumentType.getPrimitiveType(), argumentType.getNodeName());
	}

	/**
	 * Returns an argument by its argument instance
	 *
	 * @param argumentType The argument instance used to create the argument
	 * @param <T> The expected type of the argument
	 * @param defaultValue The value returned when no argument is found for the argument instance
	 * @return The argument represented by the argument instance, or the default value
	 */
	@Contract("_, !null -> !null")
	public <T> @Nullable T getByArgumentOrDefault(AbstractArgument<T, ?, ?, ?> argumentType, @Nullable T defaultValue) {
		return castArgument(getOrDefault(argumentType.getNodeName(), defaultValue), argumentType.getPrimitiveType(), argumentType.getNodeName());
	}

	/**
	 * Returns an {@link Optional} holding the argument by its argument instance
	 *
	 * @param argumentType The argument instance used to create the argument
	 * @param <T> The expected type of the argument
	 * @return An optional holding the argument represented by the argument instance, or an empty optional if no argument was found
	 */
	public <T> Optional<T> getOptionalByArgument(AbstractArgument<T, ?, ?, ?> argumentType) {
		return Optional.ofNullable(getByArgumentOrDefault(argumentType, null));
	}

	/**
	 * Returns an argument by its node name and type
	 *
	 * @param nodeName The node name of the argument
	 * @param argumentType The expected type of the argument
	 * @return The argument with given node name
	 */
	public <T> T getByClass(String nodeName, Class<T> argumentType) {
		return castArgument(get(nodeName), argumentType, nodeName);
	}

	/**
	 * Returns an argument by its node name and type
	 *
	 * @param nodeName The node name of the argument
	 * @param argumentType The expected type of the argument
	 * @param defaultValue The value returned when no argument is found for the node name
	 * @return The argument with given node name, or the default value
	 */
	@Contract("_, _, !null -> !null")
	public <T> @Nullable T getByClassOrDefault(String nodeName, Class<T> argumentType, @Nullable T defaultValue) {
		return castArgument(getOrDefault(nodeName, defaultValue), argumentType, nodeName);
	}

	/**
	 * Returns an {@link Optional} holding the argument by its node name and type
	 *
	 * @param nodeName The node name of the argument
	 * @param argumentType The expected type of the argument
	 * @return An optional holding the argument with the given node name, or an empty optional if no argument was found
	 */
	public <T> Optional<T> getOptionalByClass(String nodeName, Class<T> argumentType) {
		return Optional.ofNullable(getByClassOrDefault(nodeName, argumentType, null));
	}

	/**
	 * Returns an argument by its index and type
	 *
	 * @param index The index of the argument
	 * @param argumentType The expected type of the argument
	 * @return The argument at the given index
	 */
	public <T> T getByClass(int index, Class<T> argumentType) {
		return castArgument(get(index), argumentType, index);
	}

	/**
	 * Returns an argument by its index and type
	 *
	 * @param index The index of the argument
	 * @param argumentType The expected type of the argument
	 * @param defaultValue The value returned when no argument is found for the index
	 * @return The argument at the given index, or the default value
	 */
	@Contract("_, _, !null -> !null")
	public <T> @Nullable T getByClassOrDefault(int index, Class<T> argumentType, @Nullable T defaultValue) {
		return castArgument(getOrDefault(index, defaultValue), argumentType, index);
	}

	/**
	 * Returns an {@link Optional} holding the argument by its index and type
	 *
	 * @param index The index of the argument
	 * @param argumentType The expected type of the argument
	 * @return An optional holding the argument at the given index, or an empty optional if no argument was found
	 */
	public <T> Optional<T> getOptionalByClass(int index, Class<T> argumentType) {
		return Optional.ofNullable(getByClassOrDefault(index, argumentType, null));
	}

	@Contract("!null, _, _ -> !null")
	private <T> @Nullable T castArgument(@Nullable Object argument, Class<T> argumentType, Object argumentNameOrIndex) {
		if (argument == null) {
			return null;
		}
		if (!PRIMITIVE_TO_WRAPPER.getOrDefault(argumentType, argumentType).isAssignableFrom(argument.getClass())) {
			throw new IllegalArgumentException(buildExceptionMessage(argumentNameOrIndex, argument.getClass().getSimpleName(), argumentType.getSimpleName()));
		}
		return (T) argument;
	}

	private String buildExceptionMessage(Object argumentNameOrIndex, String expectedClass, String actualClass) {
		if (argumentNameOrIndex instanceof Integer i) {
			return "Argument at index '" + i + "' is defined as " + expectedClass + ", not " + actualClass;
		}
		if (argumentNameOrIndex instanceof String s) {
			return "Argument '" + s + "' is defined as " + expectedClass + ", not " + actualClass;
		}
		throw new IllegalStateException("Unexpected behaviour detected while building exception message!" +
			"This should never happen - if you're seeing this message, please" +
			"contact the developers of the CommandAPI, we'd love to know how you managed to get this error!");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(args);
		result = prime * result + Arrays.hashCode(rawArgs);
		result = prime * result + Objects.hash(argsMap, fullInput, rawArgsMap);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommandArguments other = (CommandArguments) obj;
		return Arrays.deepEquals(args, other.args) && Objects.equals(argsMap, other.argsMap)
				&& Objects.equals(fullInput, other.fullInput) && Arrays.equals(rawArgs, other.rawArgs)
				&& Objects.equals(rawArgsMap, other.rawArgsMap);
	}

}
