package dev.jorel.commandapi;

import dev.jorel.commandapi.commandsenders.*;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

/**
 * An implementation of {@link CommandAPIBukkit} that is compatible with a MockBukkit testing environment.
 * Does not rely on any version-specific Minecraft code to (ideally) support testing in any version.
 */
public class MockCommandAPIBukkit extends CommandAPIBukkit<MockCommandSource> {
	// Instance
	private final MockCommandAPI mockCommandAPI;

	public MockCommandAPIBukkit(MockInternalBukkitConfig config) {
		this.nms = new MockNMS();
		this.mockCommandAPI = new MockCommandAPI(this);

		CommandAPIBukkit.config = config;
	}

	// Config
	public static MockInternalBukkitConfig getConfiguration() {
		return (MockInternalBukkitConfig) CommandAPIBukkit.getConfiguration();
	}

	// Load
	@Override
	public void onLoad() {
		mockCommandAPI.onLoad();
		super.onLoad();
	}

	// Enable
	@Override
	public void onEnable() {
		super.plugin = getConfiguration().getPlugin();
		super.stopCommandRegistrations();
	}

	// Test environment-specific
	@Override
	public CommandMap getCommandMap() {
		return Bukkit.getCommandMap();
	}

	@Override
	public CommandRegistrationStrategy<MockCommandSource> createCommandRegistrationStrategy() {
		return mockCommandAPI.getCommandRegistrationStrategy();
	}

	public static boolean ENABLE_LOGGING = false;

	@Override
	public CommandAPILogger getLogger() {
		return ENABLE_LOGGING ?
			super.getLogger() :
			CommandAPILogger.bindToMethods(msg -> {}, msg -> {}, msg -> {}, (msg, ex) -> {});
	}

	// This logic copied from CommandAPISpigot
	@Override
	public BukkitCommandSender<? extends CommandSender> wrapCommandSender(CommandSender sender) {
		if (sender instanceof BlockCommandSender block) {
			return new BukkitBlockCommandSender(block);
		}
		if (sender instanceof ConsoleCommandSender console) {
			return new BukkitConsoleCommandSender(console);
		}
		if (sender instanceof Player player) {
			return new BukkitPlayer(player);
		}
		if (sender instanceof org.bukkit.entity.Entity entity) {
			return new BukkitEntity(entity);
		}
		if (sender instanceof NativeProxyCommandSender nativeProxy) {
			return new BukkitNativeProxyCommandSender(nativeProxy);
		}
		if (sender instanceof ProxiedCommandSender proxy) {
			return new BukkitProxiedCommandSender(proxy);
		}
		if (sender instanceof RemoteConsoleCommandSender remote) {
			return new BukkitRemoteConsoleCommandSender(remote);
		}
		throw new RuntimeException("Failed to wrap CommandSender " + sender + " to a CommandAPI-compatible BukkitCommandSender");
	}

	@Override
	public Platform activePlatform() {
		return Platform.SPIGOT;
	}
}
