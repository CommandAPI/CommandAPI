package dev.jorel.commandapi.preprocessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The repeatable version of {@link Differs}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface DiffersRepeatable {
	Differs[] value();
}
