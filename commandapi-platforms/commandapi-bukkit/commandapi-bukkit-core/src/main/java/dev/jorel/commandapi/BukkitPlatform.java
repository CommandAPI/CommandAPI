package dev.jorel.commandapi;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;

public interface BukkitPlatform<Source> extends CommandAPIPlatform<Argument<?>, CommandSender, Source> {

	@Override
	void onLoad(CommandAPIConfig<?> config);

	CommandMap getCommandMap();

	@Override
	BukkitCommandSender<? extends CommandSender> wrapCommandSender(CommandSender sender);

	Platform activePlatform();

	CommandRegistrationStrategy<Source> createCommandRegistrationStrategy();

}
