package org.bukkit.craftbukkit;

import dev.jorel.commandapi.test.CommandAPIServerMock;

/**
 * A {@link CommandAPIServerMock} in CraftBukkit's package. The NBT API finds CraftBukkit's classes (like
 * {@code CraftItemStack}) in the package of the running server's class, which is {@code CraftServer} on a real
 * server. With MockBukkit's server, it would look for them in MockBukkit's package instead.
 * <p>
 * When it initializes, the NBT API also tries to get the {@code MinecraftServer} from {@code CraftServer}, which this
 * isn't. It logs the exception for that and carries on, which is fine for the tests.
 */
public class CraftPackageServerMock extends CommandAPIServerMock {
}
