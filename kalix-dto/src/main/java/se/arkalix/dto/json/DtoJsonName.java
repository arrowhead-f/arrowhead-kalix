package se.arkalix.dto.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Allows overriding the name of a {@link se.arkalix.dto DTO interface} field
 * when encoding or decoding it to/from JSON.
 * <p>
 * For example, given the following interface:
 * <pre>
 *      &#64;DtoReadableAs(JSON)
 *      &#64;DtoWritableAs(JSON)
 *      public interface Rectangle {
 *          &#64;DtoJsonName("w")
 *          double width();
 *
 *          &#64;DtoJsonName("h")
 *          double height();
 *      }
 * </pre>
 * Assuming it was instantiated with the width and height 100 and 400, it then
 * reads and writes as follows:
 * <pre>
 *     {"w":100,"h":400}
 * </pre>
 * Without the &#64;DtoJsonName annotation, the same object would be
 * represented as
 * <pre>
 *     {"width":100,"height":400}
 * </pre>
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
@Target(ElementType.METHOD)
public @interface DtoJsonName {
    String value();
}
