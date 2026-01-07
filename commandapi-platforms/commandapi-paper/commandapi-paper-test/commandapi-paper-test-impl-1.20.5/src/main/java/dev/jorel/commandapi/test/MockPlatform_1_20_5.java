package dev.jorel.commandapi.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.nms.NMS;
import dev.jorel.commandapi.nms.PaperNMS;
import dev.jorel.commandapi.nms.PaperNMS_1_20_R4;
import dev.jorel.commandapi.test.mockbukkit.CommandAPIServerMock;
import dev.jorel.commandapi.test.mockbukkit.CommandAPIServerMock_1_20_5;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class MockPlatform_1_20_5 extends MockPlatform implements Enums_1_20_5 {
	// Getters
	private CommandAPIServerMock_1_20_5 server;
	private JavaPlugin plugin;

	@Override
	public @NotNull CommandAPIServerMock<?> getServer() {
		assertNotNull(server, "Tried to access server, but it was null. Is the server currently running?");
		return server;
	}

	@Override
	public @NotNull JavaPlugin getPlugin() {
		assertNotNull(plugin, "Tried to access plugin, but it was null. Is the server currently running?");
		return plugin;
	}

	// Server stop and start
	@Override
	public <T extends JavaPlugin> void startServer(Class<T> pluginClass) {
		server = MockBukkit.mock(new CommandAPIServerMock_1_20_5());
		plugin = MockBukkit.load(pluginClass);
	}

	@Override
	protected PaperNMS<?> getVersionAdapter() {
		PaperNMS_1_20_R4 paperNMS = new PaperNMS_1_20_R4();

		// Create spy
		NMS<CommandSourceStack> bukkitNMS = Mockito.spy(paperNMS.bukkitNMS());
		MockPlatform.setField(PaperNMS_1_20_R4.class, "bukkitNMS", paperNMS, bukkitNMS);

		// Override problematic methods
		Mockito.when(bukkitNMS.getMinecraftServer()).thenAnswer(invocation -> server.getMinecraftServer());
		Mockito.doAnswer(invocation -> server.getBrigadierSourceFromCommandSender(invocation.getArgument(0)))
			.when(bukkitNMS).getBrigadierSourceFromCommandSender(any());

		return paperNMS;
	}

	@Override
	public void enableServer() {
		// Run the CommandAPI's enable tasks
		assertTrue(CommandAPI.canRegister(), "Server was already enabled! Cannot enable twice!");
		Bukkit.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
		assertDoesNotThrow(() -> server.getScheduler().performOneTick());
		assertFalse(CommandAPI.canRegister());
	}

	@Override
	public void stopServer() {
		if (server != null) {
			if (plugin != null) {
				Bukkit.getScheduler().cancelTasks(plugin);
				plugin.onDisable();
			}
			MockBukkit.unmock();
		}
		server = null;
		plugin = null;
	}

	@Override
	public List<String> getAllItemNames() {
		return StreamSupport.stream(BuiltInRegistries.ITEM.spliterator(), false)
			.map(Object::toString)
			.map(s -> "minecraft:" + s)
			.sorted()
			.toList();
	}

	@Override
	public List<NamespacedKey> getAllRecipes() {
		throw new UnsupportedOperationException();
//		return recipeManager.getRecipeIds().map(k -> new NamespacedKey(k.getNamespace(), k.getPath())).toList();
	}
}
