package se.arkalix.dto.json;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.DtoReadable;
import se.arkalix.dto.DtoReader;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.json.value.JsonType;
import se.arkalix.internal.dto.DtoReaders;
import se.arkalix.internal.dto.json.JsonTokenBuffer;
import se.arkalix.internal.dto.json.JsonTokenizer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link DtoReader} for reading JSON data.
 * <p>
 * Use the {@link #instance()} method to get access to the singleton instance
 * of this class.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
public class JsonReader implements DtoReader {
    private static final Map<Class<? extends DtoReadable>, Method> READ_METHODS = new ConcurrentHashMap<>();
    private static final JsonReader INSTANCE = new JsonReader();

    private JsonReader() {}

    /**
     * @return Reference to JSON reader singleton instance.
     */
    public static JsonReader instance() {
        return INSTANCE;
    }

    @Override
    public <T extends DtoReadable> T readOne(final Class<T> class_, final BinaryReader source)
        throws DtoReadException
    {
        final var buffer = JsonTokenizer.tokenize(source);
        final var value = DtoReaders.invokeReadMethod(class_, getReadJsonMethod(class_), buffer);
        if (!buffer.atEnd()) {
            final var next = buffer.next();
            throw new DtoReadException(DtoEncoding.JSON, "Expected end of data, found",
                next.readStringRaw(source), next.begin());
        }
        return value;
    }

    @Override
    public <T extends DtoReadable> List<T> readMany(final Class<T> class_, final BinaryReader source)
        throws DtoReadException
    {
        final var buffer = JsonTokenizer.tokenize(source);
        final var readJson = getReadJsonMethod(class_);
        final var objects = new ArrayList<T>();

        var next = buffer.next();
        if (next.type() != JsonType.ARRAY) {
            throw new DtoReadException(DtoEncoding.JSON, "Expected array, found",
                next.readStringRaw(source), next.begin());
        }

        var n = next.nChildren();
        while (n-- != 0) {
            objects.add(DtoReaders.invokeReadMethod(class_, readJson, buffer));
        }

        if (!buffer.atEnd()) {
            next = buffer.next();
            throw new DtoReadException(DtoEncoding.JSON, "Expected end of data, found",
                next.readStringRaw(source), next.begin());
        }

        return objects;
    }


    private static <T extends DtoReadable> Method getReadJsonMethod(final Class<T> class_) {
        return READ_METHODS.computeIfAbsent(class_, ignored0 -> {
            try {
                return class_.getDeclaredMethod("readJson", JsonTokenBuffer.class);
            }
            catch (final NoSuchMethodException ignored1) {
                throw new UnsupportedOperationException("\"" + class_ + "\" " +
                    "does not have a public static readJson(BinaryReader) " +
                    "method; if the class was produced by the DTO code " +
                    "generator, this is likely caused by its input " +
                    "interface not having DtoEncoding.JSON as argument to its " +
                    "@DtoReadableAs annotation");
            }
        });
    }
}
