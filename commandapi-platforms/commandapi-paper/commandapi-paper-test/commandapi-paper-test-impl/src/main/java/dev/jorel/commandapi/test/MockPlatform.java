package dev.jorel.commandapi.test;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import dev.jorel.commandapi.CommandAPIPaper;
import dev.jorel.commandapi.InternalPaperConfig;
import dev.jorel.commandapi.SafeVarHandle;
import dev.jorel.commandapi.nms.APITypeProvider;
import dev.jorel.commandapi.nms.PaperNMS;
import dev.jorel.commandapi.test.mockbukkit.CommandAPIServerMock;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Handles loading the CommandAPI in a mock server environment.
 */
public abstract class MockPlatform implements Enums {
	// Instance management
	private static final MockPlatform instance = TestVersionHandler.getMockPlatform();

	public static MockPlatform getInstance() {
		return instance;
	}

	// Getters
	public abstract @NotNull CommandAPIServerMock<?> getServer();

	public abstract @NotNull JavaPlugin getPlugin();

	// Server stop and start
	public abstract <T extends JavaPlugin> void startServer(Class<T> pluginClass);

	public CommandAPIPaper<?> createPlatform(InternalPaperConfig config) {
		return new CommandAPIPaper<>(config, new APITypeProvider(getVersionAdapter()));
	}

	protected abstract PaperNMS<?> getVersionAdapter();

	public abstract void enableServer();

	public abstract void stopServer();

	// Other

	/**
	 * @return A list of all item names, sorted in alphabetical order. Each item
	 * is prefixed with {@code minecraft:}
	 */
	public abstract List<String> getAllItemNames();

	public abstract List<NamespacedKey> getAllRecipes();

	// Reflection
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

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <T> T forceGetArgument(CommandContext cmdCtx, String key) {
		Map<String, ParsedArgument> result = getFieldAs(CommandContext.class, "arguments", cmdCtx, Map.class);
		return result == null ? null : (T) result.get(key).getResult();
	}
}
