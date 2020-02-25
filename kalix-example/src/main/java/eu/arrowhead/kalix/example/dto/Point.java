package eu.arrowhead.kalix.example.dto;

import eu.arrowhead.kalix.dto.*;
import eu.arrowhead.kalix.dto.Readable;

/**
 * A point in 2D space.
 */
@Writable
@Readable
public interface Point {
    /**
     * @return X-coordinate.
     */
    @NameJSON("_x")
    Integer x();

    /**
     * @return Y-coordinate, if any.
     */
    int y();
}
