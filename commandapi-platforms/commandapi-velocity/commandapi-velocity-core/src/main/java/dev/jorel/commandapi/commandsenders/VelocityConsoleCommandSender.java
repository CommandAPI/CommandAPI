package dev.jorel.commandapi.commandsenders;
import com.velocitypowered.api.proxy.ConsoleCommandSource;

import dev.jorel.commandapi.abstractions.AbstractConsoleCommandSender;

public class VelocityConsoleCommandSender extends AbstractConsoleCommandSender<ConsoleCommandSource> implements VelocityCommandSender<ConsoleCommandSource> {

	private final ConsoleCommandSource source;
	
	public VelocityConsoleCommandSender(ConsoleCommandSource source) {
		this.source = source;
	}
	
	@Override
	public boolean hasPermission(String permissionNode) {
		return this.source.hasPermission(permissionNode);
	}
	
	@Override
	public boolean isOp() {
		return true;
	}

	@Override
	public ConsoleCommandSource getSource() {
		return this.source;
	}
	
}