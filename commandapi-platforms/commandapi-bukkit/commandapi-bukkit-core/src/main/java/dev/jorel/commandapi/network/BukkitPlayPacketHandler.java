package dev.jorel.commandapi.network;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.network.packets.UpdateRequirementsPacket;
import org.bukkit.entity.Player;

/**
 * A {@link PlayPacketHandler} for handling {@link CommandAPIPacket}s sent to Bukkit by {@link Player} connections.
 */
public class BukkitPlayPacketHandler implements PlayPacketHandler<Player> {
	@Override
	public void handleUpdateRequirementsPacket(Player sender, UpdateRequirementsPacket packet) {
		CommandAPI.updateRequirements(sender);
	}
}
