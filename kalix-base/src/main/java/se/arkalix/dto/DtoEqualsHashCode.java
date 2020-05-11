package se.arkalix.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signifies that a generated DTO class should have generated {@link
 * Object#hashCode()} and {@link Object#equals(Object)} implementations.
 * <p>
 * This annotation does nothing unless provided to an interface that is also
 * annotated with either {@link DtoReadableAs @DtoReadableAs} or {@link
 * DtoWritableAs @DtoWritableAs}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DtoEqualsHashCode {}
