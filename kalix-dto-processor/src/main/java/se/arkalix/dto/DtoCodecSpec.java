package se.arkalix.dto;

import java.util.Objects;
import java.util.Optional;

public enum DtoCodecSpec {
    JSON(DtoCodec.JSON, "decodeJson", "encodeJson");

    private final DtoCodec dtoCodec;
    private final String decoderMethodName;
    private final String encoderMethodName;

    DtoCodecSpec(
        final DtoCodec dtoCodec,
        final String decoderMethodName,
        final String encoderMethodName
    ) {
        this.dtoCodec = Objects.requireNonNull(dtoCodec, "dtoName");
        this.decoderMethodName = Objects.requireNonNull(decoderMethodName, "decoderMethodName");
        this.encoderMethodName = Objects.requireNonNull(encoderMethodName, "encoderMethodName");
    }

    public DtoCodec dtoCodec() {
        return dtoCodec;
    }

    public String decoderMethodName() {
        return decoderMethodName;
    }

    public String encoderMethodName() {
        return encoderMethodName;
    }

    public static Optional<DtoCodecSpec> getByDtoCodec(final DtoCodec dtoCodec) {
        for (final var codecSpec : DtoCodecSpec.values()) {
            if (codecSpec.dtoCodec.equals(dtoCodec)) {
                return Optional.of(codecSpec);
            }
        }
        return Optional.empty();
    }
}
