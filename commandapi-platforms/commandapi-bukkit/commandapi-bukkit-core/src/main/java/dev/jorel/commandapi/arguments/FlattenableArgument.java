package dev.jorel.commandapi.arguments;

import java.util.List;

/**
 * An interface indicating that the raw input for an argument can be flattened into a collection of Strings.
 * This is intended to be used for entity selectors in Converted commands.
 */
public interface FlattenableArgument {
	/**
	 * @param parsedInput The original CommandAPI parse result for this argument
	 * @return The flattened version of the argument input
	 */
	List<String> flatten(Object parsedInput);
}
