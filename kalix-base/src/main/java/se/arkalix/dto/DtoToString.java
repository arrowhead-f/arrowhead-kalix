package se.arkalix.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signifies that a generated DTO class should have a generated {@link
 * Object#toString()} implementation.
 * <p>
 * This annotation does nothing unless provided to an interface that is also
 * annotated with either {@link DtoReadableAs @DtoReadableAs} or {@link
 * DtoWritableAs @DtoWritableAs}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DtoToString {}
