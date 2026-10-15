package dev.jorel.commandapi.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import dev.jorel.commandapi.SafeVarHandle;
import dev.jorel.commandapi.nms.NMS;
import dev.jorel.commandapi.wrappers.ParticleData;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MockPlatform<CLW> implements NMS<CLW> {
	/*****************
	 * Instantiation *
	 *****************/

	private static MockPlatform<?> instance = null;

	@SuppressWarnings("unchecked")
	public static <CLW> MockPlatform<CLW> getInstance() {
		return (MockPlatform<CLW>) instance;
	}

	protected MockPlatform() {
		if (MockPlatform.instance == null) {
			MockPlatform.instance = this;
		} else {
			// wtf why was this called twice?
		}
	}
	
	public static void unload() {
		MockPlatform.instance = null;
	}

	/************************************
	 * CommandAPIBukkit implementations *
	 ************************************/

	private final CommandDispatcher<CLW> brigadierDispatcher = new CommandDispatcher<>();
	private final CommandDispatcher<CLW> resourcesDispatcher = new CommandDispatcher<>();

	public CommandDispatcher<CLW> getMockBrigadierDispatcher() {
		return brigadierDispatcher;
	}

	public CommandDispatcher<CLW> getMockResourcesDispatcher() {
		return resourcesDispatcher;
	}

	@Override
	public final String convert(ItemStack is) {
		throw new UnimplementedError();
	}

	@Override
	public final String convert(ParticleData<?> particle) {
		throw new UnimplementedError();
	}

	@Override
	public final String convert(PotionEffectType potion) {
		throw new UnimplementedError();
	}

	@Override
	public final String convert(Sound sound) {
		throw new UnimplementedError();
	}

	/******************
	 * Helper methods *
	 ******************/

	public static Object getField(Class<?> className, String fieldName, Object instance) {
		return getField(className, fieldName, fieldName, instance);
	}

	public static Object getField(Class<?> className, String fieldName, String mojangMappedName, Object instance) {
		try {
			Field field = className.getDeclaredField(SafeVarHandle.USING_MOJANG_MAPPINGS ? mojangMappedName : fieldName);
			field.setAccessible(true);
			return field.get(instance);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	public static void setField(Class<?> className, String fieldName, Object instance, Object value) {
		setField(className, fieldName, fieldName, instance, value);
	}

	public static void setField(Class<?> className, String fieldName, String mojangMappedName, Object instance, Object value) {
		try {
			Field field = className.getDeclaredField(SafeVarHandle.USING_MOJANG_MAPPINGS ? mojangMappedName : fieldName);
			field.setAccessible(true);
			field.set(instance, value);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	public static <T> T getFieldAs(Class<?> className, String fieldName, Object instance, Class<T> asType) {
		return getFieldAs(className, fieldName, fieldName, instance, asType);
	}

	public static <T> T getFieldAs(Class<?> className, String fieldName, String mojangMappedName, Object instance, Class<T> asType) {
		try {
			Field field = className.getDeclaredField(SafeVarHandle.USING_MOJANG_MAPPINGS ? mojangMappedName : fieldName);
			field.setAccessible(true);
			return asType.cast(field.get(instance));
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> T forceGetArgument(CommandContext cmdCtx, String key) {
		Map<String, ParsedArgument> result = getFieldAs(CommandContext.class, "arguments", cmdCtx, Map.class);
		return result == null ? null : (T) result.get(key).getResult();
	}

	/***************
	 * Other stuff *
	 ***************/

	public abstract ItemFactory getItemFactory();

	public abstract org.bukkit.advancement.Advancement addAdvancement(NamespacedKey key);
	
	public abstract void addFunction(NamespacedKey key, List<String> commands);
	public abstract void addTag(NamespacedKey key, List<List<String>> commands);

	public abstract Player setupMockedCraftPlayer(String name);

	/**
	 * Converts 1.16.5 and below potion effect names to NamespacedKey names. For
	 * example, converts "effect.minecraft.speed" into "minecraft:speed"
	 * 
	 * @param potionEffectType the potion effect to get the namespaced key for
	 * @return a Minecraft namespaced key name for a potion effect
	 */
	public abstract String getBukkitPotionEffectTypeName(PotionEffectType potionEffectType);
	
	public abstract String getNMSParticleNameFromBukkit(Particle particle);
	
	// Overrideable
	public int popFunctionCallbackResult() {
		throw new IllegalStateException("Pop function callback result hasn't been overridden");
	}
	
	static record Pair<A, B>(A first, B second) {}
	
	/**
	 * Gets recipes from {@code data/minecraft/recipe/<file>.json}. Parses them and
	 * returns a list of {@code {name, json}}, where {@code name} is the name of the
	 * file without the {@code .json} extension, and {@code json} is the parsed JSON
	 * result from the file
	 * 
	 * @param minecraftServerClass an instance of MinecraftServer.class
	 * @return A list of pairs of resource locations (with no namespace) and JSON objects
	 */
	public final List<Pair<String, JsonObject>> getRecipes(Class<?> minecraftServerClass) {
		final String recipeDirectory = "data/minecraft/recipe/";
		List<Pair<String, JsonObject>> list = new ArrayList<>();
		// Get the server jar file
		try(JarFile jar = new JarFile(minecraftServerClass.getProtectionDomain().getCodeSource().getLocation().getPath())) {
			// Iterate over everything in the jar 
			jar.entries().asIterator().forEachRemaining(entry -> {
				if(entry.getName().startsWith(recipeDirectory) && entry.getName().endsWith(".json")) {
					// If it's what we want, read everything
					InputStream is = minecraftServerClass.getClassLoader().getResourceAsStream(entry.getName());
					String jsonStr = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
					// Get the resource location (file name, no extension, no path) and parse the JSON
					JsonObject parsedJson = JsonParser.parseString(jsonStr).getAsJsonObject();
					list.add(new Pair<>(entry.getName().substring(recipeDirectory.length(), entry.getName().lastIndexOf(".")), parsedJson));
				}
			});
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load any recipes for testing!", e);
		}
		
		return list;
	}
	
	@SuppressWarnings("serial")
	private static class UnimplementedError extends Error {
		public UnimplementedError() {
			super("Unimplemented");
		}
	}

	/***********************
	 * Bukkit "enum" lists *
	 ***********************/

	public abstract org.bukkit.enchantments.Enchantment[] getEnchantments();

	public abstract org.bukkit.entity.EntityType[] getEntityTypes();

	public abstract org.bukkit.loot.LootTables[] getLootTables();

	public abstract PotionEffectType[] getPotionEffects();
	
	public abstract Sound[] getSounds();
	
	public abstract org.bukkit.block.Biome[] getBiomes();

	/**
	 * @return A list of all item names, sorted in alphabetical order. Each item
	 * is prefixed with {@code minecraft:}
	 */
	public abstract List<String> getAllItemNames();
	
	public abstract List<NamespacedKey> getAllRecipes();

	/**
	 * @return the recipes that a {@code RecipeArgument} accepts. Minecraft doesn't accept its special recipes
	 * (like banner duplication), even though they're suggested.
	 */
	public abstract List<NamespacedKey> getNonSpecialRecipes();
	
	/**********
	 * Runtime object registries (enchantments, potions etc.)
	 ********/
	
	public Map<Class<?>, Map<NamespacedKey, Object>> registry = null;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T extends Keyed> void addToRegistry(Class<T> className, NamespacedKey key, T object) {
		if (registry == null) {
			registry = new HashMap<>();
		}
		
		if (registry.containsKey(className)) {
			registry.get(className).put(key, object);
		} else {
			Map<NamespacedKey, T> registryMap = new HashMap<>();
			registryMap.put(key, object);
			registry.put(className, (Map) registryMap);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T extends Keyed> Registry<T> getRegistry(Class<T> className) {
		return new Registry() {
			@Nullable
			public T get(@NotNull NamespacedKey key) {
				return (T) registry.get(className).get(key);
			}
			
			@NotNull
			public Stream<T> stream() {
				return (Stream) registry.get(className).values().stream();
			}

			public Iterator<T> iterator() {
				return (Iterator) registry.get(className).values().iterator();
			}
		};
	}

}
