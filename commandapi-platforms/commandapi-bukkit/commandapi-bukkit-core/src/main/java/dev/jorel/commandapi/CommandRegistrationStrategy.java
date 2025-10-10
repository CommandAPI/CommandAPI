package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.jorel.commandapi.preprocessor.RequireField;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@RequireField(in = CommandNode.class, name = "children", ofType = Map.class)
@RequireField(in = CommandNode.class, name = "literals", ofType = Map.class)
@RequireField(in = CommandNode.class, name = "arguments", ofType = Map.class)
public abstract class CommandRegistrationStrategy<Source> {
	// Reflection
	// I'd like to make the Maps here `Map<String, CommandNode<Source>>`, but these static fields cannot use the type
	//  parameter Source. We still need to cast to that signature for map, so Map is raw.
	static final SafeVarHandle<CommandNode<?>, Map> commandNodeChildren;
	private static final SafeVarHandle<CommandNode<?>, Map> commandNodeLiterals;
	private static final SafeVarHandle<CommandNode<?>, Map> commandNodeArguments;

	// Compute all var handles all in one go so we don't do this during main server runtime
	static {
		commandNodeChildren = SafeVarHandle.ofOrNull(CommandNode.class, "children", "children", Map.class);
		commandNodeLiterals = SafeVarHandle.ofOrNull(CommandNode.class, "literals", "literals", Map.class);
		commandNodeArguments = SafeVarHandle.ofOrNull(CommandNode.class, "arguments", "arguments", Map.class);
	}

	// Utility methods
	protected void removeBrigadierCommands(RootCommandNode<Source> root, String commandName,
										   boolean unregisterNamespaces, Predicate<CommandNode<Source>> extraCheck) {
		Map<String, CommandNode<Source>> children = (Map<String, CommandNode<Source>>) commandNodeChildren.get(root);
		Map<String, CommandNode<Source>> literals = (Map<String, CommandNode<Source>>) commandNodeLiterals.get(root);
		Map<String, CommandNode<Source>> arguments = (Map<String, CommandNode<Source>>) commandNodeArguments.get(root);

		removeCommandFromMapIfCheckPasses(children, commandName, extraCheck);
		removeCommandFromMapIfCheckPasses(literals, commandName, extraCheck);
		// Commands should really only be represented as literals, but it is technically possible
		// to put an ArgumentCommandNode in the root, so we'll check
		removeCommandFromMapIfCheckPasses(arguments, commandName, extraCheck);

		if (unregisterNamespaces) {
			removeCommandNamespace(children, commandName, extraCheck);
			removeCommandNamespace(literals, commandName, extraCheck);
			removeCommandNamespace(arguments, commandName, extraCheck);
		}
	}

	protected static <T> void removeCommandNamespace(Map<String, T> map, String commandName, Predicate<T> extraCheck) {
		for (String key : new HashSet<>(map.keySet())) {
			if (!isThisTheCommandButNamespaced(commandName, key)) continue;

			removeCommandFromMapIfCheckPasses(map, key, extraCheck);
		}
	}

	protected static <T> void removeCommandFromMapIfCheckPasses(Map<String, T> map, String key, Predicate<T> extraCheck) {
		T element = map.get(key);
		if (element == null) return;
		if (extraCheck.test(element)) map.remove(key);
	}

	protected static boolean isThisTheCommandButNamespaced(String commandName, String key) {
		if (!key.contains(":")) return false;
		String[] split = key.split(":");
		if (split.length < 2) return false;
		return split[1].equalsIgnoreCase(commandName);
	}

	// Behavior methods
	public abstract CommandDispatcher<Source> getBrigadierDispatcher();

	public void runTasksAfterServerStart() {}

	public void postCommandRegistration(RegisteredCommand registeredCommand, LiteralCommandNode<Source> resultantNode, List<LiteralCommandNode<Source>> aliasNodes) {}

	public abstract LiteralCommandNode<Source> registerCommandNode(LiteralArgumentBuilder<Source> node, String namespace);

	public abstract void unregister(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit);

	public abstract void preReloadDataPacks();
}
