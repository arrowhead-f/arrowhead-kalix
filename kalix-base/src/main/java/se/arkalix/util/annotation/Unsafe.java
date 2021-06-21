package se.arkalix.util.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Signifies that whatever is annotated is subject to special usage
 * restrictions that are not enforced by either compile-time or runtime checks.
 * <p>
 * Make sure to read the documentation of this entity carefully.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Unsafe {}
