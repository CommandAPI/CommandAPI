package dev.jorel.commandapi.testing;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class TestBootstrap implements PluginBootstrap {

	@Override
	public void bootstrap(@NotNull BootstrapContext bootstrapContext) {
		CommandAPI.onLoad(CommandAPIPaperConfig.forBootstrap(bootstrapContext).verboseOutput(true));

		new CommandAPICommand("bootstrap")
			.executes(info -> {
				info.sender().sendMessage(Component.text("Registration from bootstrap works!"));
			})
			.register();
	}
}
