package dev.jorel.commandapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import dev.jorel.commandapi.help.CommandAPIHelpTopic;

/**
 * Class to store a registered command which has its command name and a tree representing its arguments.
 * This class also contains the information required to construct a meaningful help topic for a command.
 *
 * @param commandName     The name of this command, without any leading {@code /} characters
 * @param aliases         A {@link String}{@code []} of aliases for this command
 * @param namespace       The namespace for this command
 * @param helpTopic       The {@link CommandAPIHelpTopic} that stores the help information for this command
 * @param rootNode        The root {@link Node} in the tree structure that holds the arguments of this command
 * @param <CommandSender> The class for running platform commands
 */
public record RegisteredCommand<CommandSender>(

	/**
	 * @return The name of this command, without any leading {@code /} characters
	 */
	String commandName,

	/**
	 * @return A {@link String}{@code []} of aliases for this command
	 */
	String[] aliases,

	/**
	 * @return The namespace for this command
	 */
	String namespace,

	/**
	 * @return The {@link CommandAPIHelpTopic} that stores the help information for this command
	 */
	CommandAPIHelpTopic<CommandSender> helpTopic,

	/**
	 * @return The root {@link Node} in the tree structure that holds the arguments of this command
	 */
	Node<CommandSender> rootNode) {

	/**
	 * Class to store information about each argument in a command's tree.
	 * 
	 * @param nodeName        The name of this argument node
	 * @param className       The name of the CommandAPI object that this node represents
	 * @param helpString      The string that should be used to represent this node when automatically generating help usage.
	 * @param permission      The {@link CommandPermission} object that determines if someone can see this node
	 * @param requirements    An arbitrary additional check to perform to determine if someone can see this node
	 * @param executable      True if this node can be executed, and false otherwise
	 * @param children        A {@link List} of nodes that are children to this node
	 * @param <CommandSender> The class for running platform commands
	 */
	public record Node<CommandSender>(
		/**
		 * @return The name of this argument node
		 */
		String nodeName,

		/**
		 * @return The name of the CommandAPI object that this node represents
		 */
		String className, 

		/**
		 * @return The string that should be used to represent this node when automatically generating help usage
		 */
		String helpString, 

		/**
		 * @return True if this node can be executed, and false otherwise
		 */
		boolean executable,

		/**
		 * @return The {@link CommandPermission} object that determines if someone can see this node
		 */
		CommandPermission permission,

		/**
		 * @return An arbitrary additional check to perform to determine if someone can see this node
		 */
		Predicate<CommandSender> requirements,

		/**
		 * @return A {@link List} of nodes that are children to this node
		 */
		List<Node<CommandSender>> children) {

		/**
		 * @return A {@link List} of each executable path starting at this node. Each path is represented as a {@link List} of
		 * strings, where each string represents a node with the form {@code node_name:class_name}, for example {@code value:IntegerArgument}.
		 */
		public List<List<String>> argsAsStr() {
			List<List<String>> paths = new ArrayList<>();

			// Add this node
			String nodeString = nodeName + ":" + className;
			if (executable) {
				// This should be an ArrayList so parent nodes can insert themselves at the start
				paths.add(new ArrayList<>(List.of(nodeString)));
			}

			// Add children nodes
			for (Node<CommandSender> node : children) {
				List<List<String>> subPaths = node.argsAsStr();

				for (List<String> subPath : subPaths) {
					subPath.add(0, nodeString);
				}

				paths.addAll(subPaths);
			}

			return paths;
		}

		private Node<CommandSender> merge(Node<CommandSender> other) {
			// Merge executable status
			boolean mergeExecutable = this.executable || other.executable;

			// Merge children
			Map<String, Node<CommandSender>> childrenByName = new HashMap<>();
			for (Node<CommandSender> child : this.children) {
				childrenByName.put(child.nodeName, child);
			}
			for (Node<CommandSender> child : other.children) {
				childrenByName.compute(child.nodeName, (key, value) -> value == null ? child : value.merge(child));
			}
			List<Node<CommandSender>> mergeChildren = new ArrayList<>(childrenByName.values());

			// Other information defaults to the node that was registered first (this)
			return new Node<>(nodeName, className, helpString, mergeExecutable, permission, requirements, mergeChildren);
		}

		// There is no good way to check equality between Predicates, it's just a strict `==`
		//  However, record's impelementation of `.equals` tries to include `Predicate<CommandSender> requirement` anyway
		//  So, overide equals to ignore that (and update hashCode so it matches)
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Node)) {
				return false;
			}
			Node<CommandSender> other = (Node<CommandSender>) obj;
			return Objects.equals(nodeName, other.nodeName) && Objects.equals(className, other.className) && Objects.equals(helpString, other.helpString)
				&& Objects.equals(executable, other.executable) && Objects.equals(permission, other.permission)
				&& Objects.equals(children, other.children);
		}

		@Override
		public int hashCode() {
			return Objects.hash(nodeName, className, helpString, executable, permission, children);
		}
	}

	/**
	 * Merges the given command into this command, returning the result. The result should accurately represent
	 * the command that would result from registering the commands in order. For example, registering two commands
	 * with different permissions results in the first command's permission being used, and so the result of this
	 * method will have the first command's permission.
	 * 
	 * @param other The {@link RegisteredCommand} that was registered after this one.
	 * @return The {@link RegisteredCommand} that results from merging this and the other command.
	 */
	public RegisteredCommand<CommandSender> mergeCommandInformation(RegisteredCommand<CommandSender> other) {
		// Merge aliases
		String[] mergedAliases = new String[this.aliases.length + other.aliases.length];
		System.arraycopy(this.aliases, 0, mergedAliases, 0, this.aliases.length);
		System.arraycopy(other.aliases, 0, mergedAliases, this.aliases.length, other.aliases.length);

		// Merge arguments
		Node<CommandSender> mergedRootNode = this.rootNode.merge(other.rootNode);

		// Other information defaults to the command that was registered first (this)
		return new RegisteredCommand<>(commandName, mergedAliases, namespace, helpTopic, mergedRootNode);
	}

	/**
	 * @return A copy of this {@link RegisteredCommand}, but with {@link RegisteredCommand#namespace()} as {@code ""}.
	 */
	public RegisteredCommand<CommandSender> copyWithEmptyNamespace() {
		return new RegisteredCommand<>(commandName, aliases, "", helpTopic, rootNode);
	}

	// The default implementation of `hashCode`, `equals`, and `toString` for records don't work for arrays,
	//  so we need to override and specifically use the Arrays methods for `String[] aliases`

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(aliases);
		result = prime * result + Objects.hash(commandName, namespace, helpTopic, rootNode);
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
		RegisteredCommand<CommandSender> other = (RegisteredCommand<CommandSender>) obj;
		return Objects.equals(commandName, other.commandName) && Arrays.equals(aliases, other.aliases) && Objects.equals(namespace, other.namespace)
			&& Objects.equals(helpTopic, other.helpTopic) && Objects.equals(rootNode, other.rootNode);
	}

	@Override
	public String toString() {
		return "RegisteredCommand [commandName=" + commandName + ", aliases=" + Arrays.toString(aliases) + ", namespace=" + namespace
			+ ", helpTopic=" + helpTopic + ", rootNode=" + rootNode + "]";
	}
}
