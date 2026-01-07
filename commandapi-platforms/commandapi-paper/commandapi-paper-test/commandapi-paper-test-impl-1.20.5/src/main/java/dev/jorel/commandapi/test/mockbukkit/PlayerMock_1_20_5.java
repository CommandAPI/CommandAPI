package dev.jorel.commandapi.test.mockbukkit;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.jetbrains.annotations.Nullable;

public class PlayerMock_1_20_5 extends PlayerMock implements CommandAPIPlayerMock {
	public PlayerMock_1_20_5(PlayerMock original) {
		super(original.getServer(), original.getName(), original.getUniqueId());
	}

	@Override
	public @Nullable String nextMessage() {
		return super.nextMessage();
	}

	@Override
	public void updateCommands() {
		// Nothing to do
	}
}
