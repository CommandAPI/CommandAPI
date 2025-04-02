package dev.jorel.commandapi.kotlindsl

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import dev.jorel.commandapi.VelocityExecutable
import dev.jorel.commandapi.commandsenders.VelocityCommandSender
import dev.jorel.commandapi.commandsenders.VelocityConsoleCommandSender
import dev.jorel.commandapi.commandsenders.VelocityPlayer
import dev.jorel.commandapi.executors.*

// Executors for CommandAPICommand, CommandTree and ArgumentTree
inline fun VelocityExecutable<*>.anyExecutor(crossinline executor: (CommandSource, CommandArguments) -> Unit): VelocityExecutable<*> = executes(CommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun VelocityExecutable<*>.playerExecutor(crossinline executor: (Player, CommandArguments) -> Unit): VelocityExecutable<*> = executesPlayer(PlayerCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun VelocityExecutable<*>.consoleExecutor(crossinline executor: (ConsoleCommandSource, CommandArguments) -> Unit): VelocityExecutable<*> = executesConsole(ConsoleCommandExecutor { sender, args ->
	executor(sender, args)
})

// Resulting executors

inline fun VelocityExecutable<*>.anyResultingExecutor(crossinline executor: (CommandSource, CommandArguments) -> Int): VelocityExecutable<*> = executes(ResultingCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun VelocityExecutable<*>.playerResultingExecutor(crossinline executor: (Player, CommandArguments) -> Int): VelocityExecutable<*> = executesPlayer(PlayerResultingCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun VelocityExecutable<*>.consoleResultingExecutor(crossinline executor: (ConsoleCommandSource, CommandArguments) -> Int): VelocityExecutable<*> = executesConsole(ConsoleResultingCommandExecutor { sender, args ->
	executor(sender, args)
})

// ExecutionInfo normal executors

inline fun VelocityExecutable<*>.anyExecutionInfo(crossinline executor: (ExecutionInfo<CommandSource, VelocityCommandSender<out CommandSource>>) -> Unit): VelocityExecutable<*> = executes(CommandExecutionInfo { info ->
	executor(info)
})
inline fun VelocityExecutable<*>.playerExecutionInfo(crossinline executor: (ExecutionInfo<Player, VelocityPlayer>) -> Unit): VelocityExecutable<*> = executesPlayer(PlayerExecutionInfo { info ->
	executor(info)
})
inline fun VelocityExecutable<*>.consoleExecutionInfo(crossinline executor: (ExecutionInfo<ConsoleCommandSource, VelocityConsoleCommandSender>) -> Unit): VelocityExecutable<*> = executesConsole(ConsoleExecutionInfo { info ->
	executor(info)
})

// ExecutionInfo resulting executors

inline fun VelocityExecutable<*>.anyResultingExecutionInfo(crossinline executor: (ExecutionInfo<CommandSource, VelocityCommandSender<out CommandSource>>) -> Int): VelocityExecutable<*> = executes(ResultingCommandExecutionInfo { info ->
	executor(info)
})
inline fun VelocityExecutable<*>.playerResultingExecutionInfo(crossinline executor: (ExecutionInfo<Player, VelocityPlayer>) -> Int): VelocityExecutable<*> = executesPlayer(PlayerResultingExecutionInfo { info ->
	executor(info)
})
inline fun VelocityExecutable<*>.consoleResultingExecutionInfo(crossinline executor: (ExecutionInfo<ConsoleCommandSource, VelocityConsoleCommandSender>) -> Int): VelocityExecutable<*> = executesConsole(ConsoleResultingExecutionInfo { info ->
	executor(info)
})