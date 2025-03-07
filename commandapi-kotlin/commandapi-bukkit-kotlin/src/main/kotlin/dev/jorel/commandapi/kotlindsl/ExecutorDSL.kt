package dev.jorel.commandapi.kotlindsl

import dev.jorel.commandapi.BukkitExecutable
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.ExecutionInfo
import dev.jorel.commandapi.executors.NormalExecutor
import dev.jorel.commandapi.executors.ResultingExecutor
import dev.jorel.commandapi.executors.NormalExecutorInfo
import dev.jorel.commandapi.executors.ResultingExecutorInfo
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender

import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.RemoteConsoleCommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

// Executors for CommandAPICommand, CommandTree and ArgumentTree
inline fun BukkitExecutable<*>.anyExecutor(crossinline executor: (CommandSender, CommandArguments) -> Unit): BukkitExecutable<*> = executes( NormalExecutor<CommandSender, Any> { sender: CommandSender, args: CommandArguments ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.playerExecutor(crossinline executor: (Player, CommandArguments) -> Unit): BukkitExecutable<*> = executesPlayer(NormalExecutor<Player, Any> { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.entityExecutor(crossinline executor: (Entity, CommandArguments) -> Unit): BukkitExecutable<*> = executesEntity(NormalExecutor<Entity, Any> { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.consoleExecutor(crossinline executor: (ConsoleCommandSender, CommandArguments) -> Unit): BukkitExecutable<*> = executesConsole(NormalExecutor<ConsoleCommandSender, Any> { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.commandBlockExecutor(crossinline executor: (BlockCommandSender, CommandArguments) -> Unit): BukkitExecutable<*> = executesCommandBlock(NormalExecutor<BlockCommandSender, Any> { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.proxyExecutor(crossinline executor: (NativeProxyCommandSender, CommandArguments) -> Unit): BukkitExecutable<*> = executesProxy(NormalExecutor<NativeProxyCommandSender, Any> { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.nativeExecutor(crossinline executor: (NativeProxyCommandSender, CommandArguments) -> Unit): BukkitExecutable<*> = executesNative(NormalExecutor<NativeProxyCommandSender, Any> { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.remoteConsoleExecutor(crossinline executor: (RemoteConsoleCommandSender, CommandArguments) -> Unit): BukkitExecutable<*> = executesRemoteConsole(NormalExecutor<RemoteConsoleCommandSender, Any> { sender, args ->
	executor(sender, args)
})


// Resulting executors

inline fun BukkitExecutable<*>.anyResultingExecutor(crossinline executor: (CommandSender, CommandArguments) -> Int): BukkitExecutable<*> = executes(ResultingExecutor<CommandSender, Any> { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.playerResultingExecutor(crossinline executor: (Player, CommandArguments) -> Int): BukkitExecutable<*> = executesPlayer(ResultingExecutor<Player, Any> { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.entityResultingExecutor(crossinline executor: (Entity, CommandArguments) -> Int): BukkitExecutable<*> = executesEntity(ResultingExecutor<Entity, Any> { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.consoleResultingExecutor(crossinline executor: (ConsoleCommandSender, CommandArguments) -> Int): BukkitExecutable<*> = executesConsole(ResultingExecutor<ConsoleCommandSender, Any> { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.commandBlockResultingExecutor(crossinline executor: (BlockCommandSender, CommandArguments) -> Int): BukkitExecutable<*> = executesCommandBlock(ResultingExecutor<BlockCommandSender, Any> { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.proxyResultingExecutor(crossinline executor: (NativeProxyCommandSender, CommandArguments) -> Int): BukkitExecutable<*> = executesProxy(ResultingExecutor<NativeProxyCommandSender, Any> { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.nativeResultingExecutor(crossinline executor: (NativeProxyCommandSender, CommandArguments) -> Int): BukkitExecutable<*> = executesNative(ResultingExecutor<NativeProxyCommandSender, Any> { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.remoteConsoleResultingExecutor(crossinline executor: (RemoteConsoleCommandSender, CommandArguments) -> Int): BukkitExecutable<*> = executesRemoteConsole(ResultingExecutor<RemoteConsoleCommandSender, Any> { sender, args ->
	executor(sender, args)
})

// ExecutionInfo normal executors

inline fun BukkitExecutable<*>.anyExecutionInfo(crossinline executor: (ExecutionInfo<CommandSender, Any>) -> Unit): BukkitExecutable<*> = executes(NormalExecutorInfo<CommandSender, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.playerExecutionInfo(crossinline executor: (ExecutionInfo<Player, Any>) -> Unit): BukkitExecutable<*> = executesPlayer(NormalExecutorInfo<Player, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.entityExecutionInfo(crossinline executor: (ExecutionInfo<Entity, Any>) -> Unit): BukkitExecutable<*> = executesEntity(NormalExecutorInfo<Entity, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.consoleExecutionInfo(crossinline executor: (ExecutionInfo<ConsoleCommandSender, Any>) -> Unit): BukkitExecutable<*> = executesConsole(NormalExecutorInfo<ConsoleCommandSender, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.commandBlockExecutionInfo(crossinline executor: (ExecutionInfo<BlockCommandSender, Any>) -> Unit): BukkitExecutable<*> = executesCommandBlock(NormalExecutorInfo<BlockCommandSender, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.proxyExecutionInfo(crossinline executor: (ExecutionInfo<NativeProxyCommandSender, Any>) -> Unit): BukkitExecutable<*> = executesProxy(NormalExecutorInfo<NativeProxyCommandSender, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.nativeExecutionInfo(crossinline executor: (ExecutionInfo<NativeProxyCommandSender, Any>) -> Unit): BukkitExecutable<*> = executesNative(NormalExecutorInfo<NativeProxyCommandSender, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.remoteConsoleExecutionInfo(crossinline executor: (ExecutionInfo<RemoteConsoleCommandSender, Any>) -> Unit): BukkitExecutable<*> = executesRemoteConsole(NormalExecutorInfo<RemoteConsoleCommandSender, Any> { info ->
	executor(info)
})

// ExecutionInfo resulting executors

inline fun BukkitExecutable<*>.anyResultingExecutionInfo(crossinline executor: (ExecutionInfo<CommandSender, Any>) -> Int): BukkitExecutable<*> = executes(ResultingExecutorInfo<CommandSender, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.playerResultingExecutionInfo(crossinline executor: (ExecutionInfo<Player, Any>) -> Int): BukkitExecutable<*> = executesPlayer(ResultingExecutorInfo<Player, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.entityResultingExecutionInfo(crossinline executor: (ExecutionInfo<Entity, Any>) -> Int): BukkitExecutable<*> = executesEntity(ResultingExecutorInfo<Entity, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.consoleResultingExecutionInfo(crossinline executor: (ExecutionInfo<ConsoleCommandSender, Any>) -> Int): BukkitExecutable<*> = executesConsole(ResultingExecutorInfo<ConsoleCommandSender, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.commandBlockResultingExecutionInfo(crossinline executor: (ExecutionInfo<BlockCommandSender, Any>) -> Int): BukkitExecutable<*> = executesCommandBlock(ResultingExecutorInfo<BlockCommandSender, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.proxyResultingExecutionInfo(crossinline executor: (ExecutionInfo<NativeProxyCommandSender, Any>) -> Int): BukkitExecutable<*> = executesProxy(ResultingExecutorInfo<NativeProxyCommandSender, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.nativeResultingExecutionInfo(crossinline executor: (ExecutionInfo<NativeProxyCommandSender, Any>) -> Int): BukkitExecutable<*> = executesNative(ResultingExecutorInfo<NativeProxyCommandSender, Any> { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.remoteConsoleResultingExecutionInfo(crossinline executor: (ExecutionInfo<RemoteConsoleCommandSender, Any>) -> Int): BukkitExecutable<*> = executesRemoteConsole(ResultingExecutorInfo<RemoteConsoleCommandSender, Any> { info ->
	executor(info)
})