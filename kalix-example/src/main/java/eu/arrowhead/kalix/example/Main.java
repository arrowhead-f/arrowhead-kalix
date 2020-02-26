package eu.arrowhead.kalix.example;

import eu.arrowhead.kalix.dto.WriteException;
import eu.arrowhead.kalix.example.dto.PointBuilder;
import eu.arrowhead.kalix.example.dto.ShapeBuilder;
import eu.arrowhead.kalix.example.dto.ShapeType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(final String[] args) throws WriteException {
        System.out.println("Hello, Example!");

        final var map = new HashMap<String, Map<String, Short>>();

        final var map0 = new HashMap<String, Short>();
        map0.put("hejsan", (short) 123);
        map0.put("ojsan", (short) -127);
        map.put("hej", map0);

        final var map1 = new HashMap<String, Short>();
        map.put("tomt", map1);

        final var shape = new ShapeBuilder()
            .name("MyShape")
            .position(new PointBuilder()
                .x(1423e134)
                .y(352234.123432e-142)
                .build())
            .attributes(Arrays.asList(
                Arrays.asList((byte) 1, (byte) 2),
                Arrays.asList((byte) 123, (byte) 0, (byte) -5)))
            .attributes2(new int[]{1, 2, 3, 4, 5, 6, 7})
            .properties(map)
            .type(ShapeType.TRIANGLE)
            .build();

        final var byteBuffer = ByteBuffer.allocate(4096);
        shape.encodeJSON(byteBuffer);
        final var text = new String(byteBuffer.array(), 0, byteBuffer.position(), StandardCharsets.UTF_8);

        System.out.println(text);
    }
}
