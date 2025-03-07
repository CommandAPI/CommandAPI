package dev.jorel.commandapi.kotlindsl

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import dev.jorel.commandapi.VelocityExecutable
import dev.jorel.commandapi.executors.*

// Executors for CommandAPICommand, CommandTree and ArgumentTree
inline fun VelocityExecutable<*>.anyExecutor(crossinline executor: (CommandSource, CommandArguments) -> Unit): VelocityExecutable<*> = executes(NormalExecutor<CommandSource, CommandSource> { sender, args ->
	executor(sender, args)
})
inline fun VelocityExecutable<*>.playerExecutor(crossinline executor: (Player, CommandArguments) -> Unit): VelocityExecutable<*> = executesPlayer(NormalExecutor<Player, CommandSource> { sender, args ->
	executor(sender, args)
})
inline fun VelocityExecutable<*>.consoleExecutor(crossinline executor: (ConsoleCommandSource, CommandArguments) -> Unit): VelocityExecutable<*> = executesConsole(NormalExecutor<ConsoleCommandSource, CommandSource> { sender, args ->
	executor(sender, args)
})

// Resulting executors

inline fun VelocityExecutable<*>.anyResultingExecutor(crossinline executor: (CommandSource, CommandArguments) -> Int): VelocityExecutable<*> = executes(ResultingExecutor<CommandSource, CommandSource> { sender, args ->
	executor(sender, args)
})
inline fun VelocityExecutable<*>.playerResultingExecutor(crossinline executor: (Player, CommandArguments) -> Int): VelocityExecutable<*> = executesPlayer(ResultingExecutor<Player, CommandSource> { sender, args ->
	executor(sender, args)
})
inline fun VelocityExecutable<*>.consoleResultingExecutor(crossinline executor: (ConsoleCommandSource, CommandArguments) -> Int): VelocityExecutable<*> = executesConsole(ResultingExecutor<ConsoleCommandSource, CommandSource> { sender, args ->
	executor(sender, args)
})

// ExecutionInfo normal executors

inline fun VelocityExecutable<*>.anyExecutionInfo(crossinline executor: (ExecutionInfo<CommandSource, CommandSource>) -> Unit): VelocityExecutable<*> = executes(NormalExecutorInfo<CommandSource, CommandSource> { info ->
	executor(info)
})
inline fun VelocityExecutable<*>.playerExecutionInfo(crossinline executor: (ExecutionInfo<Player, CommandSource>) -> Unit): VelocityExecutable<*> = executesPlayer(NormalExecutorInfo<Player, CommandSource> { info ->
	executor(info)
})
inline fun VelocityExecutable<*>.consoleExecutionInfo(crossinline executor: (ExecutionInfo<ConsoleCommandSource, CommandSource>) -> Unit): VelocityExecutable<*> = executesConsole(NormalExecutorInfo<ConsoleCommandSource, CommandSource> { info ->
	executor(info)
})

// ExecutionInfo resulting executors

inline fun VelocityExecutable<*>.anyResultingExecutionInfo(crossinline executor: (ExecutionInfo<CommandSource, CommandSource>) -> Int): VelocityExecutable<*> = executes(ResultingExecutorInfo<CommandSource, CommandSource> { info ->
	executor(info)
})
inline fun VelocityExecutable<*>.playerResultingExecutionInfo(crossinline executor: (ExecutionInfo<Player, CommandSource>) -> Int): VelocityExecutable<*> = executesPlayer(ResultingExecutorInfo<Player, CommandSource> { info ->
	executor(info)
})
inline fun VelocityExecutable<*>.consoleResultingExecutionInfo(crossinline executor: (ExecutionInfo<ConsoleCommandSource, CommandSource>) -> Int): VelocityExecutable<*> = executesConsole(ResultingExecutorInfo<ConsoleCommandSource, CommandSource> { info ->
	executor(info)
})