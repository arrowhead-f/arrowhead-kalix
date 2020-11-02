package se.arkalix.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used on a {@link se.arkalix.dto DTO interface} to signify that the DTO class
 * generated from that interface should be readable from certain encodings.
 */
@Target(ElementType.TYPE)
public @interface DtoReadableAs {
    /**
     * Names encodings from which {@link se.arkalix.dto DTO class} instances
     * should be readable.
     * <p>
     * See {@link DtoEncoding} for a description of what encoding names may be
     * used here.
     */
    String[] value();
}
