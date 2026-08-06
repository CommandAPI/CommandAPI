package io.github.jorelali.delayedapicommand;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// This keeps track of the delay for each player that uses it
public class PerPlayerDelayedCommandAPICommand extends PlayerDelayedCommandAPICommand {
	// Use UUID here in case player leaves and rejoins server to get around delay
	private final Map<UUID, Long> nextTimesPerPlayer = new HashMap<>();

	public PerPlayerDelayedCommandAPICommand(String commandName, long time, TimeUnit timeUnit) {
		super(commandName, time, timeUnit);
	}

	@Override
	void throwExceptionIfCannotRun(Player player) throws WrapperCommandSyntaxException {
		// Get the next time when this player is allowed to run the command
		//  The default time is 0, which is always in the past, so the command will always be run the first time
		long nextTime = nextTimesPerPlayer.getOrDefault(player.getUniqueId(), 0L);
		long currentTime = System.currentTimeMillis();

		// If it isn't time to run the command yet, throw the exception
		if(currentTime < nextTime) {
			throw CommandAPI.failWithString(
				"You must wait "
					+ getDurationString(nextTime - currentTime)
					+ " before running this command again"
			);
		}

		// If the command is run, set the next possible time
		nextTimesPerPlayer.put(player.getUniqueId(), currentTime + delay);
	}
}
