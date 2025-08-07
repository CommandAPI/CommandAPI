package dev.jorel.commandapi;

public abstract class CommandAPIVersionHandler {
	static LoadContext getPlatform(InternalConfig config) {
		return new LoadContext(new CommandAPIVelocity());
	}

	static InternalConfig getConfig(CommandAPIConfig<?> config) {
		// This should never be a casting error since only the Velocity module has this version handler and the CommandAPIVelocityConfig
		return new InternalVelocityConfig((CommandAPIVelocityConfig) config);
	}
}
