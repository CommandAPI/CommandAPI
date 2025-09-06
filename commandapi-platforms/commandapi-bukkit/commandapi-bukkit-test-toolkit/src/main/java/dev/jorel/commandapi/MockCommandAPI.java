package dev.jorel.commandapi;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.spying.CommandAPIHandlerSpy;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;

/**
 * Additional logic that helps the CommandAPI run in a MockBukkit testing environment.
 * Does not rely on any version-specific Minecraft code to (ideally) support testing in any version.
 */
public class MockCommandAPI {
	// Static instance
	private static MockCommandAPI instance;
	private final CommandAPIBukkit<MockCommandSource> commandAPIBukkit;

	protected MockCommandAPI(CommandAPIBukkit<MockCommandSource> commandAPIBukkit) {
		MockCommandAPI.instance = this;

		this.commandAPIBukkit = commandAPIBukkit;
	}

	/**
	 * @return The {@link MockCommandAPI} instance currently loaded.
	 */
	public static MockCommandAPI getInstance() {
		return instance;
	}

	/**
	 * @return The implementation of {@link CommandAPIBukkit} which is currently loaded. This is the same object
	 * as is returned by {@link CommandAPIBukkit#get()}, but explicitly using the {@link MockCommandSource} class.
	 */
	public CommandAPIBukkit<MockCommandSource> getCommandAPIBukkit() {
		return commandAPIBukkit;
	}

	// Reflection helpers

	/**
	 * Sets a field inside a target object to the given value. Ignores private access or final status.
	 *
	 * @param targetClass The {@link Class} that contains the field.
	 * @param fieldName   The name of the field to change.
	 * @param target      The object whose field will be changed. If the field is static, this is ignored and can be null.
	 * @param value       The new value for the field.
	 * @param <Target>    The type of the target object.
	 * @throws IllegalArgumentException If the field cannot be set.
	 */
	public static <Target> void setField(Class<? super Target> targetClass, String fieldName, Target target, Object value) {
		try {
			Field field = targetClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("Reflection failed", e);
		}
	}

	// References to utility classes
	private CommandAPIHandlerSpy commandAPIHandlerSpy;
	private MockCommandRegistrationStrategy commandRegistrationStrategy;

	public void onLoad() {
		// Intercept calls to CommandAPIHandler
		commandAPIHandlerSpy = new CommandAPIHandlerSpy(CommandAPIHandler.getInstance());
		CommandAPIHandler<Argument<?>, CommandSender, MockCommandSource> commandAPIHandler = commandAPIHandlerSpy.spyHandler();
		setField(CommandAPIHandler.class, "instance", null, commandAPIHandler);

		// Setup objects
		commandRegistrationStrategy = new MockCommandRegistrationStrategy(commandAPIHandler);
	}

	/**
	 * @return The {@link CommandAPIHandlerSpy} object intercepting calls to {@link CommandAPIHandler} methods.
	 */
	public CommandAPIHandlerSpy getCommandAPIHandlerSpy() {
		return commandAPIHandlerSpy;
	}

	/**
	 * @return The {@link MockCommandRegistrationStrategy} handling Brigadier command logic.
	 */
	public MockCommandRegistrationStrategy getCommandRegistrationStrategy() {
		return commandRegistrationStrategy;
	}

	// Logging
	/**
	 * A global toggle for whether the default logger returned by {@link CommandAPIBukkit#getLogger()} should print
	 * messages to the console. This is {@code false} by default, so not messages will appear. If you don't provide
	 * your own logger using {@link CommandAPI#setLogger(CommandAPILogger)} and set this to {@code true} before calling
	 * {@link CommandAPI#onLoad(CommandAPIConfig)}, then the CommandAPI will write messages into the test log.
	 */
	public static boolean ENABLE_LOGGING = false;
}
