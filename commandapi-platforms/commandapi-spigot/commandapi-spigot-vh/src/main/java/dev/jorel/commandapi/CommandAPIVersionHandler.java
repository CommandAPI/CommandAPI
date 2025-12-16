package dev.jorel.commandapi;

import dev.jorel.commandapi.exceptions.UnsupportedVersionException;
import dev.jorel.commandapi.nms.SpigotNMS_1_20_R1;
import dev.jorel.commandapi.nms.SpigotNMS_1_20_R2;
import dev.jorel.commandapi.nms.SpigotNMS_1_20_R3;
import dev.jorel.commandapi.nms.SpigotNMS_1_20_R4;
import dev.jorel.commandapi.nms.SpigotNMS_1_21_R1;
import dev.jorel.commandapi.nms.SpigotNMS_1_21_R2;
import dev.jorel.commandapi.nms.SpigotNMS_1_21_R3;
import dev.jorel.commandapi.nms.SpigotNMS_1_21_R4;
import dev.jorel.commandapi.nms.SpigotNMS_1_21_R5;
import dev.jorel.commandapi.nms.SpigotNMS_1_21_R6;
import dev.jorel.commandapi.nms.SpigotNMS_1_21_R7;
import org.bukkit.Bukkit;

public abstract class CommandAPIVersionHandler {

	static LoadContext getPlatform(CommandAPIConfig<?> config) {
		InternalSpigotConfig internalSpigotConfig;
		if (config instanceof CommandAPISpigotConfig spigotConfig) {
			internalSpigotConfig = new InternalSpigotConfig(spigotConfig);
		} else {
			throw new IllegalArgumentException("CommandAPISpigot was loaded with non-Spigot config!");
		}
		try {
			String version = Bukkit.getBukkitVersion().split("-")[0];
			CommandAPIPlatform<?, ?, ?> platform = switch (version) {
				case "1.20", "1.20.1" -> new SpigotNMS_1_20_R1(internalSpigotConfig);
				case "1.20.2" -> new SpigotNMS_1_20_R2(internalSpigotConfig);
				case "1.20.3", "1.20.4" -> new SpigotNMS_1_20_R3(internalSpigotConfig);
				case "1.20.5", "1.20.6" -> new SpigotNMS_1_20_R4(internalSpigotConfig);
				case "1.21", "1.21.1" -> new SpigotNMS_1_21_R1(internalSpigotConfig);
				case "1.21.2", "1.21.3" -> new SpigotNMS_1_21_R2(internalSpigotConfig);
				case "1.21.4" -> new SpigotNMS_1_21_R3(internalSpigotConfig);
				case "1.21.5" -> new SpigotNMS_1_21_R4(internalSpigotConfig);
				case "1.21.6", "1.21.7", "1.21.8" -> new SpigotNMS_1_21_R5(internalSpigotConfig);
				case "1.21.9", "1.21.10" -> new SpigotNMS_1_21_R6(internalSpigotConfig);
				case "1.21.11" -> new SpigotNMS_1_21_R7(internalSpigotConfig);
				default -> null;
			};
			if (platform != null) {
				return new LoadContext(platform);
			}
			if (internalSpigotConfig.fallbackToLatestNMS()) {
				return new LoadContext(new SpigotNMS_1_21_R7(internalSpigotConfig), () -> {
					CommandAPI.logWarning("Loading the CommandAPI with the latest and potentially incompatible NMS implementation.");
					CommandAPI.logWarning("While you may find success with this, further updates might be necessary to fully support the version you are using.");
				});
			}
			throw new UnsupportedVersionException(version);
		} catch (Throwable error) {
			// Something went sideways when trying to load a platform. This probably means we're shading the wrong mappings.
			// Because this is an error we'll just rethrow this (instead of piping it into logError, which we can't really
			// do anyway since the CommandAPILogger isn't loaded), but include some helpful(?) logging that might point
			// users in the right direction
			throw new IllegalStateException("The CommandAPI's NMS hook failed to load! This version of the CommandAPI is " +
				(MojangMappedVersionHandler.isMojangMapped() ? "Mojang" : "Spigot") + "-mapped. Have you checked that " +
				"you are using a CommandAPI version that matches the mappings that your plugin is using?", error);
		}
	}

}
