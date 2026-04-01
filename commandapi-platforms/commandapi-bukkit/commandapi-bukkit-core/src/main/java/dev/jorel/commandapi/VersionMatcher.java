package dev.jorel.commandapi;

interface VersionMatcher {

	static String getRelevantVersion(String version) {
		String[] parts = version.split("\\.");
		if (parts[0].equals("1")) {
			return version;
		}
		return parts[0] + "." + parts[1];
	}

}
