package io.github.jorelali.delayedapicommand;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

// This shares its delay for all players
public class GlobalPlayerDelayedCommandAPICommand extends PlayerDelayedCommandAPICommand {
	// The next time when this command will be allowed to run
	//  The default time is 0, which is always in the past, so the command will always be run the first time
	private long nextTime = 0;

	public GlobalPlayerDelayedCommandAPICommand(String commandName, long time, TimeUnit timeUnit) {
		super(commandName, time, timeUnit);
	}

	@Override
	void throwExceptionIfCannotRun(Player player) throws WrapperCommandSyntaxException {
		// We don't have to worry about this overflowing for about 290 million years
		// https://stackoverflow.com/questions/2978452/when-will-system-currenttimemillis-overflow
		// This code will reward your patience by letting you run the command without waiting
		long currentTime = System.currentTimeMillis();

		// If it isn't time to run the command yet, throw the exception
		if(currentTime < nextTime) {
			throw CommandAPI.failWithString(
				"This command cannot be run for another "
					+ getDurationString(nextTime - currentTime)
			);
		}

		// If the command is run, set the next possible time
		nextTime = currentTime + delay;
	}
}
