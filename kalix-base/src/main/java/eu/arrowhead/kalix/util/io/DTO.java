package eu.arrowhead.kalix.util.io;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.ByteBuffer;

public interface DTO {
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    @interface Decodable {
        Format[] value() default {Format.JSON};
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    @interface Encodable {
        Format[] value() default {Format.JSON};
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.METHOD)
    @interface Optional {}

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.METHOD})
    @interface NameJSON {
        String value();
    }

    enum Format {
        JSON
    }

    interface DecodableJSON {}

    interface EncodableJSON {
        void encodeJson(final ByteBuffer target);
    }
}
