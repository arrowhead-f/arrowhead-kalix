package se.arkalix.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on a {@link se.arkalix.dto DTO interface} to signify that the DTO class
 * generated from that interface should be writable to certain encodings.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DtoWritableAs {
    /**
     * Encodings to which {@link se.arkalix.dto DTO class} instances should be
     * writable.
     */
    DtoEncoding[] value();
}
