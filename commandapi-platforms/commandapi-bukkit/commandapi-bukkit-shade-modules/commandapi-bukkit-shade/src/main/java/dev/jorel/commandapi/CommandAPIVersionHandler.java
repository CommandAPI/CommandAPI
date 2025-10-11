package dev.jorel.commandapi;

import dev.jorel.commandapi.nms.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public abstract class CommandAPIVersionHandler {
	// Determine whether this is paper or not
	public static final boolean IS_PAPER;

	static {
		boolean isPaper;
		try {
			Class.forName("io.papermc.paper.ServerBuildInfo");
			isPaper = true;
		} catch (ClassNotFoundException ignored) {
			isPaper = false;
		}
		IS_PAPER = isPaper;
	}

	// Handle configuration
	private static Consumer<CommandAPIBukkitConfig<?>> configureBukkit = null;

	public static void configureBukkit(Consumer<CommandAPIBukkitConfig<?>> configure) {
		configureBukkit = configure;
	}

	private static Consumer<CommandAPIPaperConfig> configurePaper = null;

	public static void configurePaper(Consumer<CommandAPIPaperConfig> configure) {
		configurePaper = configure;
	}

	private static Consumer<CommandAPISpigotConfig> configureSpigot = null;

	public static void configureSpigot(Consumer<CommandAPISpigotConfig> configure) {
		configureSpigot = configure;
	}

	public static void load(JavaPlugin plugin) {
		CommandAPIBukkitConfig<?> config;
		if (IS_PAPER) {
			CommandAPIPaperConfig paperConfig = CommandAPIPaperConfig.forPlugin(plugin);

			if (configurePaper != null) {
				configurePaper.accept(paperConfig);
			}

			config = paperConfig;
		} else {
			CommandAPISpigotConfig spigotConfig = new CommandAPISpigotConfig(plugin);

			if (configureSpigot != null) {
				configureSpigot.accept(spigotConfig);
			}

			config = spigotConfig;
		}

		if (configureBukkit != null) {
			configureBukkit.accept(config);
		}

		CommandAPI.onLoad(config);
	}

	// Expected method that CommandAPI#onLoad uses
	static LoadContext getPlatform(CommandAPIConfig<?> config) {
		if (IS_PAPER) {
			InternalPaperConfig internalPaperConfig;
			if (config instanceof CommandAPIPaperConfig paperConfig) {
				internalPaperConfig = new InternalPaperConfig(paperConfig);
			} else {
				throw new IllegalArgumentException("CommandAPIPaper was loaded with non-Paper config!");
			}
			return CommandAPIPaperVersionHandler.loadPaper(internalPaperConfig);
		} else {
			InternalSpigotConfig internalSpigotConfig;
			if (config instanceof CommandAPISpigotConfig spigotConfig) {
				internalSpigotConfig = new InternalSpigotConfig(spigotConfig);
			} else {
				throw new IllegalArgumentException("CommandAPISpigot was loaded with non-Spigot config!");
			}
			return CommandAPISpigotVersionHandler.loadSpigot(internalSpigotConfig);
		}
	}
}
