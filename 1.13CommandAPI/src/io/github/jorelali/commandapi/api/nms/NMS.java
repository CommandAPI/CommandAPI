package io.github.jorelali.commandapi.api.nms;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.jorelali.commandapi.api.FunctionWrapper;
import io.github.jorelali.commandapi.api.arguments.CustomProvidedArgument.SuggestionProviders;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument.EntitySelector;
import io.github.jorelali.commandapi.api.arguments.LocationArgument.LocationType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;

public interface NMS {

	//Returns the world in which a command sender is from
	default World getCommandSenderWorld(CommandSender sender) {
		if(sender instanceof BlockCommandSender) {
			return ((BlockCommandSender) sender).getBlock().getWorld();
		} else if(sender instanceof ProxiedCommandSender) {
			return getCommandSenderWorld(((ProxiedCommandSender) sender).getCallee());
		} else if(sender instanceof Entity) {
			return ((Entity) sender).getWorld();
		} else {
			return null;
		}
	}
	
	/**
	 * A String array of Minecraft versions that this NMS implementation
	 * is compatible with. For example, ["1.14", "1.14.1", "1.14.2", "1.14.3"]
	 * @return A String array of compatible Minecraft versions
	 */
	public String[] compatibleVersions();
	
	/**
	 * Creates a JSON file that describes the hierarchical structure of
	 * the commands that have been registered by the server.
	 * @param server The NMS MinecraftServer instance
	 * @param file The JSON file to write to
	 * @param dispatcher The Brigadier CommandDispatcher
	 * @throws IOException When the file fails to be written to
	 */
	public void createDispatcherFile(Object server, File file, CommandDispatcher<?> dispatcher) throws IOException;
	
	/**
	 * Retrieve a specific NMS implemented SuggestionProvider
	 * @param provider The SuggestionProvider type to retrieve
	 * @return A SuggestionProvider that matches the SuggestionProviders input
	 */
	public SuggestionProvider<?> getSuggestionProvider(SuggestionProviders provider);
	
	/**
	 * Retrieves a CommandSender, given some CommandContext. This
	 * method should handle Proxied CommandSenders for entities
	 * if a Proxy is being used.
	 * @param cmdCtx The CommandContext for a given command
	 * @return A CommandSender instance (such as a ProxiedNativeCommandSender or Player)
	 */
	public CommandSender getSenderForCommand(CommandContext<?> cmdCtx);
	
	/**
	 * Returns a CommandSender of a given CommandListenerWrapper object
	 * @param clw The CommandListenerWrapper object
	 * @return A CommandSender (not proxied) from the command listener wrapper
	 */
	public CommandSender getCommandSenderForCLW(Object clw);

	/**
	 * Given the MinecraftServer instance, returns the Brigadier
	 * CommandDispatcher from the NMS CommandDispatcher
	 * @param server The NMS MinecraftServer instance
	 * @return A Brigadier CommandDispatcher
	 */
	public CommandDispatcher<?> getBrigadierDispatcher(Object server);

	/**
	 * Checks if a Command is an instance of the OBC VanillaCommandWrapper
	 * @param command The Command to check
	 * @return true if Command is an instance of VanillaCommandWrapper
	 */
	public boolean isVanillaCommandWrapper(Command command);
	
	/**
	 * Returns the Server's internal (OBC) CommandMap
	 * @return A SimpleCommandMap from the OBC server
	 */
	public SimpleCommandMap getSimpleCommandMap();
	
	//Argument implementations
	public ChatColor 			getChatColor(CommandContext<?> cmdCtx, String str);
	public BaseComponent[] 		getChatComponent(CommandContext<?> cmdCtx, String str);
	public Enchantment 			getEnchantment(CommandContext<?> cmdCtx, String str);
	public ItemStack 			getItemStack(CommandContext<?> cmdCtx, String str) throws CommandSyntaxException;
	public Location 			getLocation(CommandContext<?> cmdCtx, String str, LocationType locationType, CommandSender sender) throws CommandSyntaxException;
	public Particle 			getParticle(CommandContext<?> cmdCtx, String str);
	public PotionEffectType 	getPotionEffect(CommandContext<?> cmdCtx, String str) throws CommandSyntaxException;
	public FunctionWrapper[] 	getFunction(CommandContext<?> cmdCtx, String str) throws CommandSyntaxException;
	public Player 				getPlayer(CommandContext<?> cmdCtx, String str) throws CommandSyntaxException;
	public Object 				getEntitySelector(CommandContext<?> cmdCtx, String str, EntitySelector selector) throws CommandSyntaxException;
	public EntityType 			getEntityType(CommandContext<?> cmdCtx, String str, CommandSender sender) throws CommandSyntaxException;
	public LootTable 			getLootTable(CommandContext<?> cmdCtx, String str);
	public Sound                getSound(CommandContext<?> cmdCtx, String key);
	public Advancement          getAdvancement(CommandContext<?> cmdCtx, String key) throws CommandSyntaxException;
	public Recipe               getRecipe(CommandContext<?> cmdCtx, String key) throws CommandSyntaxException;

	//Argument types
	public ArgumentType<?> _ArgumentChatFormat();
	public ArgumentType<?> _ArgumentChatComponent();
	public ArgumentType<?> _ArgumentMinecraftKeyRegistered();
	public ArgumentType<?> _ArgumentMobEffect();
	public ArgumentType<?> _ArgumentProfile();
	public ArgumentType<?> _ArgumentParticle();
	public ArgumentType<?> _ArgumentPosition();
	public ArgumentType<?> _ArgumentVec3();
	public ArgumentType<?> _ArgumentItemStack();
	public ArgumentType<?> _ArgumentTag();
	public ArgumentType<?> _ArgumentEntitySummon();
	public ArgumentType<?> _ArgumentEntity(EntitySelector selector);
	public ArgumentType<?> _ArgumentEnchantment();



}
