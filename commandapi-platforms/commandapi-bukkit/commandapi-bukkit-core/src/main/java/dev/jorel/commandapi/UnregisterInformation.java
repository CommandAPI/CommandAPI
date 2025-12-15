package dev.jorel.commandapi;

public record UnregisterInformation(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit) {
}
