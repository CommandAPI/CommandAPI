package dev.jorel.commandapi.test.network;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.exceptions.ProtocolVersionTooOldException;
import dev.jorel.commandapi.network.packets.UpdateRequirementsPacket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UpdateRequirementsPacketTests extends NetworkTestBase {

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

	/*********
	 * Tests *
	 *********/

	@Test
	void sendReceiveTestWithUpdateRequirementsPacket() {
		PlayerMock player = getPluginMessagingPlayer("player");
		// Interrupt normal calls to updateCommands, because MockPlayer throws an UnimplementedOperationException
		//  getPluginMessagingPlayer already returns a mocked spy, so we don't have to spy ourselves
		Mockito.doNothing().when(player).updateCommands();

		// Protocol version currently 0

		// Error when sending packet to target that has not declared they understand CommandAPI plugin messages
		assertThrowsWithMessage(
			ProtocolVersionTooOldException.class,
			"Tried to send a packet to " + player + ", which is using protocol version 0. " +
				"This system is using version 1. That version is too old to receive the packet, or CommandAPI networking is not enabled. " +
				"CommandAPI version 10.0.0 or greater is required to receive UpdateRequirementsPacket.",
			() -> getSentBytes(player, new UpdateRequirementsPacket())
		);

		setProtocolVersion(player, 1); // Protocol version now 1

		// Packet should be encoded as just the id since no data is included
		assertPacketEncodesAndDecodes(player,
			new UpdateRequirementsPacket(),
			new byte[]{0}
		);

		// Update commands should have been called for this player
		Mockito.verify(player, Mockito.times(1)).updateCommands();
	}
}
