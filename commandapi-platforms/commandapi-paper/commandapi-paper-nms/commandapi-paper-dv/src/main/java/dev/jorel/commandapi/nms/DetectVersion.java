package dev.jorel.commandapi.nms;

import io.papermc.paper.ServerBuildInfo;
import net.minecraft.SharedConstants;

public class DetectVersion {

	private DetectVersion() {}

	public static String getVersion() {
		try {
			return ServerBuildInfo.buildInfo().minecraftVersionId();
		} catch (Throwable t) {
			// We don't even care what the exception was, we just know that if it throws, we are on a version that doesn't have
			// the ServerBuildInfo so we can fall back to internals which are all the same on versions on and before 1.20.6
			return SharedConstants.getCurrentVersion().getId();
		}
	}

}
