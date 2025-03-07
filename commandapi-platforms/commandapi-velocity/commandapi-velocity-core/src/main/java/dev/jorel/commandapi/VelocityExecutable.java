package dev.jorel.commandapi;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;

import dev.jorel.commandapi.executors.ExecutorType;
import dev.jorel.commandapi.executors.NormalExecutor;
import dev.jorel.commandapi.executors.NormalExecutorInfo;
import dev.jorel.commandapi.executors.ResultingExecutor;
import dev.jorel.commandapi.executors.ResultingExecutorInfo;
import dev.jorel.commandapi.executors.VelocityNormalTypedExecutor;
import dev.jorel.commandapi.executors.VelocityResultingTypedExecutor;

public interface VelocityExecutable<Impl extends VelocityExecutable<Impl>> extends PlatformExecutable<Impl, CommandSource> {
	// Regular command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;CommandSource, CommandSource> -&gt; ()</code> that will be executed when the command is run
	 * @param types    A list of executor types to use this executes method for.
	 * @return this command builder
	 */
	default Impl executes(NormalExecutorInfo<CommandSource, CommandSource> executor, ExecutorType... types) {
		if (types == null || types.length == 0) {
			types = new ExecutorType[]{ExecutorType.ALL};
		}

		getExecutor().addExecutor(new VelocityNormalTypedExecutor<>(executor, types));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(CommandSource, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @param types    A list of executor types to use this executes method for.
	 * @return this command builder
	 */
	default Impl executes(NormalExecutor<CommandSource, CommandSource> executor, ExecutorType... types) {
		// While we can cast directly to `NormalExecutorInfo` (because `NormalExecutor` extends it), this method
		//  is necessary to help Java identify the expression signature of user defined lambdas.
		//  The same applies for the rest of the executes methods
		return executes((NormalExecutorInfo<CommandSource, CommandSource>) executor, types);
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;CommandSource, CommandSource> -&gt; int</code> that will be executed when the command is run
	 * @param types    A list of executor types to use this executes method for.
	 * @return this command builder
	 */
	default Impl executes(ResultingExecutorInfo<CommandSource, CommandSource> executor, ExecutorType... types) {
		if (types == null || types.length == 0) {
			types = new ExecutorType[]{ExecutorType.ALL};
		}

		getExecutor().addExecutor(new VelocityResultingTypedExecutor<>(executor, types));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(CommandSource, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @param types    A list of executor types to use this executes method for.
	 * @return this command builder
	 */
	default Impl executes(ResultingExecutor<CommandSource, CommandSource> executor, ExecutorType... types) {
		return executes((ResultingExecutorInfo<CommandSource, CommandSource>) executor, types);
	}

	// Player command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;Player, CommandSource> -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesPlayer(NormalExecutorInfo<Player, CommandSource> executor) {
		getExecutor().addExecutor(new VelocityNormalTypedExecutor<>(executor, ExecutorType.PLAYER));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(Player, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesPlayer(NormalExecutor<Player, CommandSource> executor) {
		return executesPlayer((NormalExecutorInfo<Player, CommandSource>) executor);
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;Player, CommandSource> -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesPlayer(ResultingExecutorInfo<Player, CommandSource> executor) {
		getExecutor().addExecutor(new VelocityResultingTypedExecutor<>(executor, ExecutorType.PLAYER));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(Player, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesPlayer(ResultingExecutor<Player, CommandSource> executor) {
		return executesPlayer((ResultingExecutorInfo<Player, CommandSource>) executor);
	}

	// Console command executor

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;ConsoleCommandSource, CommandSource> -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesConsole(NormalExecutorInfo<ConsoleCommandSource, CommandSource> executor) {
		getExecutor().addExecutor(new VelocityNormalTypedExecutor<>(executor, ExecutorType.CONSOLE));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>(ConsoleCommandSource, CommandArguments) -&gt; ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesConsole(NormalExecutor<ConsoleCommandSource, CommandSource> executor) {
		return executesConsole((NormalExecutorInfo<ConsoleCommandSource, CommandSource>) executor);
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;ConsoleCommandSource, CommandSource> -&gt; int</code> or
	 *                 <code>(ConsoleCommandSource, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesConsole(ResultingExecutorInfo<ConsoleCommandSource, CommandSource> executor) {
		getExecutor().addExecutor(new VelocityResultingTypedExecutor<>(executor, ExecutorType.CONSOLE));
		return instance();
	}

	/**
	 * Adds an executor to the current command builder
	 *
	 * @param executor A lambda of type <code>ExecutionInfo&lt;ConsoleCommandSource> -&gt; int</code> or
	 *                 <code>(ConsoleCommandSource, CommandArguments) -&gt; int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	default Impl executesConsole(ResultingExecutor<ConsoleCommandSource, CommandSource> executor) {
		return executesConsole((ResultingExecutorInfo<ConsoleCommandSource, CommandSource>) executor);
	}
}
