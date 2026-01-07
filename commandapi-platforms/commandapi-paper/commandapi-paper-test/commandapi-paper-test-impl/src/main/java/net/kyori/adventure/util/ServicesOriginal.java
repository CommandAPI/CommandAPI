package net.kyori.adventure.util;

import net.kyori.adventure.internal.properties.AdventureProperties;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// Copied from net.kyori.adventure.util.Services
//  Paper uses that class to dynamically load the server
//  implementation of certain interfaces. MockBukkit uses
//  this dynamic behavior to conveniently insert their
//  own implementations. However, this code gets mad if
//  multiple implementations are available, which they
//  are due to us merging the Paper server and MockBukkit
//  environments. To get around this, we override the usual
//  implementation of Services and hijack how problematic
//  classes get loaded.
public class ServicesOriginal {
	private static final boolean SERVICE_LOAD_FAILURES_ARE_FATAL;

	public static <P> @NotNull Optional<P> service(final @NotNull Class<P> type) {
		ServiceLoader<P> loader = Services0.loader(type);
		Iterator<P> it = loader.iterator();

		while(true) {
			if (it.hasNext()) {
				P instance;
				try {
					instance = (P)it.next();
				} catch (Throwable t) {
					if (!SERVICE_LOAD_FAILURES_ARE_FATAL) {
						continue;
					}

					throw new IllegalStateException("Encountered an exception loading service " + type, t);
				}

				if (it.hasNext()) {
					// Why not just load the first implementation :(
					throw new IllegalStateException("Expected to find one service " + type + ", found multiple");
				}

				return Optional.of(instance);
			}

			return Optional.empty();
		}
	}

	public static <P> @NotNull Optional<P> serviceWithFallback(final @NotNull Class<P> type) {
		ServiceLoader<P> loader = Services0.loader(type);
		Iterator<P> it = loader.iterator();
		P firstFallback = null;

		while(it.hasNext()) {
			P instance;
			try {
				instance = (P)it.next();
			} catch (Throwable t) {
				if (!SERVICE_LOAD_FAILURES_ARE_FATAL) {
					continue;
				}

				throw new IllegalStateException("Encountered an exception loading service " + type, t);
			}

			if (!(instance instanceof Services.Fallback)) {
				return Optional.of(instance);
			}

			if (firstFallback == null) {
				firstFallback = instance;
			}
		}

		return Optional.ofNullable(firstFallback);
	}

	public static <P> Set<P> services(final Class<? extends P> clazz) {
		ServiceLoader<? extends P> loader = Services0.loader(clazz);
		Set<P> providers = new HashSet();

		for(P instance : loader) {
			try {
				;
			} catch (ServiceConfigurationError ex) {
				if (!SERVICE_LOAD_FAILURES_ARE_FATAL) {
					continue;
				}

				throw new IllegalStateException("Encountered an exception loading a provider for " + clazz + ": ", ex);
			}

			providers.add(instance);
		}

		return Collections.unmodifiableSet(providers);
	}

	static {
		SERVICE_LOAD_FAILURES_ARE_FATAL = Boolean.TRUE.equals(AdventureProperties.SERVICE_LOAD_FAILURES_ARE_FATAL.value());
	}
}

