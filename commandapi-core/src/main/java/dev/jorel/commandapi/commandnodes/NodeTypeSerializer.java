package dev.jorel.commandapi.commandnodes;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.jorel.commandapi.CommandAPIHandler;

import java.util.HashMap;
import java.util.Map;

@FunctionalInterface
// Java generics with class literals are annoying, so I can't find a way to parameterize CommandNode here
public interface NodeTypeSerializer<T extends CommandNode> {
	// Interface for serializing CommandNode subclasses
	void extractTypeInformation(JsonObject target, T type);

	// Serializer registry
	Map<Class<? extends CommandNode<?>>, NodeTypeSerializer<?>> nodeTypeSerializers = new HashMap<>();

	static <T extends CommandNode<?>> void registerSerializer(Class<T> clazz, NodeTypeSerializer<T> serializer) {
		nodeTypeSerializers.put(clazz, serializer);
	}

	// Use serializers
	NodeTypeSerializer<?> UNKNOWN = (target, type) -> {
		target.addProperty("type", "unknown");
		target.addProperty("typeClassName", type.getClass().getName());
	};

	static <T extends CommandNode<?>> NodeTypeSerializer<T> getSerializer(T type) {
		initialize();
		// Try to find the serializer for the most specific parent class of this node
		Class<?> clazz = type.getClass();
		while (!CommandNode.class.equals(clazz)) {
			NodeTypeSerializer<T> serializer = (NodeTypeSerializer<T>) nodeTypeSerializers.get(clazz);
			if (serializer != null) return serializer;

			clazz = clazz.getSuperclass();
		}
		return (NodeTypeSerializer<T>) UNKNOWN;
	}

	static <T extends CommandNode<?>> void addTypeInformation(JsonObject target, T type) {
		getSerializer(type).extractTypeInformation(target, type);
	}

	// Initialize registry - for some reason interfaces can't have static initializers?
	static void initialize() {
		if (nodeTypeSerializers.containsKey(RootCommandNode.class)) return;

		// BRIGADIER CommandNodes
		registerSerializer(RootCommandNode.class, (target, type) -> target.addProperty("type", "root"));
		registerSerializer(LiteralCommandNode.class, (target, type) -> target.addProperty("type", "literal"));

		registerSerializer(ArgumentCommandNode.class, ((target, type) -> {
			ArgumentType<?> argumentType = type.getType();

			target.addProperty("type", "argument");
			target.addProperty("argumentType", argumentType.getClass().getName());

			CommandAPIHandler.getInstance().getPlatform()
				.getArgumentTypeProperties(argumentType).ifPresent(properties -> target.add("properties", properties));
		}));
	}
}
