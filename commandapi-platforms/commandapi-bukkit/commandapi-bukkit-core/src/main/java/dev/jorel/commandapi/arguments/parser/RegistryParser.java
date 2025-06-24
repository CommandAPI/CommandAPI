package dev.jorel.commandapi.arguments.parser;

import dev.jorel.commandapi.arguments.parser.function.ThrowingSupplyingParser;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffectType;

public record RegistryParser<T>(
	ThrowingSupplyingParser<T> type,
	ThrowingSupplyingParser<NamespacedKey> key
) {
}
