package dev.jorel.commandapi;

public interface PlatformExecutable<Impl
/// @cond DOX
extends PlatformExecutable<Impl, CommandSender>
/// @endcond
, CommandSender> extends ChainableBuilder<Impl> {
	/**
	 * Links to {@link Executable#getExecutor()} (make sure it has the same signature)
	 */
	CommandAPIExecutor<CommandSender> getExecutor();
}
