package io.github.jorelali

import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player

class SayHelloCommand {

	fun register() {
		commandTree("sayhello") {
			entitySelectorArgumentOnePlayer("target") {
				playerExecutor { player, args ->
					val target: Player = args[0] as Player
					target.sendMessage("§6${player.name} says hello to you!")
				}
			}
		}

		commandAPICommand("suicide") {
			literalArgument("confirm")
			playerExecutor { player, _ ->
				player.health = 0.0
			}
		}
	}

}