package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jorel.commandapi.MockCommandSource;
import dev.jorel.commandapi.arguments.parser.Parser;
import dev.jorel.commandapi.arguments.parser.ParserArgument;
import dev.jorel.commandapi.arguments.parser.Result;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProfileArgumentType implements ArgumentType<ProfileArgumentType.ProfileSelector> {
	// No internal state is necessary
	public static final ProfileArgumentType INSTANCE = new ProfileArgumentType();

	private ProfileArgumentType() {

	}

	// ArgumentType implementation
	@FunctionalInterface
	public interface ProfileSelector {
		List<PlayerProfile> getProfiles(MockCommandSource source) throws CommandSyntaxException;
	}

	public static final SimpleCommandExceptionType ERROR_UNKNOWN_PLAYER = new SimpleCommandExceptionType(
		ArgumentUtilities.translatedMessage("argument.player.unknown")
	);

	private static final ParserArgument<String> readName = ArgumentUtilities.readUntilWithoutEscapeCharacter(' ');

	public static final Parser<ProfileSelector> parser = reader -> {
		if (reader.canRead() && reader.peek() == '@') {
			// Looks like reading an entity selector
			return EntitySelectorParser.parser.getResult(reader).continueWith(
				// successfully read an entity selector, adapt it to our profile selector
				Result.wrapFunctionResult(entitySelector -> {
					if (entitySelector.includesEntities()) {
						throw EntitySelectorArgumentType.ERROR_ONLY_PLAYERS_ALLOWED.create();
					}
					return (ProfileSelector) source -> {
						List<Player> players = entitySelector.findPlayers(source);
						if (players.isEmpty()) {
							throw EntitySelectorArgumentType.NO_PLAYERS_FOUND.create();
						}

						List<PlayerProfile> profiles = new ArrayList<>(players.size());
						for (Player player : players) {
							profiles.add(player.getPlayerProfile());
						}
						return profiles;
					};
				})
				// entity selector could not be parsed, pass error unchanged
			);
		}

		// Looks like reading a name
		return readName.getResult(reader).continueWith(
			// Successfully read name, convert to profile selector
			Result.wrapFunctionResult(name -> source -> {
				// TODO: I'm not sure if or how this should check if offline player profiles exist
				Player player = Bukkit.getPlayerExact(name);
				if (player == null) {
					throw ERROR_UNKNOWN_PLAYER.create();
				}
				return List.of(player.getPlayerProfile());
			})
			// Name was not parsed, pass error unchanged
		);
	};

	@Override
	public ProfileSelector parse(StringReader reader) throws CommandSyntaxException {
		return parser.parse(reader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return EntitySelectorParser.parser.listSuggestions(context, builder);
	}

	public static List<? extends PlayerProfile> getProfiles(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		return cmdCtx.getArgument(key, ProfileSelector.class).getProfiles(cmdCtx.getSource());
	}
}
