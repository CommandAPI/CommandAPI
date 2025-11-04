package dev.jorel.commandapi.network;

import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.network.packets.SetVersionPacket;
import org.bukkit.entity.Player;

/**
 * A {@link HandshakePacketHandler} for handling {@link CommandAPIPacket}s sent to Bukkit by {@link Player} connections.
 */
public class BukkitHandshakePacketHandler implements HandshakePacketHandler<Player> {
	@Override
	public void handleSetVersionPacket(Player sender, SetVersionPacket packet) {
		CommandAPIBukkit.get().getMessenger().setProtocolVersion(sender, packet.protocolVersion());
	}
}
