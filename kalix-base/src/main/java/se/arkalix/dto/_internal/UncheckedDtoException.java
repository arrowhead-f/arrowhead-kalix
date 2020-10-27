package se.arkalix.dto._internal;

import se.arkalix.dto.DtoException;
import se.arkalix.util.annotation.Internal;

@Internal
public class UncheckedDtoException extends RuntimeException {
    private final DtoException inner;

    public UncheckedDtoException(final DtoException inner) {
        super(null, null, false, false);
        this.inner = inner;
    }

    public DtoException unwrap() {
        return inner;
    }
}
