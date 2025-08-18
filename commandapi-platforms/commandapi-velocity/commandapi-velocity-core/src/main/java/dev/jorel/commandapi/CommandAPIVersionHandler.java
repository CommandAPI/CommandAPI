package dev.jorel.commandapi;

public abstract class CommandAPIVersionHandler {
	static LoadContext getPlatform(CommandAPIConfig<?> config) {
		InternalVelocityConfig internalVelocityConfig;
		if (config instanceof CommandAPIVelocityConfig velocityConfig) {
			internalVelocityConfig = new InternalVelocityConfig(velocityConfig);
		} else {
			throw new IllegalArgumentException("CommandAPIVelocity was loaded with non-Velocity config!");
		}
		return new LoadContext(new CommandAPIVelocity(internalVelocityConfig));
	}
}
