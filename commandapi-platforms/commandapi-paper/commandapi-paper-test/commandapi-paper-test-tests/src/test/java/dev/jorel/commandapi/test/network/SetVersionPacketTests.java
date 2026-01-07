package dev.jorel.commandapi.test.network;

import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.network.packets.SetVersionPacket;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SetVersionPacketTests extends NetworkTestBase {

	/*********
	 * Setup *
	 *********/

	@BeforeEach
	public void setUp() {
		super.setUp();
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	/*******************
	 * Utility methods *
	 *******************/

	public int getProtocolVersion(Player player) {
		return CommandAPIBukkit.get().getMessenger().getConnectionProtocolVersion(player);
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void sendReceiveTestWithSetVersionPacket() {
		Player player = addPlayer(); // Protocol version currently 0

		assertEquals(0, getProtocolVersion(player));

		// Packet is encoded as id, then a VarInt for the version inside
		//  After receiving the packet, the version recorded for the player that sent it should update
		assertPacketEncodesAndDecodes(player,
			new SetVersionPacket(1),
			new byte[]{0, 1}
		);
		assertEquals(1, getProtocolVersion(player));

		assertPacketEncodesAndDecodes(player,
			new SetVersionPacket(128),
			new byte[]{0, (byte) 0x80, 0x01}
		);
		assertEquals(128, getProtocolVersion(player));
	}
}
