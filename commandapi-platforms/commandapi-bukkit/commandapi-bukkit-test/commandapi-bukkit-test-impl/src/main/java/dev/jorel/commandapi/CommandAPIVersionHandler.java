package dev.jorel.commandapi;

import dev.jorel.commandapi.test.TestVersionHandler;

public abstract class CommandAPIVersionHandler {
	public static final String profileId = getProfileId();
	public static final boolean IS_MOJANG_MAPPED = isMojangMapped();
	
	private static String getProfileId() {
		String profileIdProperty = System.getProperty("profileId");
		if(profileIdProperty != null) {
			if ( profileIdProperty.endsWith("_Mojang")) {
				return profileIdProperty.substring(0, profileIdProperty.length() - "_Mojang".length());
			} else {
				return profileIdProperty;
			}
		} else {
			return null;
		}
	}
	
	private static boolean isMojangMapped() {
		String profileIdProperty = System.getProperty("profileId");
		if(profileIdProperty != null) {
			return profileIdProperty.endsWith("_Mojang");
		} else {
			return false;
		}
	}
	
	static LoadContext getPlatform() {
		return new LoadContext(TestVersionHandler.getMockPlatform().setupCommandAPIBukkit());
	}
	
	public static MCVersion getVersion() {
		if(profileId == null) {
			System.out.println("Using default version 1.20");
			return MCVersion.V1_20;
		} else {
			return switch(profileId) {
				case "Minecraft_1_20_5" -> MCVersion.V1_20_5;
				case "Minecraft_1_20_3" -> MCVersion.V1_20_3;
				case "Minecraft_1_20_2" -> MCVersion.V1_20_2;
				case "Minecraft_1_20" -> MCVersion.V1_20;
				default -> throw new IllegalArgumentException("Unexpected value: " + System.getProperty("profileId"));
			};
		}
	}
}
