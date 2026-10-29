package dev.jorel.commandapi.test.network;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.network.*;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NetworkTestBase extends TestBase {

	/*********
	 * Setup *
	 *********/
	protected ArgumentCaptor<byte[]> rawBytesCapture;
	protected ArgumentCaptor<CommandAPIPacket> packetCapture;

	public void setUp() {
		// Load and enable
		super.setUp();
		assertDoesNotThrow(() -> server.getScheduler().performOneTick());

		// Mock messenger
		BukkitCommandAPIMessenger spyMessenger = spy(CommandAPIBukkit.get().getMessenger());

		rawBytesCapture = ArgumentCaptor.forClass(byte[].class);
		doCallRealMethod().when(spyMessenger).sendRawBytes(any(), any(), rawBytesCapture.capture());

		// Mock packetHandlers
		BukkitPacketHandlerProvider provider = MockPlatform.getFieldAs(
			CommandAPIMessenger.class, "packetHandlerProvider", spyMessenger, BukkitPacketHandlerProvider.class
		);
		assert provider != null;

		packetCapture = ArgumentCaptor.forClass(CommandAPIPacket.class);

		BukkitHandshakePacketHandler handshakeSpy = spy(provider.getHandshakePacketHandler());
		doCallRealMethod().when(handshakeSpy).handlePacket(any(), packetCapture.capture());

		BukkitPlayPacketHandler playSpy = spy(provider.getPlayPacketHandler());
		doCallRealMethod().when(playSpy).handlePacket(any(), packetCapture.capture());

		// Insert spies
		MockPlatform.setField(BukkitPacketHandlerProvider.class, "handshakePacketHandler", provider, handshakeSpy);
		MockPlatform.setField(BukkitPacketHandlerProvider.class, "playPacketHandler", provider, playSpy);

		MockPlatform.setField(CommandAPIBukkit.class, "messenger", CommandAPIBukkit.get(), spyMessenger);
	}

	public void tearDown() {
		super.tearDown();
	}

	/***************************************************
	 * Utility methods for sending and receiving bytes *
	 ***************************************************/

	public PlayerMock getPluginMessagingPlayer(String name) {
		PlayerMock player = spy(new PlayerMock(server, name));

		// PlayerMock#sendPluginMessage is not implemented on versions 1.16 or 1.17
		//  Easiest to always override the call
		doAnswer(invocation -> {
			Plugin source = invocation.getArgument(0);
			String channel = invocation.getArgument(1);
			byte[] message = invocation.getArgument(2);
			StandardMessenger.validatePluginMessage(server.getMessenger(), source, channel, message);
			return null;
		}).when(player).sendPluginMessage(any(), any(), any());

		return player;
	}

	public byte[] getSentBytes(Player target, CommandAPIPacket packet) {
		CommandAPIBukkit.get().getMessenger().sendPacket(target, packet);
		try {
			return rawBytesCapture.getValue();
		} catch (MockitoException ignored) {
			// Exception may be thrown if argument was not captured because the method was not called
			// In that case, return null to signal no bytes were sent
			return null;
		}
	}

	public CommandAPIPacket getHandledPacket(Player source, String channel, byte[] message) {
		CommandAPIBukkit.get().getMessenger().onPluginMessageReceived(channel, source, message);
		try {
			return packetCapture.getValue();
		} catch (MockitoException ignored) {
			// Exception may be thrown if argument was not captured because the method was not called
			// In that case, return null to signal no packet was handled
			return null;
		}
	}

	public void assertPacketEncodesAndDecodes(Player player, CommandAPIPacket packet, byte[] bytes) {
		CommandAPIProtocol protocol = CommandAPIProtocol.getProtocolForPacket(packet.getClass());
		assertNotNull(protocol, "Packet class \"" + packet.getClass().getSimpleName() + "\" " +
			"is not registered in the CommandAPIProtocol! This packet cannot be sent or received.");

		assertArrayEquals(bytes, getSentBytes(player, packet));
		assertEquals(packet, getHandledPacket(player, protocol.getChannelIdentifier(), bytes));
	}

	protected void setProtocolVersion(Player player, int protocolVersion) {
		CommandAPIBukkit.get().getMessenger().setProtocolVersion(player, protocolVersion);
	}
}
