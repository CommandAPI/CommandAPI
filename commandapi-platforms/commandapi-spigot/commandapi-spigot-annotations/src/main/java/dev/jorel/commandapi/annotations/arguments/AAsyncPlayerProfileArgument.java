package dev.jorel.commandapi.annotations.arguments;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation equivalent of the {@link dev.jorel.commandapi.arguments.AsyncPlayerProfileArgument}
 */
@Primitive("java.util.concurrent.CompletableFuture<java.util.List<org.bukkit.profile.PlayerProfile>>")
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface AAsyncPlayerProfileArgument {
}