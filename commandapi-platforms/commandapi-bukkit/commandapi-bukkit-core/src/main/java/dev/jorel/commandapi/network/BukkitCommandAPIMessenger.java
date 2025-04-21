package dev.jorel.commandapi.network;

import dev.jorel.commandapi.network.packets.SetVersionPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

// Based on https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/
/**
 * A {@link CommandAPIMessenger} for sending and receiving messages to and from CommandAPIBukkit and {@link Player}s
 * connected to the Bukkit server.
 */
public class BukkitCommandAPIMessenger extends CommandAPIMessenger<Player, Player> implements PluginMessageListener, Listener {
	private final JavaPlugin plugin;
	private final Map<Player, Integer> protocolVersionPerPlayer;

	/**
	 * Creates a new {@link BukkitCommandAPIMessenger}.
	 *
	 * @param plugin            The plugin sending and receiving messages.
	 * @param reportFailedSends If true, {@link CommandAPIMessenger#sendPacket(Object, CommandAPIPacket)} will throw an exception
	 *                          if it cannot send the requested packet. Otherwise, if false, that method will fail silently.
	 */
	public BukkitCommandAPIMessenger(JavaPlugin plugin, boolean reportFailedSends) {
		super(new BukkitPacketHandlerProvider(), reportFailedSends);
		this.plugin = plugin;

		this.protocolVersionPerPlayer = new HashMap<>();

		// Register to listen for and send plugin messages on each channel
		Messenger messenger = Bukkit.getServer().getMessenger();
		for (String channelIdentifier : CommandAPIProtocol.getAllChannelIdentifiers()) {
			messenger.registerIncomingPluginChannel(this.plugin, channelIdentifier, this);
			messenger.registerOutgoingPluginChannel(this.plugin, channelIdentifier);
		}
		// Register to listen for player join and leave
		Bukkit.getPluginManager().registerEvents(this, this.plugin);
	}

	@EventHandler
	public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event) {
		// Run once the handshake channel is registered
		//  We can't send messages until the channel is registered, so this is the earliest this can happen
		if (!event.getChannel().equals(CommandAPIProtocol.HANDSHAKE.getChannelIdentifier())) return;

		// Send SetVersionPacket to inform player of our capabilities
		this.sendPacket(event.getPlayer(), new SetVersionPacket(CommandAPIProtocol.PROTOCOL_VERSION));
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		// Remove player from our protocol version map, so we don't keep track of useless data
		this.protocolVersionPerPlayer.remove(event.getPlayer());
	}

	@Override
	public void close() {
		// Unregister this listener
		Messenger messenger = Bukkit.getServer().getMessenger();
		for (String channelIdentifier : CommandAPIProtocol.getAllChannelIdentifiers()) {
			messenger.unregisterIncomingPluginChannel(this.plugin, channelIdentifier);
			messenger.unregisterOutgoingPluginChannel(this.plugin, channelIdentifier);
		}
		HandlerList.unregisterAll(this);
	}

	/**
	 * Sets the {@link CommandAPIProtocol#PROTOCOL_VERSION} being used by the given player. This is only intended to be
	 * called by {@link BukkitHandshakePacketHandler#handleSetVersionPacket(Player, SetVersionPacket)} when a
	 * {@link SetVersionPacket} is received.
	 *
	 * @param sender          The player that sent the {@link SetVersionPacket}.
	 * @param protocolVersion The {@link CommandAPIProtocol#PROTOCOL_VERSION} in the {@link SetVersionPacket}.
	 */
	public void setProtocolVersion(Player sender, int protocolVersion) {
		this.protocolVersionPerPlayer.put(sender, protocolVersion);
	}

	@Override
	public int getConnectionProtocolVersion(Player target) {
		return this.protocolVersionPerPlayer.getOrDefault(target, 0);
	}

	@Override
	public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
		// A plugin message was sent to Bukkit, check if it is for us
		CommandAPIProtocol protocol = CommandAPIProtocol.getProtocolForChannel(channel);
		if (protocol == null) return;

		// Handle the message
		messageReceived(protocol, player, message);
	}

	@Override
	public void sendRawBytes(CommandAPIProtocol protocol, Player target, byte[] bytes) {
		target.sendPluginMessage(this.plugin, protocol.getChannelIdentifier(), bytes);
	}
}
