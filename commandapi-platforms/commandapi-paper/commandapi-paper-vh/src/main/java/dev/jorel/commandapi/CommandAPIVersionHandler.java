package dev.jorel.commandapi;

import dev.jorel.commandapi.exceptions.UnsupportedVersionException;
import dev.jorel.commandapi.nms.APITypeProvider;
import dev.jorel.commandapi.nms.PaperNMS;
import dev.jorel.commandapi.nms.PaperNMS_1_20_R4;
import dev.jorel.commandapi.nms.PaperNMS_1_21_R1;
import dev.jorel.commandapi.nms.PaperNMS_1_21_R2;
import dev.jorel.commandapi.nms.PaperNMS_1_21_R3;
import dev.jorel.commandapi.nms.PaperNMS_1_21_R4;
import dev.jorel.commandapi.nms.PaperNMS_1_21_R5;
import dev.jorel.commandapi.nms.PaperNMS_1_21_R6;
import dev.jorel.commandapi.nms.PaperNMS_1_21_R7;
import io.papermc.paper.ServerBuildInfo;

public abstract class CommandAPIVersionHandler {
	static LoadContext getPlatform(CommandAPIConfig<?> config) {
		InternalPaperConfig internalPaperConfig;
		if (config instanceof CommandAPIPaperConfig paperConfig) {
			internalPaperConfig = new InternalPaperConfig(paperConfig);
		} else {
			throw new IllegalArgumentException("CommandAPIPaper was loaded with non-Paper config!");
		}

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
				case "1.21.11" -> new PaperNMS_1_21_R7();
				default -> null;
			};
			if (versionAdapter != null) {
				return new LoadContext(
					new CommandAPIPaper<>(internalPaperConfig, new APITypeProvider(versionAdapter)),
					() -> CommandAPI.logNormal("Loaded version " + CommandAPI.getPlatformMessage(versionAdapter))
				);
			}
			if (internalPaperConfig.fallbackToLatestNMS()) {
				PaperNMS<?> paperNMS = new PaperNMS_1_21_R7();
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
			if (error instanceof NoClassDefFoundError) {
				// Thrown when users use versions before the ServerBuildInfo was added. We should inform them
				// to update since Paper 1.20.6 was experimental then anyway
				throw new IllegalStateException("The CommandAPI doesn't support any version before Paper 1.20.6 build 79. Please update your server!", error);
			} else {
				throw error;
			}
		}
	}
}
