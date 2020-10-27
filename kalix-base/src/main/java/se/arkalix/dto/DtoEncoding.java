package se.arkalix.dto;

import se.arkalix.dto.json.JsonReader;
import se.arkalix.dto.json.JsonWriter;

import java.util.Objects;

/**
 * Enumerates the encodings that can be read and written by the Kalix
 * {@link se.arkalix.dto DTO} package.
 */
public enum DtoEncoding {
    /**
     * JavaScript Object Notation (JSON).
     *
     * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
     */
    JSON(JsonReader.instance(), JsonWriter.instance()),
    ;

    private final DtoReader reader;
    private final DtoWriter writer;

    DtoEncoding(final DtoReader reader, final DtoWriter writer) {
        this.reader = Objects.requireNonNull(reader, "reader");
        this.writer = Objects.requireNonNull(writer, "writer");
    }

    /**
     * Gets {@link DtoReader} useful for decoding objects encoded with this DTO
     * encoding.
     *
     * @return DTO reader.
     */
    public DtoReader reader() {
        return reader;
    }

    /**
     * Gets {@link DtoWriter} useful for encoded objects with this DTO
     * encoding.
     *
     * @return DTO writer.
     */
    public DtoWriter writer() {
        return writer;
    }
}
