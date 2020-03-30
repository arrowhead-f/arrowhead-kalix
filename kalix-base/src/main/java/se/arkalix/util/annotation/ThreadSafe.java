package se.arkalix.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signifies that the annotated method in question is thread-safe, and, as a
 * consequence, may be safely accessed concurrently and in parallel by multiple
 * threads.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface ThreadSafe {}
