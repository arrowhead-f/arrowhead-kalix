package eu.arrowhead.kalix.example.dto;

import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.Writable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Writable
@Readable
public interface Shape {
    Point position();
    ShapeType type();
    Optional<String> name();
    Map<String, Map<String, Integer>> properties();
    List<List<BigDecimal>> attributes();
    BigInteger[] attributes2();
    boolean[] bools();
    Optional<List<String>> strangs();
}
