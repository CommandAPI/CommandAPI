package dev.jorel.commandapi.arguments.serializer;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;

import java.util.Objects;
import java.util.function.Function;

public class NumberArgumentTypeSerializer<N extends Number, T extends ArgumentType<N>> implements ArgumentTypeSerializer.Properties<T> {
	private final N absoluteMinimum;
	private final N absoluteMaximum;

	private final Function<T, N> getMinimum;
	private final Function<T, N> getMaximum;

	public NumberArgumentTypeSerializer(
		N absoluteMinimum, N absoluteMaximum,
		Function<T, N> getMinimum, Function<T, N> getMaximum
	) {
		this.absoluteMinimum = absoluteMinimum;
		this.absoluteMaximum = absoluteMaximum;

		this.getMinimum = getMinimum;
		this.getMaximum = getMaximum;
	}

	@Override
	public void fillJson(JsonObject result, T type) {
		N minimum = getMinimum.apply(type);
		if (Objects.equals(minimum, absoluteMinimum)) {
			result.addProperty("min", "absolute");
		} else {
			result.addProperty("min", minimum);
		}

		N maximum = getMaximum.apply(type);
		if (Objects.equals(maximum, absoluteMaximum)) {
			result.addProperty("max", "absolute");
		} else {
			result.addProperty("max", maximum);
		}
	}
}
