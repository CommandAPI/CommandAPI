package dev.jorel.commandapi.test.mockbukkit;

import be.seeseemelk.mockbukkit.MockUnsafeValues;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.PaperLifecycleEventManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.BooleanSupplier;

public class CommandAPIUnsafeValues extends MockUnsafeValues {
	@Override
	public LifecycleEventManager<Plugin> createPluginLifecycleEventManager(JavaPlugin plugin, BooleanSupplier registrationCheck) {
		// MockBukkit hasn't given this a proper implementation in this version
		return new PaperLifecycleEventManager<>(plugin, registrationCheck);
	}
}
