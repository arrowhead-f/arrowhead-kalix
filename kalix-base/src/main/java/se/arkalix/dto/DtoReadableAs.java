package se.arkalix.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used on a {@link se.arkalix.dto DTO interface} to signify that the DTO class
 * generated from that interface should be readable from certain encodings.
 * <p>
 * Each value provided to this interface must be the canonical name of a class
 * implementing the {@link DtoReader} interface. Most typically, each such
 * reader will provide a public static final variable containing such a name.
 */
@Target(ElementType.TYPE)
public @interface DtoReadableAs {
    /**
     * Names encodings from which {@link se.arkalix.dto DTO class} instances
     * should be readable.
     */
    String[] value();
}
