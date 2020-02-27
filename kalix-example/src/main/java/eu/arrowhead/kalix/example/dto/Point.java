package eu.arrowhead.kalix.example.dto;

import eu.arrowhead.kalix.dto.*;
import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.json.JsonName;

/**
 * A point in 2D space.
 */
@Writable
@Readable
public interface Point {
    /**
     * @return X-coordinate.
     */
    @JsonName("_x")
    double x();

    /**
     * @return Y-coordinate, if any.
     */
    Double y();
}
