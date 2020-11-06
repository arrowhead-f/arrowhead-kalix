package se.arkalix.dto;

import java.util.Objects;
import java.util.Optional;

public enum DtoEncodingSpec {
    JSON(DtoEncoding.JSON, "decodeJson", "encodeJson");

    private final String dtoName;
    private final String decoderMethodName;
    private final String encoderMethodName;

    DtoEncodingSpec(
        final String dtoName,
        final String decoderMethodName,
        final String encoderMethodName
    ) {
        this.dtoName = Objects.requireNonNull(dtoName, "dtoName");
        this.decoderMethodName = Objects.requireNonNull(decoderMethodName, "decoderMethodName");
        this.encoderMethodName = Objects.requireNonNull(encoderMethodName, "encoderMethodName");
    }

    public String dtoName() {
        return dtoName;
    }

    public String decoderMethodName() {
        return decoderMethodName;
    }

    public String encoderMethodName() {
        return encoderMethodName;
    }

    public static Optional<DtoEncodingSpec> getByDtoName(final String dtoName) {
        for (final var encodingSpec : DtoEncodingSpec.values()) {
            if (encodingSpec.dtoName.equals(dtoName)) {
                return Optional.of(encodingSpec);
            }
        }
        return Optional.empty();
    }
}
