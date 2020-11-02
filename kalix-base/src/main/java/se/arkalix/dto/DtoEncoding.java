package se.arkalix.dto;

/**
 * Contains names of all encoding implementations that are provided by any
 * Kalix annotation processor.
 * <p>
 * The contained names are suitable as arguments to the {@link
 * DtoReadableAs @DtoReadableAs} and {@link DtoWritableAs @DtoWritableAs}
 * annotations, given that an annotation processing facility is available for
 * generating DTO classes for the names.
 *
 * @see se.arkalix.dto
 */
public final class DtoEncoding {
    private DtoEncoding() {}

    /**
     * JavaScript Object Notation (JSON).
     *
     * This variable is suitable as arguments to the {@link
     * DtoReadableAs @DtoReadableAs} and {@link DtoWritableAs @DtoWritableAs}
     * annotations.
     *
     * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
     */
    public static final String JSON = "se.arkalix.dto.json";
}
