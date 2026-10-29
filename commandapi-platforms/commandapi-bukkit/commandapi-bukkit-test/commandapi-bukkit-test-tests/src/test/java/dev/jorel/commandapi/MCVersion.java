package dev.jorel.commandapi;

import com.github.zafarkhaja.semver.Version;

/**
 * A representation of Minecraft versions. Versions with no patch number (e.g.
 * 1.19) are represented with a patch number of 0 for semantic versioning
 */
public enum MCVersion {

	// 26
	V26_3(Version.valueOf("26.3.0"));

	private Version version;

	MCVersion(Version version) {
		this.version = version;
	}

	public boolean greaterThanOrEqualTo(MCVersion version) {
		return this.version.greaterThanOrEqualTo(version.version);
	}

	public boolean lessThanOrEqualTo(MCVersion version) {
		return this.version.lessThanOrEqualTo(version.version);
	}
	
	public boolean greaterThan(MCVersion version) {
		return this.version.greaterThan(version.version);
	}

	public boolean lessThan(MCVersion version) {
		return this.version.lessThan(version.version);
	}
}