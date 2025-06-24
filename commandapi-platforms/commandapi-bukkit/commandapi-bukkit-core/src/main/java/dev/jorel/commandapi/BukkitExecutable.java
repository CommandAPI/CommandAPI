package dev.jorel.commandapi;

import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.exceptions.PlatformException;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.*;
import org.bukkit.command.CommandSender;

public interface BukkitExecutable<Impl
/// @cond DOX
extends BukkitExecutable<Impl>
/// @endcond
> extends PlatformExecutable<Impl, CommandSender> {

	// Regular command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(CommandSender, Object[]) -&gt; ()</code> that will be executed when the command is run
	 * @param types    A list of executor types to use this executes method for.
	 * @return this command builder
	 */
	default Impl executes(CommandExecutor executor, ExecutorType... types) {
		if (types == null || types.length == 0) {
			getExecutor().addNormalExecutor(executor);
		} else {
			for (ExecutorType type : types) {
				getExecutor().addNormalExecutor(new CommandExecutor() {
					@Override
					public void run(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
						executor.executeWith(new BukkitExecutionInfo<>(sender, CommandAPIBukkit.get().wrapCommandSender(sender), args));
					}

					@Override
					public ExecutorType getType() {
						return type;
					}
				});
			}
		}
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(BukkitCommandExecutionInfo) -&gt; ()</code> that will be executed when the command is run
	 * @param types    A list of executor types to use this executes method for.
	 * @return this command builder
	 */
	default Impl executes(CommandExecutionInfo executor, ExecutorType... types) {
		if (types == null || types.length == 0) {
			getExecutor().addNormalExecutor(executor);
		} else {
			for (ExecutorType type : types) {
				getExecutor().addNormalExecutor(new CommandExecutionInfo() {

					@Override
					public void run(ExecutionInfo<CommandSender, BukkitCommandSender<? extends CommandSender>> info) throws WrapperCommandSyntaxException {
						executor.executeWith(info);
					}

					@Override
					public ExecutorType getType() {
						return type;
					}
				});
			}
		}
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(CommandSender, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @param types    A list of executor types to use this executes method for.
	 * @return this command builder
	 */
	default Impl executes(ResultingCommandExecutor executor, ExecutorType... types) {
		if (types == null || types.length == 0) {
			getExecutor().addResultingExecutor(executor);
		} else {
			for (ExecutorType type : types) {
				getExecutor().addResultingExecutor(new ResultingCommandExecutor() {

					@Override
					public int run(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
						executor.executeWith(new BukkitExecutionInfo<>(sender, CommandAPIBukkit.get().wrapCommandSender(sender), args));
						return 1;
					}

					@Override
					public ExecutorType getType() {
						return type;
					}
				});
			}
		}
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(BukkitCommandExecutionInfo) -&gt; int</code> that will be executed when the command is run
	 * @param types    A list of executor types to use this executes method for.
	 * @return this command builder
	 */
	default Impl executes(ResultingCommandExecutionInfo executor, ExecutorType... types) {
		if (types == null || types.length == 0) {
			getExecutor().addResultingExecutor(executor);
		} else {
			for (ExecutorType type : types) {
				getExecutor().addResultingExecutor(new ResultingCommandExecutionInfo() {

					@Override
					public int run(ExecutionInfo<CommandSender, BukkitCommandSender<? extends CommandSender>> info) throws WrapperCommandSyntaxException {
						executor.executeWith(info);
						return 1;
					}

					@Override
					public ExecutorType getType() {
						return type;
					}
				});
			}
		}
		return instance();
	}


	// Player command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(Player, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesPlayer(PlayerCommandExecutor executor) {
		getExecutor().addNormalExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesPlayer(PlayerExecutionInfo info) {
		getExecutor().addNormalExecutor(info);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(Player, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesPlayer(PlayerResultingCommandExecutor executor) {
		getExecutor().addResultingExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesPlayer(PlayerResultingExecutionInfo info) {
		getExecutor().addResultingExecutor(info);
		return instance();
	}

	// Entity command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(Entity, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesEntity(EntityCommandExecutor executor) {
		getExecutor().addNormalExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesEntity(EntityExecutionInfo info) {
		getExecutor().addNormalExecutor(info);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(Entity, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesEntity(EntityResultingCommandExecutor executor) {
		getExecutor().addResultingExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesEntity(EntityResultingExecutionInfo info) {
		getExecutor().addResultingExecutor(info);
		return instance();
	}

	// Proxy command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(Entity, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesProxy(ProxyCommandExecutor executor) {
		getExecutor().addNormalExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesProxy(ProxyExecutionInfo info) {
		getExecutor().addNormalExecutor(info);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(Entity, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesProxy(ProxyResultingCommandExecutor executor) {
		getExecutor().addResultingExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesProxy(ProxyResultingExecutionInfo info) {
		getExecutor().addResultingExecutor(info);
		return instance();
	}

	// Command block command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(BlockCommandSender, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesCommandBlock(CommandBlockCommandExecutor executor) {
		getExecutor().addNormalExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesCommandBlock(CommandBlockExecutionInfo info) {
		getExecutor().addNormalExecutor(info);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(BlockCommandSender, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesCommandBlock(CommandBlockResultingCommandExecutor executor) {
		getExecutor().addResultingExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesCommandBlock(CommandBlockResultingExecutionInfo info) {
		getExecutor().addResultingExecutor(info);
		return instance();
	}

	// Console command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(ConsoleCommandSender, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesConsole(ConsoleCommandExecutor executor) {
		getExecutor().addNormalExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesConsole(ConsoleExecutionInfo info) {
		getExecutor().addNormalExecutor(info);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(ConsoleCommandSender, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesConsole(ConsoleResultingCommandExecutor executor) {
		getExecutor().addResultingExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesConsole(ConsoleResultingExecutionInfo info) {
		getExecutor().addResultingExecutor(info);
		return instance();
	}

	// Native command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(NativeCommandExecutor, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesNative(NativeCommandExecutor executor) {
		getExecutor().addNormalExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesNative(NativeExecutionInfo info) {
		getExecutor().addNormalExecutor(info);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(NativeCommandExecutor, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesNative(NativeResultingCommandExecutor executor) {
		getExecutor().addResultingExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesNative(NativeResultingExecutionInfo info) {
		getExecutor().addResultingExecutor(info);
		return instance();
	}

	// RemoteConsole command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(RemoteConsoleCommandExecutor, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesRemoteConsole(RemoteConsoleCommandExecutor executor) {
		getExecutor().addNormalExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesRemoteConsole(RemoteConsoleExecutionInfo info) {
		getExecutor().addNormalExecutor(info);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(RemoteConsoleResultingCommandExecutor, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesRemoteConsole(RemoteConsoleResultingCommandExecutor executor) {
		getExecutor().addResultingExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesRemoteConsole(RemoteConsoleResultingExecutionInfo info) {
		getExecutor().addResultingExecutor(info);
		return instance();
	}

	// Feedback-forwarding command executor
	
	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(FeedbackForwardingCommandExecutor, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesFeedbackForwarding(FeedbackForwardingCommandExecutor executor) {
		if (CommandAPIBukkit.platform().activePlatform() != Platform.PAPER) {
			throw new PlatformException("Attempted to use a FeedbackForwardingCommandExecutor on a non-paper platform (" + CommandAPIBukkit.platform().activePlatform().name() + ")!");
		}
		getExecutor().addNormalExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesFeedbackForwarding(FeedbackForwardingExecutionInfo info) {
		if (CommandAPIBukkit.platform().activePlatform() != Platform.PAPER) {
			throw new PlatformException("Attempted to use a FeedbackForwardingExecutionInfo on a non-paper platform (" + CommandAPIBukkit.platform().activePlatform().name() + ")!");
		}
		getExecutor().addNormalExecutor(info);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(FeedbackForwardingCommandExecutor, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesFeedbackForwarding(FeedbackForwardingResultingCommandExecutor executor) {
		if (CommandAPIBukkit.platform().activePlatform() != Platform.PAPER) {
			throw new PlatformException("Attempted to use a FeedbackForwardingResultingCommandExecutor on a non-paper platform (" + CommandAPIBukkit.platform().activePlatform().name() + ")!");
		}
		getExecutor().addResultingExecutor(executor);
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param info A lambda of type <code>(ExecutionInfo) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesFeedbackForwarding(FeedbackForwardingResultingExecutionInfo info) {
		if (CommandAPIBukkit.platform().activePlatform() != Platform.PAPER) {
			throw new PlatformException("Attempted to use a FeedbackForwardingResultingExecutionInfo on a non-paper platform (" + CommandAPIBukkit.platform().activePlatform().name() + ")!");
		}
		getExecutor().addResultingExecutor(info);
		return instance();
	}
}
