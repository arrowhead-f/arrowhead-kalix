package se.arkalix.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Signifies that the annotated type in question can only be read from or
 * written to the specified {@link DtoEncoding encoding}.
 */
@Target(ElementType.TYPE)
public @interface DtoExclusive {
    /**
     * The DTO encoding the annotated type is exclusive to.
     * <p>
     * See {@link DtoEncoding} for a description of what encoding names may be
     * used here.
     */
    String value();
}
