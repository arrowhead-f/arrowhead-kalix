package eu.arrowhead.kalix.dto;

import java.nio.ByteBuffer;

public interface WritableDTO {
    interface JSON extends WritableDTO {
        void encodeJSON(final ByteBuffer target) throws WriteException;
    }
}
