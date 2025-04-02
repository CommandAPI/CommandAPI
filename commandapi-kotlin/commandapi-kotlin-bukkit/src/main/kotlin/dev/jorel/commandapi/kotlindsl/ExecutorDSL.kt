package dev.jorel.commandapi.kotlindsl

import dev.jorel.commandapi.BukkitExecutable
import dev.jorel.commandapi.commandsenders.BukkitBlockCommandSender
import dev.jorel.commandapi.commandsenders.BukkitCommandSender
import dev.jorel.commandapi.commandsenders.BukkitConsoleCommandSender
import dev.jorel.commandapi.commandsenders.BukkitEntity
import dev.jorel.commandapi.commandsenders.BukkitNativeProxyCommandSender
import dev.jorel.commandapi.commandsenders.BukkitPlayer
import dev.jorel.commandapi.commandsenders.BukkitRemoteConsoleCommandSender
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandBlockCommandExecutor
import dev.jorel.commandapi.executors.CommandBlockExecutionInfo
import dev.jorel.commandapi.executors.CommandBlockResultingCommandExecutor
import dev.jorel.commandapi.executors.CommandBlockResultingExecutionInfo
import dev.jorel.commandapi.executors.CommandExecutionInfo
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.ConsoleCommandExecutor
import dev.jorel.commandapi.executors.ConsoleExecutionInfo
import dev.jorel.commandapi.executors.ConsoleResultingCommandExecutor
import dev.jorel.commandapi.executors.ConsoleResultingExecutionInfo
import dev.jorel.commandapi.executors.EntityCommandExecutor
import dev.jorel.commandapi.executors.EntityExecutionInfo
import dev.jorel.commandapi.executors.EntityResultingCommandExecutor
import dev.jorel.commandapi.executors.EntityResultingExecutionInfo
import dev.jorel.commandapi.executors.ExecutionInfo
import dev.jorel.commandapi.executors.NativeCommandExecutor
import dev.jorel.commandapi.executors.NativeExecutionInfo
import dev.jorel.commandapi.executors.NativeResultingCommandExecutor
import dev.jorel.commandapi.executors.NativeResultingExecutionInfo
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import dev.jorel.commandapi.executors.PlayerExecutionInfo
import dev.jorel.commandapi.executors.PlayerResultingCommandExecutor
import dev.jorel.commandapi.executors.PlayerResultingExecutionInfo
import dev.jorel.commandapi.executors.ProxyCommandExecutor
import dev.jorel.commandapi.executors.ProxyExecutionInfo
import dev.jorel.commandapi.executors.ProxyResultingCommandExecutor
import dev.jorel.commandapi.executors.ProxyResultingExecutionInfo
import dev.jorel.commandapi.executors.RemoteConsoleCommandExecutor
import dev.jorel.commandapi.executors.RemoteConsoleExecutionInfo
import dev.jorel.commandapi.executors.RemoteConsoleResultingCommandExecutor
import dev.jorel.commandapi.executors.RemoteConsoleResultingExecutionInfo
import dev.jorel.commandapi.executors.ResultingCommandExecutionInfo
import dev.jorel.commandapi.executors.ResultingCommandExecutor
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.ProxiedCommandSender
import org.bukkit.command.RemoteConsoleCommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

// Executors for CommandAPICommand, CommandTree and ArgumentTree
inline fun BukkitExecutable<*>.anyExecutor(crossinline executor: (CommandSender, CommandArguments) -> Unit): BukkitExecutable<*> = executes(CommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.playerExecutor(crossinline executor: (Player, CommandArguments) -> Unit): BukkitExecutable<*> = executesPlayer(PlayerCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.entityExecutor(crossinline executor: (Entity, CommandArguments) -> Unit): BukkitExecutable<*> = executesEntity(EntityCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.consoleExecutor(crossinline executor: (ConsoleCommandSender, CommandArguments) -> Unit): BukkitExecutable<*> = executesConsole(ConsoleCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.commandBlockExecutor(crossinline executor: (BlockCommandSender, CommandArguments) -> Unit): BukkitExecutable<*> = executesCommandBlock(CommandBlockCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.proxyExecutor(crossinline executor: (ProxiedCommandSender, CommandArguments) -> Unit): BukkitExecutable<*> = executesProxy(ProxyCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.nativeExecutor(crossinline executor: (NativeProxyCommandSender, CommandArguments) -> Unit): BukkitExecutable<*> = executesNative(NativeCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.remoteConsoleExecutor(crossinline executor: (RemoteConsoleCommandSender, CommandArguments) -> Unit): BukkitExecutable<*> = executesRemoteConsole(RemoteConsoleCommandExecutor { sender, args ->
	executor(sender, args)
})


// Resulting executors

inline fun BukkitExecutable<*>.anyResultingExecutor(crossinline executor: (CommandSender, CommandArguments) -> Int): BukkitExecutable<*> = executes(ResultingCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.playerResultingExecutor(crossinline executor: (Player, CommandArguments) -> Int): BukkitExecutable<*> = executesPlayer(PlayerResultingCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.entityResultingExecutor(crossinline executor: (Entity, CommandArguments) -> Int): BukkitExecutable<*> = executesEntity(EntityResultingCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.consoleResultingExecutor(crossinline executor: (ConsoleCommandSender, CommandArguments) -> Int): BukkitExecutable<*> = executesConsole(ConsoleResultingCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.commandBlockResultingExecutor(crossinline executor: (BlockCommandSender, CommandArguments) -> Int): BukkitExecutable<*> = executesCommandBlock(CommandBlockResultingCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.proxyResultingExecutor(crossinline executor: (ProxiedCommandSender, CommandArguments) -> Int): BukkitExecutable<*> = executesProxy(ProxyResultingCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.nativeResultingExecutor(crossinline executor: (NativeProxyCommandSender, CommandArguments) -> Int): BukkitExecutable<*> = executesNative(NativeResultingCommandExecutor { sender, args ->
	executor(sender, args)
})
inline fun BukkitExecutable<*>.remoteConsoleResultingExecutor(crossinline executor: (RemoteConsoleCommandSender, CommandArguments) -> Int): BukkitExecutable<*> = executesRemoteConsole(RemoteConsoleResultingCommandExecutor { sender, args ->
	executor(sender, args)
})

// ExecutionInfo normal executors

inline fun BukkitExecutable<*>.anyExecutionInfo(crossinline executor: (ExecutionInfo<CommandSender, BukkitCommandSender<out CommandSender>>) -> Unit): BukkitExecutable<*> = executes(CommandExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.playerExecutionInfo(crossinline executor: (ExecutionInfo<Player, BukkitPlayer>) -> Unit): BukkitExecutable<*> = executesPlayer(PlayerExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.entityExecutionInfo(crossinline executor: (ExecutionInfo<Entity, BukkitEntity>) -> Unit): BukkitExecutable<*> = executesEntity(EntityExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.consoleExecutionInfo(crossinline executor: (ExecutionInfo<ConsoleCommandSender, BukkitConsoleCommandSender>) -> Unit): BukkitExecutable<*> = executesConsole(ConsoleExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.commandBlockExecutionInfo(crossinline executor: (ExecutionInfo<BlockCommandSender, BukkitBlockCommandSender>) -> Unit): BukkitExecutable<*> = executesCommandBlock(CommandBlockExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.proxyExecutionInfo(crossinline executor: (ExecutionInfo<NativeProxyCommandSender, BukkitNativeProxyCommandSender>) -> Unit): BukkitExecutable<*> = executesProxy(ProxyExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.nativeExecutionInfo(crossinline executor: (ExecutionInfo<NativeProxyCommandSender, BukkitNativeProxyCommandSender>) -> Unit): BukkitExecutable<*> = executesNative(NativeExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.remoteConsoleExecutionInfo(crossinline executor: (ExecutionInfo<RemoteConsoleCommandSender, BukkitRemoteConsoleCommandSender>) -> Unit): BukkitExecutable<*> = executesRemoteConsole(RemoteConsoleExecutionInfo { info ->
	executor(info)
})

// ExecutionInfo resulting executors

inline fun BukkitExecutable<*>.anyResultingExecutionInfo(crossinline executor: (ExecutionInfo<CommandSender, BukkitCommandSender<out CommandSender>>) -> Int): BukkitExecutable<*> = executes(ResultingCommandExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.playerResultingExecutionInfo(crossinline executor: (ExecutionInfo<Player, BukkitPlayer>) -> Int): BukkitExecutable<*> = executesPlayer(PlayerResultingExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.entityResultingExecutionInfo(crossinline executor: (ExecutionInfo<Entity, BukkitEntity>) -> Int): BukkitExecutable<*> = executesEntity(EntityResultingExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.consoleResultingExecutionInfo(crossinline executor: (ExecutionInfo<ConsoleCommandSender, BukkitConsoleCommandSender>) -> Int): BukkitExecutable<*> = executesConsole(ConsoleResultingExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.commandBlockResultingExecutionInfo(crossinline executor: (ExecutionInfo<BlockCommandSender, BukkitBlockCommandSender>) -> Int): BukkitExecutable<*> = executesCommandBlock(CommandBlockResultingExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.proxyResultingExecutionInfo(crossinline executor: (ExecutionInfo<NativeProxyCommandSender, BukkitNativeProxyCommandSender>) -> Int): BukkitExecutable<*> = executesProxy(ProxyResultingExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.nativeResultingExecutionInfo(crossinline executor: (ExecutionInfo<NativeProxyCommandSender, BukkitNativeProxyCommandSender>) -> Int): BukkitExecutable<*> = executesNative(NativeResultingExecutionInfo { info ->
	executor(info)
})
inline fun BukkitExecutable<*>.remoteConsoleResultingExecutionInfo(crossinline executor: (ExecutionInfo<RemoteConsoleCommandSender, BukkitRemoteConsoleCommandSender>) -> Int): BukkitExecutable<*> = executesRemoteConsole(RemoteConsoleResultingExecutionInfo { info ->
	executor(info)
})