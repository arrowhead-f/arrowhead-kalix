package se.arkalix.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on a {@link se.arkalix.dto DTO interface} to signify that the DTO class
 * generated from that interface should be readable from certain encodings.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DtoReadableAs {
    /**
     * Encodings from which {@link se.arkalix.dto DTO class} instances should
     * be readable.
     */
    DtoEncoding[] value();
}
