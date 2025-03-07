package dev.jorel.commandapi.arguments.serializer;


import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;



@FunctionalInterface
public interface ArgumentTypeSerializer<T extends ArgumentType<?>> {
	// Interface for serializing ArgumentType
	Optional<JsonObject> extractProperties(T type);

	// Serializer registry
	Map<Class<? extends ArgumentType<?>>, ArgumentTypeSerializer<?>> argumentTypeSerializers = new HashMap<>();

	static <T extends ArgumentType<?>> void registerSerializer(Class<T> clazz, ArgumentTypeSerializer<T> serializer) {
		argumentTypeSerializers.put(clazz, serializer);
	}

	@FunctionalInterface
	interface Properties<T extends ArgumentType<?>> extends ArgumentTypeSerializer<T> {
		void fillJson(JsonObject result, T type);

		@Override
		default Optional<JsonObject> extractProperties(T type) {
			JsonObject result = new JsonObject();
			fillJson(result, type);
			return Optional.of(result);
		}
	}

	static <T extends ArgumentType<?>> void registerPropertiesSerializer(Class<T> clazz, Properties<T> serializer) {
		registerSerializer(clazz, serializer);
	}

	ArgumentTypeSerializer<?> NO_PROPERTIES = type -> Optional.empty();

	static <T extends ArgumentType<?>> void registerEmptySerializer(Class<T> clazz) {
		registerSerializer(clazz, (ArgumentTypeSerializer<T>) NO_PROPERTIES);
	}

	// Use serializers
	Properties<?> UNKNOWN = (result, type) -> result.addProperty("unknown", type.toString());

	static <T extends ArgumentType<?>> ArgumentTypeSerializer<T> getSerializer(T type) {
		initialize();
		return (ArgumentTypeSerializer<T>) argumentTypeSerializers.getOrDefault(type.getClass(), UNKNOWN);
	}

	static <T extends ArgumentType<?>> Optional<JsonObject> getProperties(T type) {
		return getSerializer(type).extractProperties(type);
	}

	// Initialize registry - for some reason interfaces can't have static initializers?
	static void initialize() {
		if (argumentTypeSerializers.containsKey(BoolArgumentType.class)) return;

		// BRIGADIER ARGUMENTS
		registerEmptySerializer(BoolArgumentType.class);

		registerPropertiesSerializer(DoubleArgumentType.class, new NumberArgumentTypeSerializer<>(
			-1.7976931348623157E308, Double.MAX_VALUE,
			DoubleArgumentType::getMinimum, DoubleArgumentType::getMaximum
		));
		registerPropertiesSerializer(FloatArgumentType.class, new NumberArgumentTypeSerializer<>(
			-3.4028235E38F, Float.MAX_VALUE,
			FloatArgumentType::getMinimum, FloatArgumentType::getMaximum
		));
		registerPropertiesSerializer(IntegerArgumentType.class, new NumberArgumentTypeSerializer<>(
			Integer.MIN_VALUE, Integer.MAX_VALUE,
			IntegerArgumentType::getMinimum, IntegerArgumentType::getMaximum
		));
		registerPropertiesSerializer(LongArgumentType.class, new NumberArgumentTypeSerializer<>(
			Long.MIN_VALUE, Long.MAX_VALUE,
			LongArgumentType::getMinimum, LongArgumentType::getMaximum
		));

		registerPropertiesSerializer(StringArgumentType.class, (result, type) ->
			result.addProperty("type", switch (type.getType()) {
				case SINGLE_WORD -> "unquoted";
				case QUOTABLE_PHRASE -> "quotable";
				case GREEDY_PHRASE -> "greedy";
			})
		);
	}
}
