package eu.arrowhead.kalix.example.dto;

import eu.arrowhead.kalix.util.io.*;

/**
 * A point in 2D space.
 */
@DTO.Decodable
@DTO.Encodable
public interface Point {
    /**
     * @return X-coordinate.
     */
    @DTO.NameJSON("_x")
    int x();

    /**
     * @return Y-coordinate.
     */
    @DTO.NameJSON("_y")
    @DTO.Optional
    int y();
}
