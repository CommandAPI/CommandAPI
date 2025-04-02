package dev.jorel.commandapi;

import com.github.zafarkhaja.semver.Version;

/**
 * A representation of Minecraft versions. Versions with no patch number (e.g.
 * 1.19) are represented with a patch number of 0 for semantic versioning
 */
public enum MCVersion {
	
	// 1.20
	V1_20_6(Version.valueOf("1.20.6")),
	V1_20_5(Version.valueOf("1.20.5")),
	V1_20_4(Version.valueOf("1.20.4")),
	V1_20_3(Version.valueOf("1.20.3")),
	V1_20_2(Version.valueOf("1.20.2")),
	V1_20(Version.valueOf("1.20.0")), // Also covers 1.20.1

	// 1.19
	V1_19_4(Version.valueOf("1.19.4")),
	V1_19_3(Version.valueOf("1.19.3")),
	V1_19_2(Version.valueOf("1.19.2")),
	V1_19_1(Version.valueOf("1.19.1")),
	V1_19(Version.valueOf("1.19.0")),

	// 1.18
	V1_18_2(Version.valueOf("1.18.2")),
	V1_18_1(Version.valueOf("1.18.1")),
	V1_18(Version.valueOf("1.18.0")),

	// 1.17
	V1_17_1(Version.valueOf("1.17.1")),
	V1_17(Version.valueOf("1.17.0")),

	// 1.16
	V1_16_5(Version.valueOf("1.16.5")),
	V1_16_4(Version.valueOf("1.16.4")),
	V1_16_3(Version.valueOf("1.16.3")),
	V1_16_2(Version.valueOf("1.16.2")),
	V1_16_1(Version.valueOf("1.16.1")),
	V1_16(Version.valueOf("1.16.0")),

	// 1.15
	V1_15_2(Version.valueOf("1.15.2")),
	V1_15_1(Version.valueOf("1.15.1")),
	V1_15(Version.valueOf("1.15.0")),

	// 1.14
	V1_14_4(Version.valueOf("1.14.4")),
	V1_14_3(Version.valueOf("1.14.3")),
	V1_14_2(Version.valueOf("1.14.2")),
	V1_14_1(Version.valueOf("1.14.1")),
	V1_14(Version.valueOf("1.14.0")),

	// 1.13
	V1_13_2(Version.valueOf("1.13.2")),
	V1_13_1(Version.valueOf("1.13.1")),
	V1_13(Version.valueOf("1.13.0"));

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