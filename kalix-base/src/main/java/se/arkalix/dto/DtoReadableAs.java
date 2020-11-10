package se.arkalix.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used on a {@link se.arkalix.dto DTO interface} to signify that the DTO class
 * generated from that interface should be readable from certain codecs.
 */
@Target(ElementType.TYPE)
public @interface DtoReadableAs {
    /**
     * Names codecs from which {@link se.arkalix.dto DTO class} instances
     * should be readable.
     * <p>
     * See {@link DtoCodec} for a description of what codec names may be
     * used here.
     */
    DtoCodec[] value();
}
