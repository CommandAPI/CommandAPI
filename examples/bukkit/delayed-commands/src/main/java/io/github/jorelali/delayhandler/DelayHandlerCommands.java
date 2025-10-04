package io.github.jorelali.delayhandler;

import dev.jorel.commandapi.CommandAPICommand;

import java.util.concurrent.TimeUnit;

public class DelayHandlerCommands {
	public static void registerCommands() {
		// PlayerDelayHandler is a general interface for turning a normal player executor into a delayed executor
		// PerPlayerDelayHandler keeps track of the delay for each player that uses it
		PlayerDelayHandler perPlayerDelay = new PerPlayerDelayHandler(10, TimeUnit.SECONDS);

		new CommandAPICommand("delayHandlerPerPlayer1")
			// PlayerDelayHandler works directly on the Executors of the method, so it goes inside the `executes` methods
			//  This delay only acts on this method
			//  This also works for the executes methods of CommandTree and ArgumentTree
			.executesPlayer(perPlayerDelay.delayMethod(info -> info.sender().sendMessage("You ran delayHandlerPerPlayer1")))
			.register();

		new CommandAPICommand("delayHandlerPerPlayer2")
			// While the PlayerDelayHandler only works on one Executor at a time, it can be reused for multiple methods,
			//  making them to share a delay
			.executesPlayer(perPlayerDelay.delayMethod(info -> info.sender().sendMessage("You ran delayHandlerPerPlayer2")))
			.register();

		// GlobalPlayerDelayHandler shares its delay for all players
		PlayerDelayHandler globalDelay = new GlobalPlayerDelayHandler(10, TimeUnit.SECONDS);

		new CommandAPICommand("delayHandlerGlobal1")
			// This new PlayerDelayHandler is independent of the first two commands, since it is a different object
			.executesPlayer(globalDelay.delayMethod(info -> info.sender().sendMessage("You ran delayHandlerGlobal1")))
			.register();

		new CommandAPICommand("delayHandlerGlobal2")
			.executesPlayer(globalDelay.delayMethod(info -> info.sender().sendMessage("You ran delayHandlerGlobal2")))
			.register();
	}
}
