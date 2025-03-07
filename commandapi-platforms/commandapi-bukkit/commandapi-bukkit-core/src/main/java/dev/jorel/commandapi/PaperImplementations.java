package dev.jorel.commandapi;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.jorel.commandapi.commandnodes.DifferentClientNode;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class PaperImplementations<Source> implements Listener {

	private final boolean isPaperPresent;
	private final boolean isFoliaPresent;
	private final boolean isPaperBrigAPI;
	private final CommandAPIBukkit<Source> nmsInstance;
	private final Class<? extends CommandSender> feedbackForwardingCommandSender;

	/**
	 * Constructs a PaperImplementations object
	 * 
	 * @param isPaperPresent Whether this is a Paper server or not
	 * @param isFoliaPresent Whether this is a Folia server or not
	 * @param nmsInstance    The instance of NMS
	 */
	@SuppressWarnings("unchecked")
	public PaperImplementations(boolean isPaperPresent, boolean isFoliaPresent, CommandAPIBukkit<Source> nmsInstance) {
		this.isPaperPresent = isPaperPresent;
		this.isFoliaPresent = isFoliaPresent;
		this.nmsInstance = nmsInstance;
		
		Class<? extends CommandSender> tempFeedbackForwardingCommandSender = null;
		try {
			tempFeedbackForwardingCommandSender = (Class<? extends CommandSender>) Class.forName("io.papermc.paper.commands.FeedbackForwardingSender");
		} catch (ClassNotFoundException e) {
			// uhh...
		}
		
		this.feedbackForwardingCommandSender = tempFeedbackForwardingCommandSender;

		boolean paperCommandSourceStackPresent;
		try {
			Class.forName("io.papermc.paper.command.brigadier.CommandSourceStack");
			paperCommandSourceStackPresent = true;
		} catch (ClassNotFoundException e) {
			paperCommandSourceStackPresent = false;
		}
		this.isPaperBrigAPI = paperCommandSourceStackPresent;
	}

	/**
	 * Registers paper-specific events (if paper is present), including:
	 * <ul>
	 *     <li>{@link #onServerReloadResources(ServerResourcesReloadedEvent)}</li>
	 *     <li>{@link #onCommandsSentToPlayer(AsyncPlayerSendCommandsEvent)}</li>
	 * </ul>
	 *
	 * @param plugin the plugin that the CommandAPI is being used from
	 */
	public void registerEvents(Plugin plugin) {
		if (!isPaperPresent) {
			CommandAPI.logNormal("Did not hook into Paper events");
			return;
		}
		CommandAPI.logNormal("Hooking into Paper events");
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * Automatically calls the CommandAPI's internal datapack
	 * reloading function when {@code /minecraft:reload} is called.
	 */
	@EventHandler
	public void onServerReloadResources(ServerResourcesReloadedEvent event) {
		// This event is called after Paper is done with everything command related
		// which means we can put commands back
		nmsInstance.getCommandRegistrationStrategy().preReloadDataPacks();

		// Normally, the reloadDataPacks() method is responsible for updating commands for
		// online players. If, however, datapacks aren't supposed to be reloaded upon /minecraft:reload
		// we have to do this manually here. This won't have any effect on Spigot and Paper version prior to
		// paper-1.20.6-65
		if (!CommandAPIBukkit.getConfiguration().shouldHookPaperReload()) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.updateCommands();
			}
			return;
		}
		CommandAPI.logNormal("/minecraft:reload detected. Reloading CommandAPI commands!");
		nmsInstance.reloadDataPacks();
	}

	@EventHandler
	@SuppressWarnings("UnstableApiUsage") // This event is marked @Experimental, but it's been here since 1.15-ish
	public void onCommandsSentToPlayer(AsyncPlayerSendCommandsEvent<?> event) {
		// This event fires twice, once async then sync
		//  We only want to do this once, and it is safe to run async
		if (!event.isAsynchronous()) return;

		// Rewrite nodes to their client-side version when commands are sent to a client
		Source source = nmsInstance.getBrigadierSourceFromCommandSender(event.getPlayer());
		DifferentClientNode.rewriteAllChildren(source, (RootCommandNode<Source>) event.getCommandNode(), false);
	}

	/**
	 * @return Bukkit's {@link CommandMap}
	 */
	public CommandMap getCommandMap() {
		if (isPaperPresent) {
			return Bukkit.getServer().getCommandMap();
		} else {
			return nmsInstance.getSimpleCommandMap();
		}
	}
	
	/**
	 * @return whether we're using paper or not
	 */
	public boolean isPaperPresent() {
		return this.isPaperPresent;
	}

	/**
	 * @return whether we're running a Paper server with the Paper Brigadier command API
	 */
	public boolean isPaperBrigAPI() {
		return this.isPaperBrigAPI;
	}
	
	/**
	 * @return whether we're using folia or not
	 */
	public boolean isFoliaPresent() {
		return this.isFoliaPresent;
	}
	
	/**
	 * @return a class reference pointing to {@code io.papermc.paper.commands.FeedbackForwardingSender}
	 */
	public Class<? extends CommandSender> getFeedbackForwardingCommandSender() {
		return this.feedbackForwardingCommandSender;
	}

	/**
	 * Builds a {@link WrapperCommandSyntaxException} from a message with colour codes like {@link ChatColor} or using the § symbol.
	 *
	 * @param message the error message to be displayed
	 * @return A {@link WrapperCommandSyntaxException} with the given message as error message
	 */
	public WrapperCommandSyntaxException getExceptionFromString(String message) {
		if (isPaperPresent) {
			// I don't know why, but if you set this to an Object first, then cast it to a Component,
			// running this code is totally fine on a Spigot server. If you don't do this (e.g. set
			// it to a Component or inline this), for some reason Java throws a stronk at runtime.
			// For your sanity and the sanity of whoever has to maintain this in the future, please
			// DO NOT try to simplify this statement:
			final Object adventureComponent = LegacyComponentSerializer.legacySection().deserialize(message);
			return new WrapperCommandSyntaxException(new SimpleCommandExceptionType(BukkitTooltip.messageFromAdventureComponent((Component) adventureComponent)).create());
		} else {
			return new WrapperCommandSyntaxException(new SimpleCommandExceptionType(BukkitTooltip.messageFromBaseComponents(TextComponent.fromLegacyText(message))).create());
		}
	}	

}
