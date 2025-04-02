package dev.jorel.commandapi.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.SafeVarHandle;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;

/**
 * Sets up the CommandAPI for running in a mock environment.
 */
public abstract class MockPlatform<Source> implements Enums {
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
			throw new IllegalStateException("MockPlatform loaded twice? I don't think this should happen!");
		}
	}

	public static void unload() {
		MockPlatform.instance = null;
	}

	/************************
	 * CommandAPIBukkit spy *
	 ************************/

	protected CommandAPIBukkit<Source> commandAPIBukkit;

	public CommandAPIBukkit<Source> setupCommandAPIBukkit() {
		// Set up a Mockito spy
		//  We want to forward most methods to the original implementation so we
		//  can test that code, but we need to override some methods to help them
		//  play nice with MockBukkit.
		CommandAPIBukkit<Source> spy = createCommandAPIBukkitSpy();

		// General setup
		// Ignore, nothing to do here
		Mockito.doNothing().when(spy).reloadDataPacks();

		// Stub in our getMinecraftServer implementation
		Mockito.doAnswer(i -> getMinecraftServer()).when(spy).getMinecraftServer();
		// Stub in our getBrigadierSourceFromCommandSender
		//  nms expects CommandSenders to be CraftCommandSenders
		Mockito.doAnswer(i -> getBrigadierSourceFromCommandSender(i.getArgument(0)))
			.when(spy).getBrigadierSourceFromCommandSender(any());
		// Stub in our getSimpleCommandMap implementation
		//  nms throws a class cast exception  (`CraftServer` vs `CommandAPIServerMock`)
		Mockito.doAnswer(i -> getSimpleCommandMap()).when(spy).getSimpleCommandMap();
		// Stub in our getHelpMap implementation
		//  nms throws a class cast exception (`SimpleHelpMap` vs `HelpMapMock`)
		Mockito.doAnswer(i -> getHelpMap()).when(spy).getHelpMap();

		// Inject spy
		this.commandAPIBukkit = spy;
		setField(CommandAPIBukkit.class, "instance", null, spy);

		return spy;
	}

	protected abstract CommandAPIBukkit<Source> createCommandAPIBukkitSpy();

	public abstract Source getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> senderWrapper);

	public abstract <T> T getMinecraftServer();

	public abstract SimpleCommandMap getSimpleCommandMap();

	public abstract Map<String, HelpTopic> getHelpMap();

	/**************
	 * Reflection *
	 **************/

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

	/******************
	 * Helper methods *
	 ******************/

	private final CommandDispatcher<Source> brigadierDispatcher = new CommandDispatcher<>();
	private final CommandDispatcher<Source> resourcesDispatcher = new CommandDispatcher<>();

	public CommandDispatcher<Source> getMockBrigadierDispatcher() {
		return brigadierDispatcher;
	}

	public CommandDispatcher<Source> getMockResourcesDispatcher() {
		return resourcesDispatcher;
	}

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
		try(JarFile jar = new JarFile(minecraftServerClass.getProtectionDomain().getCodeSource().getLocation().getPath())) {
			// Iterate over everything in the jar 
			jar.entries().asIterator().forEachRemaining(entry -> {
				if(entry.getName().startsWith("data/minecraft/recipes/") && entry.getName().endsWith(".json")) {
					// If it's what we want, read everything
					InputStream is = minecraftServerClass.getClassLoader().getResourceAsStream(entry.getName());
					String jsonStr = new BufferedReader(new InputStreamReader(is))
						.lines()
						.map(line -> {
							// We can't load tags in the testing environment. If we have any recipes that
							// use tags as ingredients (e.g. wooden_axe or charcoal), we'll get an illegal
							// state exception from TagUtil complaining that a tag has been used before it
							// was bound. To mitigate this, we simply remove all tags and put in a dummy
							// item (in this case, stick)
							if(line.contains("\"tag\": ")) {
								return "\"item\": \"minecraft:stick\"";
							}
							return line;
						})
						.collect(Collectors.joining("\n"));
					// Get the resource location (file name, no extension, no path) and parse the JSON.
					// Using deprecated method as the alternative doesn't exist in 1.17
					@SuppressWarnings("deprecation")
					JsonObject parsedJson = new JsonParser().parse(jsonStr).getAsJsonObject();
					list.add(new Pair<>(entry.getName().substring("data/minecraft/recipes/".length(), entry.getName().lastIndexOf(".")), parsedJson));
				}
			});
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load any recipes for testing!", e);
		}
		
		return list;
	}

	/**
	 * @return A list of all item names, sorted in alphabetical order. Each item
	 * is prefixed with {@code minecraft:}
	 */
	public abstract List<String> getAllItemNames();
	
	public abstract List<NamespacedKey> getAllRecipes();
	
	/**********
	 * Runtime object registries (enchantments, potions etc.)
	 ********/
	
	public Map<Class<?>, Map<NamespacedKey, Object>> registry = new HashMap<>();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T extends Keyed> void addToRegistry(Class<T> className, NamespacedKey key, T object) {
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

			@NotNull
			public Iterator<T> iterator() {
				return (Iterator) registry.get(className).values().iterator();
			}
		};
	}
}
