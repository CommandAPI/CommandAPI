package io.github.jorelali.delayedapicommand;

import java.util.concurrent.TimeUnit;

public class DelayedAPICommands {
	public static void registerCommands() {
		// PlayerDelayedCommandAPICommand has all the functions of a CommandAPICommand, but also adds delay to any
		//  executors given by the `executesPlayer` methods

		// PerPlayerDelayedCommandAPICommand keeps track of the delay for each player that uses it
		new PerPlayerDelayedCommandAPICommand("delayedAPICommandPerPlayer", 10, TimeUnit.SECONDS)
			.executesPlayer(info -> {
				info.sender().sendMessage("You ran delayedAPICommandPerPlayer");
			})
			.register();

		// GlobalPlayerDelayedCommandAPICommand shares its delay for all players
		new GlobalPlayerDelayedCommandAPICommand("delayedAPICommandGlobal", 10, TimeUnit.SECONDS)
			.executesPlayer((player, args) -> {
				player.sendMessage("You ran delayedAPICommandGlobal");
			})
			.register();
	}
}
