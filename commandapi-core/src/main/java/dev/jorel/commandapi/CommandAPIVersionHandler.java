package dev.jorel.commandapi;

/**
 * This file handles loading the correct platform implementation. The CommandAPIVersionHandler
 * file within the commandapi-core module is NOT used at run time. Instead, the platform modules
 * replace this class with their own version that handles loads the correct class for their version.
 */
public abstract class CommandAPIVersionHandler {

	/**
	 * Returns an instance of the version's implementation of CommandAPIPlatform.
	 *
	 * @return an instance of CommandAPIPlatform which can run on the currently active server
	 */
	static LoadContext getPlatform(CommandAPIConfig<?> config) {
		throw new IllegalStateException("You have the wrong copy of the CommandAPI! If you're shading, did you use commandapi-core instead of commandapi-{platform}-shade?");
	}

}
