package dev.jorel.commandapi.nms;

import net.minecraft.SharedConstants;

public class DetectVersion {

	private DetectVersion() {}

	public static String getVersion() {
		return SharedConstants.getCurrentVersion().getId();
	}

}
