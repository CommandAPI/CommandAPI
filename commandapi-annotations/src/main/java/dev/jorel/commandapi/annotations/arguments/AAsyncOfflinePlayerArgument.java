package dev.jorel.commandapi.annotations.arguments;

import dev.jorel.commandapi.arguments.AsyncOfflinePlayerArgument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation equivalent of the {@link AsyncOfflinePlayerArgument}
 */
@Primitive("java.util.concurrent.CompletableFuture<org.bukkit.OfflinePlayer>")
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface AAsyncOfflinePlayerArgument {
}
