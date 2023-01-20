package dev.jorel.commandapi.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.wrappers.ParticleData;

public abstract class MockPlatform<CLW> extends CommandAPIBukkit<CLW> {

	/*****************
	 * Instantiation *
	 *****************/

	private static MockPlatform<?> instance = null;

	@SuppressWarnings("unchecked")
	public static <CLW> MockPlatform<CLW> getInstance() {
		return (MockPlatform<CLW>) instance;
	}

	public MockPlatform() {
		if (instance == null) {
			instance = this;
		} else {
			// wtf why was this called twice?
		}
	}

	/************************************
	 * CommandAPIBukkit implementations *
	 ************************************/

	private CommandDispatcher<CLW> dispatcher = null;

	@Override
	public final CommandDispatcher<CLW> getBrigadierDispatcher() {
		if (this.dispatcher == null) {
			this.dispatcher = new CommandDispatcher<>();
		}
		return this.dispatcher;
	}

	@Override
	public final BukkitCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<CLW> cmdCtx, boolean forceNative) {
		return getCommandSenderFromCommandSource(cmdCtx.getSource());
	}

	@Override
	public final void resendPackets(Player player) {
		return; // There's nothing to do here, we can't "send packets to players"
	}

	@Override
	public final void addToHelpMap(Map<String, HelpTopic> helpTopicsToAdd) {
		throw new Error("unimplemented");
	}

	@Override
	public final String convert(ItemStack is) {
		throw new Error("unimplemented");
	}

	@Override
	public final String convert(ParticleData<?> particle) {
		throw new Error("unimplemented");
	}

	@Override
	public final String convert(PotionEffectType potion) {
		throw new Error("unimplemented");
	}

	@Override
	public final String convert(Sound sound) {
		throw new Error("unimplemented");
	}

	@Override
	public final HelpTopic generateHelpTopic(String commandName, String shortDescription, String fullDescription, String permission) {
		throw new Error("unimplemented");
	}

	@Override
	public final boolean isVanillaCommandWrapper(Command command) {
		throw new Error("unimplemented");
	}

	@Override
	public final void reloadDataPacks() {
		throw new Error("unimplemented");
	}

	/******************
	 * Helper methods *
	 ******************/

	public static Object getField(Class<?> className, String fieldName, Object instance) {
		try {
			Field field = className.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(instance);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	public static void setField(Class<?> className, String fieldName, Object instance, Object value) {
		try {
			Field field = className.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(instance, value);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFieldAs(Class<?> className, String fieldName, Object instance, Class<T> asType) {
		try {
			Field field = className.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(instance);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	/***************
	 * Other stuff *
	 ***************/

	public abstract ItemFactory getItemFactory();

	public abstract org.bukkit.advancement.Advancement addAdvancement(NamespacedKey key);

	/**
	 * Converts 1.16.5 and below potion effect names to NamespacedKey names. For
	 * example, converts "effect.minecraft.speed" into "minecraft:speed"
	 * 
	 * @param potionEffectType the potion effect to get the namespaced key for
	 * @return a Minecraft namespaced key name for a potion effect
	 */
	public abstract String getBukkitPotionEffectTypeName(PotionEffectType potionEffectType);
	
	static record Pair<A, B>(A first, B second) {};
	
	/**
	 * Gets recipes from {@code data/minecraft/recipes/<file>.json}. Parses them and
	 * returns a list of {@code {name, json}}, where {@code name} is the name of the
	 * file without the {@code .json} extension, and {@code json} is the parsed JSON
	 * result from the file
	 * 
	 * @param minecraftServerClass an instance of MinecraftServer.class
	 * @return A list of pairs of resource locations (with no namespace) and JSON objects
	 */
	public final List<Pair<String, JsonObject>> getRecipes(Class<?> minecraftServerClass) {
		List<Pair<String, JsonObject>> list = new ArrayList<>();
		// Get the spigot-x.x.x-Rx.x-SNAPSHOT.jar file
		JarFile jar = null;
		try {
			jar = new JarFile(minecraftServerClass.getProtectionDomain().getCodeSource().getLocation().getPath());
		} catch (IOException e) {
			e.printStackTrace();
			return list;
		}
		// Iterate over everything in the jar 
		jar.entries().asIterator().forEachRemaining(entry -> {
			if(entry.getName().startsWith("data/minecraft/recipes/") && entry.getName().endsWith(".json")) {
				// If it's what we want, read everything
				InputStream is = minecraftServerClass.getClassLoader().getResourceAsStream(entry.getName());
				String jsonStr = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
				// Get the resource location (file name, no extension, no path) and parse the JSON.
				list.add(new Pair<>(entry.getName().substring("data/minecraft/recipes/".length(), entry.getName().lastIndexOf(".")), JsonParser.parseString(jsonStr).getAsJsonObject()));
			}
		});
		return list;
	}

	/***********************
	 * Bukkit "enum" lists *
	 ***********************/

	public abstract org.bukkit.enchantments.Enchantment[] getEnchantments();

	public abstract org.bukkit.entity.EntityType[] getEntityTypes();

	public abstract org.bukkit.loot.LootTables[] getLootTables();

	public abstract org.bukkit.potion.PotionEffectType[] getPotionEffects();
	
	public abstract org.bukkit.Sound[] getSounds();

	/**
	 * @return A list of all item names, sorted in alphabetical order. Each item
	 * is prefixed with {@code minecraft:}
	 */
	public abstract List<String> getAllItemNames();
	
	public List<NamespacedKey> getAllRecipes() {return List.of();}

}