package net.kyori.adventure.util;

import be.seeseemelk.mockbukkit.adventure.LegacyComponentSerializerProviderImpl;
import be.seeseemelk.mockbukkit.plugin.MockBukkitPluginLoader;
import be.seeseemelk.mockbukkit.services.ServerBuildInfoMock;
import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.PluginLoader;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public final class Services extends ServicesOriginal {
	private Services() {
	}

	private static <P> Optional<P> wrap(Object o) {
		return (Optional<P>) Optional.of(o);
	}

	public static <P> @NotNull Optional<P> service(final @NotNull Class<P> type) {
		// Classes which MockBukkit also tries to provide, which confuses the Paper server
		//  We want to use the MockBukkit implementations
		if (type.equals(ServerBuildInfo.class)) {
			return wrap(new ServerBuildInfoMock());
		}
		if (type.equals(PluginLoader.class)) {
			return wrap(new MockBukkitPluginLoader());
		}
		if (type.equals(LegacyComponentSerializer.Provider.class)) {
			return wrap(new LegacyComponentSerializerProviderImpl());
		}
		return ServicesOriginal.service(type);
	}

	public static <P> @NotNull Optional<P> serviceWithFallback(final @NotNull Class<P> type) {
		return ServicesOriginal.serviceWithFallback(type);
	}

	public static <P> Set<P> services(final Class<? extends P> clazz) {
		return ServicesOriginal.services(clazz);
	}

	public interface Fallback {
	}
}

