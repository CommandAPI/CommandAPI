package dev.jorel.commandapi;

import dev.jorel.commandapi.exceptions.UnsupportedVersionException;
import dev.jorel.commandapi.nms.*;
import io.papermc.paper.ServerBuildInfo;

public class CommandAPIPaperVersionHandler {
	// Copied from commandapi-paper-vh
	static LoadContext loadPaper(InternalPaperConfig internalPaperConfig) {
		try {
			ServerBuildInfo buildInfo = ServerBuildInfo.buildInfo();
			String version = buildInfo.minecraftVersionId();
			PaperNMS<?> versionAdapter = switch (version) {
				case "1.20.6" -> new PaperNMS_1_20_R4();
				case "1.21", "1.21.1" -> new PaperNMS_1_21_R1();
				case "1.21.2", "1.21.3" -> new PaperNMS_1_21_R2();
				case "1.21.4" -> new PaperNMS_1_21_R3();
				case "1.21.5" -> new PaperNMS_1_21_R4();
				case "1.21.6", "1.21.7", "1.21.8" -> new PaperNMS_1_21_R5();
				case "1.21.9", "1.21.10" -> new PaperNMS_1_21_R6();
				default -> null;
			};
			if (versionAdapter != null) {
				return new LoadContext(
					new CommandAPIPaper<>(internalPaperConfig, new APITypeProvider(versionAdapter)),
					() -> CommandAPI.logNormal("Loaded version " + CommandAPI.getPlatformMessage(versionAdapter))
				);
			}
			if (internalPaperConfig.fallbackToLatestNMS()) {
				PaperNMS<?> paperNMS = new PaperNMS_1_21_R6();
				return new LoadContext(
					new CommandAPIPaper<>(internalPaperConfig, new APITypeProvider(paperNMS)),
					() -> {
						CommandAPI.logNormal("Loaded version " + CommandAPI.getPlatformMessage(paperNMS));
						CommandAPI.logWarning("Loading the CommandAPI with the latest and potentially incompatible NMS implementation.");
						CommandAPI.logWarning("While you may find success with this, further updates might be necessary to fully support the version you are using.");
					}
				);
			}
			version = buildInfo.asString(ServerBuildInfo.StringRepresentation.VERSION_SIMPLE);
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
