package dev.jorel.commandapi.test.mockbukkit;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * MockBukkit's package structure changed between versions we want to test,
 * so we can't actually use their version of this class directly. This version
 * is implemented per-version, pointing at the appropriate MockBukkit class.
 */
public interface CommandAPIPlayerMock extends Player {
	void setLocation(@NotNull Location location);

	@Nullable String nextMessage();
}
