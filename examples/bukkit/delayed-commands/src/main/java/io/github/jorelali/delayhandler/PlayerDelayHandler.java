package io.github.jorelali.delayhandler;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.ExecutionInfo;
import dev.jorel.commandapi.executors.PlayerExecutionInfo;
import org.bukkit.entity.Player;

import java.time.Duration;

// This is a general interface for turning a normal player executor into a delayed executor
public interface PlayerDelayHandler {
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

	// This method is implemented by the child classes
	// If the command is currently delayed for the player, an exception should be thrown
	void throwExceptionIfCannotRun(ExecutionInfo<Player, ?> info) throws WrapperCommandSyntaxException;

	// This method takes one CommandAPI executor and uses it inside a new executor
	// The new executor also uses the throwExceptionIfCannotRun method to stop
	//  command execution if the command's delay is currently in effect
	default PlayerExecutionInfo delayMethod(PlayerExecutionInfo executor) {
		return info -> {
			throwExceptionIfCannotRun(info);
			executor.run(info);
		};
	}
}
