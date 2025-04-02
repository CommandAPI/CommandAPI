package dev.jorel.commandapi.test;

/**
 * This file loads the test implementation for 1.20. This overrides the TestVersionHandler file
 * within the commandapi-bukkit-test-impl module.
 */
public abstract class TestVersionHandler {

	/**
	 * @return An implementation of {@link MockPlatform} that works for the current test version.
	 */
	public static MockPlatform<?> getMockPlatform() {
		return new MockPlatform_1_20();
	}
}
