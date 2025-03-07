package dev.jorel.commandapi;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.executors.BukkitNormalTypedExecutor;
import dev.jorel.commandapi.executors.BukkitResultingTypedExecutor;
import dev.jorel.commandapi.executors.ExecutorType;
import dev.jorel.commandapi.executors.NormalExecutor;
import dev.jorel.commandapi.executors.NormalExecutorInfo;
import dev.jorel.commandapi.executors.ResultingExecutor;
import dev.jorel.commandapi.executors.ResultingExecutorInfo;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;

public interface BukkitExecutable<Impl
/// @cond DOX
extends BukkitExecutable<Impl>
/// @endcond
> extends PlatformExecutable<Impl, CommandSender> {
	// Regular command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;CommandSender, ?> -&gt; ()</code> that will be executed when the command is run
	 * @param types    A list of executor types to use this executes method for.
	 * @return this command builder
	 */
	default Impl executes(NormalExecutorInfo<CommandSender, ?> executor, ExecutorType... types) {
		if (types == null || types.length == 0) {
			types = new ExecutorType[]{ExecutorType.ALL};
		}

		getExecutor().addExecutor(new BukkitNormalTypedExecutor<>(executor, types));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(CommandSender, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @param types    A list of executor types to use this executes method for.
	 * @return this command builder
	 */
	default Impl executes(NormalExecutor<CommandSender, ?> executor, ExecutorType... types) {
		// While we can cast directly to `NormalExecutorInfo` (because `NormalExecutor` extends it), this method
		//  is necessary to help Java identify the expression signature of user defined lambdas.
		//  The same applies for the rest of the executes methods.
		return executes((NormalExecutorInfo<CommandSender, ?>) executor, types);
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;CommandSender, ?> -&gt; int</code> that will be executed when the command is run
	 * @param types    A list of executor types to use this executes method for.
	 * @return this command builder
	 */
	default Impl executes(ResultingExecutorInfo<CommandSender, ?> executor, ExecutorType... types) {
		if (types == null || types.length == 0) {
			types = new ExecutorType[]{ExecutorType.ALL};
		}

		getExecutor().addExecutor(new BukkitResultingTypedExecutor<>(executor, types));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(CommandSender, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @param types    A list of executor types to use this executes method for.
	 * @return this command builder
	 */
	default Impl executes(ResultingExecutor<CommandSender, ?> executor, ExecutorType... types) {
		return executes((ResultingExecutorInfo<CommandSender, ?>) executor, types);
	}

	// Player command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;Player, ?> -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesPlayer(NormalExecutorInfo<Player, ?> executor) {
		getExecutor().addExecutor(new BukkitNormalTypedExecutor<>(executor, ExecutorType.PLAYER));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(Player, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesPlayer(NormalExecutor<Player, ?> executor) {
		return executesPlayer((NormalExecutorInfo<Player, ?>) executor);
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;Player, ?> -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesPlayer(ResultingExecutorInfo<Player, ?> executor) {
		getExecutor().addExecutor(new BukkitResultingTypedExecutor<>(executor, ExecutorType.PLAYER));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(Player, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesPlayer(ResultingExecutor<Player, ?> executor) {
		return executesPlayer((ResultingExecutorInfo<Player, ?>) executor);
	}

	// Entity command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;Entity, ?> -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesEntity(NormalExecutorInfo<Entity, ?> executor) {
		getExecutor().addExecutor(new BukkitNormalTypedExecutor<>(executor, ExecutorType.ENTITY));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(Entity, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesEntity(NormalExecutor<Entity, ?> executor) {
		return executesEntity((NormalExecutorInfo<Entity, ?>) executor);
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;Entity, ?> -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesEntity(ResultingExecutorInfo<Entity, ?> executor) {
		getExecutor().addExecutor(new BukkitResultingTypedExecutor<>(executor, ExecutorType.ENTITY));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(Entity, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesEntity(ResultingExecutor<Entity, ?> executor) {
		return executesEntity((ResultingExecutorInfo<Entity, ?>) executor);
	}

	// Proxy command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;NativeProxyCommandSender, ?> -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesProxy(NormalExecutorInfo<NativeProxyCommandSender, ?> executor) {
		getExecutor().addExecutor(new BukkitNormalTypedExecutor<>(executor, ExecutorType.PROXY));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(NativeProxyCommandSender, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesProxy(NormalExecutor<NativeProxyCommandSender, ?> executor) {
		return executesProxy((NormalExecutorInfo<NativeProxyCommandSender, ?>) executor);
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;ProxiedCommandSender, ?> -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesProxy(ResultingExecutorInfo<NativeProxyCommandSender, ?> executor) {
		getExecutor().addExecutor(new BukkitResultingTypedExecutor<>(executor, ExecutorType.PROXY));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(NativeProxyCommandSender, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesProxy(ResultingExecutor<NativeProxyCommandSender, ?> executor) {
		return executesProxy((ResultingExecutorInfo<NativeProxyCommandSender, ?>) executor);
	}

	// Command block command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;BlockCommandSender, ?> -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesCommandBlock(NormalExecutorInfo<BlockCommandSender, ?> executor) {
		getExecutor().addExecutor(new BukkitNormalTypedExecutor<>(executor, ExecutorType.BLOCK));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(BlockCommandSender, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesCommandBlock(NormalExecutor<BlockCommandSender, ?> executor) {
		return executesCommandBlock((NormalExecutorInfo<BlockCommandSender, ?>) executor);
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;BlockCommandSender, ?> -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesCommandBlock(ResultingExecutorInfo<BlockCommandSender, ?> executor) {
		getExecutor().addExecutor(new BukkitResultingTypedExecutor<>(executor, ExecutorType.BLOCK));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type  <code>(BlockCommandSender, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesCommandBlock(ResultingExecutor<BlockCommandSender, ?> executor) {
		return executesCommandBlock((ResultingExecutorInfo<BlockCommandSender, ?>) executor);
	}

	// Console command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;ConsoleCommandSender, ?> -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesConsole(NormalExecutorInfo<ConsoleCommandSender, ?> executor) {
		getExecutor().addExecutor(new BukkitNormalTypedExecutor<>(executor, ExecutorType.CONSOLE));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(ConsoleCommandSender, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesConsole(NormalExecutor<ConsoleCommandSender, ?> executor) {
		return executesConsole((NormalExecutorInfo<ConsoleCommandSender, ?>) executor);
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;ConsoleCommandSender, ?> -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesConsole(ResultingExecutorInfo<ConsoleCommandSender, ?> executor) {
		getExecutor().addExecutor(new BukkitResultingTypedExecutor<>(executor, ExecutorType.CONSOLE));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(ConsoleCommandSender, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesConsole(ResultingExecutor<ConsoleCommandSender, ?> executor) {
		return executesConsole((ResultingExecutorInfo<ConsoleCommandSender, ?>) executor);
	}

	// Native command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;NativeProxyCommandSender, ?> -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesNative(NormalExecutorInfo<NativeProxyCommandSender, ?> executor) {
		getExecutor().addExecutor(new BukkitNormalTypedExecutor<>(executor, ExecutorType.NATIVE));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(NativeProxyCommandSender, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesNative(NormalExecutor<NativeProxyCommandSender, ?> executor) {
		return executesNative((NormalExecutorInfo<NativeProxyCommandSender, ?>) executor);
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;NativeProxyCommandSender, ?> -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesNative(ResultingExecutorInfo<NativeProxyCommandSender, ?> executor) {
		getExecutor().addExecutor(new BukkitResultingTypedExecutor<>(executor, ExecutorType.NATIVE));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(NativeProxyCommandSender, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesNative(ResultingExecutor<NativeProxyCommandSender, ?> executor) {
		return executesNative((ResultingExecutorInfo<NativeProxyCommandSender, ?>) executor);
	}

	// RemoteConsole command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;RemoteConsoleCommandSender, ?> -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesRemoteConsole(NormalExecutorInfo<RemoteConsoleCommandSender, ?> executor) {
		getExecutor().addExecutor(new BukkitNormalTypedExecutor<>(executor, ExecutorType.REMOTE));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(RemoteConsoleCommandSender, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesRemoteConsole(NormalExecutor<RemoteConsoleCommandSender, ?> executor) {
		return executesRemoteConsole((NormalExecutorInfo<RemoteConsoleCommandSender, ?>) executor);
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;RemoteConsoleCommandSender, ?> -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesRemoteConsole(ResultingExecutorInfo<RemoteConsoleCommandSender, ?> executor) {
		getExecutor().addExecutor(new BukkitResultingTypedExecutor<>(executor, ExecutorType.REMOTE));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(RemoteConsoleCommandSender, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesRemoteConsole(ResultingExecutor<RemoteConsoleCommandSender, ?> executor) {
		return executesRemoteConsole((ResultingExecutorInfo<RemoteConsoleCommandSender, ?>) executor);
	}

	// Feedback-forwarding command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;FeedbackForwardingCommandExecutor, ?> -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesFeedbackForwarding(NormalExecutorInfo<CommandSender, ?> executor) {
		getExecutor().addExecutor(new BukkitNormalTypedExecutor<>(executor, ExecutorType.FEEDBACK_FORWARDING));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(FeedbackForwardingCommandExecutor, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesFeedbackForwarding(NormalExecutor<CommandSender, ?> executor) {
		return executesFeedbackForwarding((NormalExecutorInfo<CommandSender, ?>) executor);
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;FeedbackForwardingCommandExecutor, ?> -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesFeedbackForwarding(ResultingExecutorInfo<CommandSender, ?> executor) {
		getExecutor().addExecutor(new BukkitResultingTypedExecutor<>(executor, ExecutorType.FEEDBACK_FORWARDING));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(FeedbackForwardingCommandExecutor, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesFeedbackForwarding(ResultingExecutor<CommandSender, ?> executor) {
		return executesFeedbackForwarding((ResultingExecutorInfo<CommandSender, ?>) executor);
	}
}
