package dev.jorel.commandapi.test;

/**
 * This file handles loading the correct test implementation. The TestVersionHandler file
 * within the commandapi-bukkit-test-impl module is NOT used at run time. Instead, the
 * test-impl modules replace this class with their own version that loads the correct class.
 */
public abstract class TestVersionHandler {

	/**
	 * @return An implementation of {@link MockPlatform} that works for the current test version.
	 */
	public static MockPlatform<?> getMockPlatform() {
		throw new IllegalStateException("Wrong version of TestVersionHandler loaded! " +
			"Make sure you are using commandapi-bukkit-test-impl-[version] instead of commandapi-bukkit-test-impl");
	}
}
