package se.arkalix.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Signifies that the annotated type in question can only be read from or
 * written to the specified {@link DtoCodec codec}.
 */
@Target(ElementType.TYPE)
public @interface DtoExclusive {
    /**
     * The DTO codec the annotated type is exclusive to.
     * <p>
     * See {@link DtoCodec} for a description of what codec names may be
     * used here.
     */
    DtoCodec value();
}
