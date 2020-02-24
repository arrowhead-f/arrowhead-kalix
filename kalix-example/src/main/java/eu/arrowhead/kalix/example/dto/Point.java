package eu.arrowhead.kalix.example.dto;

import eu.arrowhead.kalix.util.io.Decodable;
import eu.arrowhead.kalix.util.io.Encodable;

/**
 * A point in 2D space.
 */
@Decodable("json")
@Encodable("json")
public interface Point {
    /**
     * @return X-coordinate.
     */
    int x();

    /**
     * @param newX Desired new X-coordiante.
     */
    void x(int newX);

    /**
     * @return Y-coordinate.
     */
    int y();
}
