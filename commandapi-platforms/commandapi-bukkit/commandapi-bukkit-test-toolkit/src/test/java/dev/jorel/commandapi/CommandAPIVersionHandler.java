package dev.jorel.commandapi;

public abstract class CommandAPIVersionHandler {
	static LoadContext getPlatform() {
		return new LoadContext(new MockCommandAPIBukkit());
	}
}
