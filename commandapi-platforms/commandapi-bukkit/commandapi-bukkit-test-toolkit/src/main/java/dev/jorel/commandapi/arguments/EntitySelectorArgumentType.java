package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jorel.commandapi.MockCommandSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record EntitySelectorArgumentType(boolean singleTarget, boolean playersOnly) implements ArgumentType<EntitySelector> {
	// Internal state necessary
	public static EntitySelectorArgumentType entities() {
		return new EntitySelectorArgumentType(false, false);
	}

	public static EntitySelectorArgumentType players() {
		return new EntitySelectorArgumentType(false, true);
	}

	public static EntitySelectorArgumentType entity() {
		return new EntitySelectorArgumentType(true, false);
	}

	public static EntitySelectorArgumentType player() {
		return new EntitySelectorArgumentType(true, true);
	}

	// ArgumentType implementation
	public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_ENTITY = new SimpleCommandExceptionType(
		ArgumentUtilities.translatedMessage("argument.entity.toomany")
	);
	public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_PLAYER = new SimpleCommandExceptionType(
		ArgumentUtilities.translatedMessage("argument.player.toomany")
	);
	public static final SimpleCommandExceptionType ERROR_ONLY_PLAYERS_ALLOWED = new SimpleCommandExceptionType(
		ArgumentUtilities.translatedMessage("argument.player.entities")
	);
	public static final SimpleCommandExceptionType NO_ENTITIES_FOUND = new SimpleCommandExceptionType(
		ArgumentUtilities.translatedMessage("argument.entity.notfound.entity")
	);
	public static final SimpleCommandExceptionType NO_PLAYERS_FOUND = new SimpleCommandExceptionType(
		ArgumentUtilities.translatedMessage("argument.entity.notfound.player")
	);

	@Override
	public EntitySelector parse(StringReader reader) throws CommandSyntaxException {
		EntitySelector entityselector = EntitySelectorParser.parser.parse(reader);
		// I don't know why Minecraft does `reader.setCursor(0)` here before throwing exceptions, but it does ¯\_(ツ)_/¯
		//  That has the goofy result of underlining the whole command when it should really only underline the selector
		//  This is easily fixed, just store `reader.getCursor()` before parsing the selector
		if (entityselector.maxResults() > 1 && this.singleTarget) {
			if (this.playersOnly) {
				reader.setCursor(0);
				throw ERROR_NOT_SINGLE_PLAYER.createWithContext(reader);
			} else {
				reader.setCursor(0);
				throw ERROR_NOT_SINGLE_ENTITY.createWithContext(reader);
			}
		} else if (entityselector.includesEntities() && this.playersOnly && !entityselector.selfSelector()) {
			reader.setCursor(0);
			throw ERROR_ONLY_PLAYERS_ALLOWED.createWithContext(reader);
		} else {
			return entityselector;
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return EntitySelectorParser.parser.listSuggestions(context, builder);
	}

	public static List<? extends Entity> findManyEntities(CommandContext<MockCommandSource> cmdCtx, String key, boolean allowEmpty) throws CommandSyntaxException {
		EntitySelector selector = cmdCtx.getArgument(key, EntitySelector.class);
		List<? extends Entity> entities = selector.findEntities(cmdCtx.getSource());
		if (entities.isEmpty() && !allowEmpty) {
			throw NO_ENTITIES_FOUND.create();
		}
		return entities;
	}

	public static List<Player> findManyPlayers(CommandContext<MockCommandSource> cmdCtx, String key, boolean allowEmpty) throws CommandSyntaxException {
		EntitySelector selector = cmdCtx.getArgument(key, EntitySelector.class);
		List<Player> players = selector.findPlayers(cmdCtx.getSource());
		if (players.isEmpty() && !allowEmpty) {
			throw NO_PLAYERS_FOUND.create();
		}
		return players;
	}

	// Funnily, the logic for finding single entity vs player is different, at least on 1.21 ¯\_(ツ)_/¯
	public static Entity findSingleEntity(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		List<? extends Entity> entities = findManyEntities(cmdCtx, key, false);
		if (entities.size() > 1) {
			throw ERROR_NOT_SINGLE_ENTITY.create();
		}
		return entities.get(0);
	}

	public static Player findSinglePlayer(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		List<Player> players = findManyPlayers(cmdCtx, key, true);
		if (players.size() != 1) {
			throw NO_PLAYERS_FOUND.create();
		}
		return players.get(0);
	}
}
