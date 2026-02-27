package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An interface declaring methods required to override argument suggestions
 *
 * @param <T> The type of the underlying object that this argument casts to
 * @param <S> A custom type which is represented by this argument. For example,
 *            a {@link StringArgument} will have a custom type
 *            <code>String</code>
 */
public abstract class SafeOverrideableArgument<T, S> extends Argument<T> implements SafeOverrideable<T, S, Argument<T>, Argument<?>, CommandSender> {
	private final Function<S, String> mapper;

	/**
	 * Instantiates this argument and assigns the mapper to the provided mapper
	 *
	 * @param nodeName the node name of this argument
	 * @param rawType  the NMS raw argument type of this argument
	 * @param mapper   the mapping function that maps this argument type to a string
	 *                 for suggestions
	 */
	protected SafeOverrideableArgument(String nodeName, Supplier<ArgumentType<?>> rawType, Function<S, String> mapper) {
		super(nodeName, rawType);
		this.mapper = mapper;
	}

	@Override
	public Function<S, String> getMapper() {
		return mapper;
	}

	/**
	 * <p>
	 * Composes a <code>S</code> to a <code>NamespacedKey</code> mapping function to
	 * convert <code>S</code> to a <code>String</code>
	 *
	 * @param mapper the mapping function from <code>S</code> to
	 *               <code>NamespacedKey</code>
	 * @return a composed function that converts <code>S</code> to
	 * <code>String</code>
	 */
	public static <S> Function<S, String> fromKey(Function<S, NamespacedKey> mapper) {
		return mapper.andThen(NamespacedKey::toString);
	}
}
