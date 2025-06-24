package dev.jorel.commandapi.arguments.parser;

import dev.jorel.commandapi.arguments.parser.function.ThrowingFunctionParser;
import dev.jorel.commandapi.arguments.parser.function.ThrowingSupplyingParser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

public record EntitySelectorParser(
	ThrowingSupplyingParser<Player> player,
	ThrowingSupplyingParser<Entity> entity,
	ThrowingFunctionParser<Boolean, Collection<Player>> players,
	ThrowingFunctionParser<Boolean, Collection<Entity>> entities
) {
}
