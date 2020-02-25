package eu.arrowhead.kalix.example.dto;

import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.Writable;

import java.util.Optional;

@Writable
@Readable
public interface Shape {
    Point position();
    ShapeType type();
    Optional<String> name();
}
