package dev.jorel.commandapi;

import dev.jorel.commandapi.arguments.AbstractArgument;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Class to store a registered command which has its command name and a list of
 * arguments as a string. The arguments are expected to be of the form
 * {@code node_name:class_name}, for example {@code value:IntegerArgument}. This
 * class also contains the information required to construct a meaningful help
 * topic for a command
 */
public record RegisteredCommand(

	/**
	 * @return The name of this command, without any leading {@code /} characters
	 */
	String commandName,

	/**
	 * @return The list of node names and argument class simple names in the form
	 *         {@code node_name:class_name}, for example
	 *         {@code value:}{@link IntegerArgument}
	 */
	List<String> argsAsStr,

	/**
	 * @return An unmodifiable list of arguments for this command
	 */
	List<AbstractArgument<?, ?, ?, ?>> arguments,

	/**
	 * @return An {@link Optional} containing this command's help's short
	 *         descriptions
	 */
	Optional<String> shortDescription,

	/**
	 * @return An {@link Optional} containing this command's help's full
	 *         descriptions
	 */
	Optional<String> fullDescription,

	/**
	 * @return An {@link Optional} containing this command's help's
	 *          usage
	 */
	Optional<String[]> usageDescription,
	
	/**
	 * @return An {@link Optional} containing this command's help topic (for Bukkit)
	 */
	Optional<Object> helpTopic,

	/**
	 * @return a {@link String}{@code []} of aliases for this command
	 */
	String[] aliases,

	/**
	 * @return The {@link CommandPermission} required to run this command
	 */
	CommandPermission permission,

	/**
	 * @return The namespace for this command
	 */
	String namespace) {

	public boolean shouldGenerateHelpTopic() {
		return fullDescription.isPresent() || usageDescription.isPresent() || helpTopic.isPresent();
	}

	// As https://stackoverflow.com/a/32083420 mentions, Optional's hashCode, equals, and toString method don't work if the
	//  Optional wraps an array, like `Optional<String[]> usageDescription`, so we have to use the Arrays methods ourselves

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(aliases);
		result = prime * result + Arrays.hashCode(usageDescription.orElse(null));
		result = prime * result + Objects.hash(argsAsStr, commandName, fullDescription, permission, shortDescription, helpTopic, namespace);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RegisteredCommand)) {
			return false;
		}
		RegisteredCommand other = (RegisteredCommand) obj;
		return Arrays.equals(aliases, other.aliases) && Objects.equals(argsAsStr, other.argsAsStr) && Objects.equals(commandName, other.commandName)
			&& Objects.equals(namespace, other.namespace) && Arrays.equals(usageDescription.orElse(null), other.usageDescription.orElse(null))
			&& Objects.equals(fullDescription, other.fullDescription) && Objects.equals(permission, other.permission) && Objects.equals(shortDescription, other.shortDescription)
			&& Objects.equals(helpTopic, other.helpTopic);
	}

	@Override
	public String toString() {
		return "RegisteredCommand [commandName=" + commandName + ", argsAsStr=" + argsAsStr + ", shortDescription=" + shortDescription + ", fullDescription=" + fullDescription
			+ ", usageDescription=" + (usageDescription.isPresent() ? "Optional[" + Arrays.toString(usageDescription.get()) + "]" : "Optional.empty")
			+ ", aliases=" + Arrays.toString(aliases) + ", permission=" + permission + ", namespace=" + namespace + ", helpTopic=" + helpTopic + "]";
	}

}