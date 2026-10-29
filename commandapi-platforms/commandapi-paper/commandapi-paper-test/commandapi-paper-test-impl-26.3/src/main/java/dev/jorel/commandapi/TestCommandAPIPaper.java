package dev.jorel.commandapi;

import dev.jorel.commandapi.nms.BundledNMS;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A {@link CommandAPIPaper} that can run in a MockBukkit environment. MockBukkit doesn't implement Paper's
 * lifecycle events, so this skips registering them when loading and enabling. The rest of the platform (sender
 * wrapping, permissions, help topics, etc.) is the real Paper implementation.
 */
public class TestCommandAPIPaper extends CommandAPIPaper<CommandSourceStack> {
	public TestCommandAPIPaper(InternalPaperConfig config, BundledNMS<CommandSourceStack> nms) {
		super(config, nms);
	}

	@Override
	public void onLoad() {
		// Skips CommandAPIPaper#onLoad's lifecycle event registration
		super.checkDependencies();
	}

	@Override
	public void onEnable() {
		super.plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(getConfiguration().getPluginName());

		// Same as CommandAPIPaper#onEnable, except for the datapack reload and lifecycle events
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			getCommandRegistrationStrategy().runTasksAfterServerStart();
			updateHelpForCommands(CommandAPI.getRegisteredCommands());
		}, 1L);

		super.stopCommandRegistrations();
	}
}
