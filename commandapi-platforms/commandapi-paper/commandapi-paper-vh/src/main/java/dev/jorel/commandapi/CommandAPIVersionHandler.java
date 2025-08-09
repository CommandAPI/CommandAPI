package dev.jorel.commandapi;

import dev.jorel.commandapi.exceptions.UnsupportedVersionException;
import dev.jorel.commandapi.nms.APITypeProvider;
import dev.jorel.commandapi.nms.BundledNMS;
import dev.jorel.commandapi.nms.PaperNMS;
import dev.jorel.commandapi.nms.PaperNMS_1_20_R4;
import dev.jorel.commandapi.nms.PaperNMS_1_21_R1;
import dev.jorel.commandapi.nms.PaperNMS_1_21_R2;
import dev.jorel.commandapi.nms.PaperNMS_1_21_R3;
import dev.jorel.commandapi.nms.PaperNMS_1_21_R4;
import dev.jorel.commandapi.nms.PaperNMS_1_21_R5;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;

import java.util.ArrayList;
import java.util.List;

public abstract class CommandAPIVersionHandler {

	static LoadContext getPlatform(InternalConfig config) {
		InternalPaperConfig paperConfig = (InternalPaperConfig) config;
		return new LoadContext(new CommandAPIPaper<>(paperConfig));
	}

	static Object getVersion() {
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
				default -> null;
			};
			if (versionAdapter != null) {
				return new VersionContext(
					new APITypeProvider(versionAdapter),
					(logger) -> {},
					(logger) -> logger.info("Loaded version " + getPlatformMessage(versionAdapter))
				);
			}
			if (CommandAPIPaper.getConfiguration().fallbackToLatestNMS()) {
				return new VersionContext(new APITypeProvider(new PaperNMS_1_21_R5()), (logger) -> {
					logger.warning("Loading the CommandAPI with the latest and potentially incompatible NMS implementation.");
					logger.warning("While you may find success with this, further updates might be necessary to fully support the version you are using.");
				});
			}
			version = buildInfo.asString(ServerBuildInfo.StringRepresentation.VERSION_SIMPLE);
			throw new UnsupportedVersionException(version);
		} catch (Throwable error) {
			if (error instanceof ClassNotFoundException cnfe) {
				// Thrown when users use versions before the ServerBuildInfo was added. We should inform them
				// to update since Paper 1.20.6 was experimental then anyway
				throw new IllegalStateException("The CommandAPI doesn't support any version before Paper 1.20.6 build 79. Please update your server!", cnfe);
			} else {
				throw error;
			}
		}
	}

	static InternalConfig getConfig(CommandAPIConfig<?> config) {
		// This should never be a casting error since only the Paper module has this version handler and the CommandAPIPaperConfig
		return new InternalPaperConfig((CommandAPIPaperConfig<? extends LifecycleEventOwner>) config);
	}

	private static String getPlatformMessage(PaperNMS<?> nms) {
		final String platformClassHierarchy;
		{
			List<String> platformClassHierarchyList = new ArrayList<>();
			Class<?> platformClass = nms.getClass();
			// Goes up through class inheritance only (ending at Object, but we don't want to include that)
			// CommandAPIPlatform is an interface, so it is not included
			while (platformClass != null && platformClass != Object.class) {
				platformClassHierarchyList.add(platformClass.getSimpleName());
				platformClass = platformClass.getSuperclass();
			}
			platformClassHierarchy = String.join(" > ", platformClassHierarchyList);
		}
		return platformClassHierarchy;
	}

}
