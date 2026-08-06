package io.github.jorelali.delayedapicommand;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.*;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

// This class extends CommandAPICommand to implement delayed commands
public abstract class PlayerDelayedCommandAPICommand extends CommandAPICommand {
	protected final long delay;

	public PlayerDelayedCommandAPICommand(String commandName, long time, TimeUnit timeUnit) {
		super(commandName);
		// Delay is the value in milliseconds
		delay = timeUnit.toMillis(time);
	}

	// This method is implemented by the child classes
	// If the command is currently delayed for the player, an exception should be thrown
	abstract void throwExceptionIfCannotRun(Player player) throws WrapperCommandSyntaxException;

	// This helper method formats a millisecond duration into a String representing how long is left in the delay
	static String getDurationString(long millis) {
		Duration duration = Duration.ofMillis(millis);

		long days = duration.toDays();
		long hours = duration.toHours() % 24;
		long minutes = duration.toMinutes() % 60;
		long seconds = duration.getSeconds() % 60;

		String durationString;
		if(days != 0) durationString = days + ":" + hours + ":" + minutes + " days";
		else if (hours != 0) durationString = hours + ":" + minutes + ":" + seconds + "hours";
		else if (minutes != 0) durationString = minutes + ":" + seconds + "minutes";
		else if (seconds != 1) durationString = seconds + " seconds";
		else  durationString = "1 second";

		return durationString;
	}

	// Override the usual executes methods to replace the executor with a delayed executor
	// They take one CommandAPI executor and uses it inside a new executor
	// The new executor also uses the throwExceptionIfCannotRun method to stop
	//  command execution if the command's delay is currently in effect
	@Override
	public CommandAPICommand executesPlayer(PlayerExecutionInfo executor) {
		super.executesPlayer(info -> {
			throwExceptionIfCannotRun(info.sender());
			executor.run(info);
		});

		return this;
	}

	@Override
	public CommandAPICommand executesPlayer(PlayerCommandExecutor executor) {
		super.executesPlayer((player, args) -> {
			throwExceptionIfCannotRun(player);
			executor.run(player, args);
		});

		return this;
	}

	@Override
	public CommandAPICommand executesPlayer(PlayerResultingExecutionInfo executor) {
		super.executesPlayer(info -> {
			throwExceptionIfCannotRun(info.sender());
			return executor.run(info);
		});

		return this;
	}

	@Override
	public CommandAPICommand executesPlayer(PlayerResultingCommandExecutor executor) {
		super.executesPlayer((player, args) -> {
			throwExceptionIfCannotRun(player);
			executor.run(player, args);
		});

		return this;
	}
}
