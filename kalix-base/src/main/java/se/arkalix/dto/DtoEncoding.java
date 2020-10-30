package se.arkalix.dto;

/**
 * Contains names of all encoding implementations that are provided by a Kalix
 * package.
 * <p>
 * These names are suitable as arguments to the {@link
 * DtoReadableAs @DtoReadableAs} and {@link DtoWritableAs @DtoWritableAs}
 * annotations.
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
     * @see se.arkalix.dto.json.JsonEncoding JsonEncoding
     * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
     */
    public static final String JSON = "se.arkalix.dto.json.JsonEncoding";
}
