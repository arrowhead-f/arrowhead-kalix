package se.arkalix.dto.json;

import se.arkalix.dto.*;
import se.arkalix.dto._internal.DtoReaders;
import se.arkalix.dto._internal.UncheckedDtoException;
import se.arkalix.encoding.binary.BinaryReader;
import se.arkalix.encoding.binary.BinaryWriter;
import se.arkalix.dto.json._internal.JsonTokenBuffer;
import se.arkalix.dto.json._internal.JsonTokenizer;
import se.arkalix.dto.json.value.JsonType;
import se.arkalix.encoding.Encoding;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link DtoReader} and {@link DtoWriter} for reading and writing JSON data.
 * <p>
 * Use the {@link #instance()} method to get access to the singleton instance
 * of this class.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
public class JsonEncoding implements DtoReader, DtoWriter {
    private static final byte[] EMPTY_ARRAY = new byte[]{'[', ']'};

    private static final Map<Class<? extends DtoReadable>, Method> readMethods = new ConcurrentHashMap<>();
    private static final JsonEncoding instance = new JsonEncoding();

    private JsonEncoding() {}

    /**
     * @return Reference to JSON encoding singleton instance.
     */
    public static JsonEncoding instance() {
        return instance;
    }

    @Override
    public Encoding encoding() {
        return Encoding.JSON;
    }

    @Override
    public <T extends DtoReadable> T readOne(final Class<T> class_, final BinaryReader source)
        throws DtoReadException
    {
        final var buffer = JsonTokenizer.tokenize(source);
        final var value = DtoReaders.invokeReadMethod(class_, getReadJsonMethod(class_), buffer);
        if (!buffer.atEnd()) {
            final var next = buffer.next();
            throw new DtoReadUnexpectedToken(class_, this, "Expected " +
                "end of data, found", next.readStringRaw(source), next.begin());
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
            throw new DtoReadUnexpectedToken(class_, this, "" +
                "expected array of encoded instances of target class, found",
                next.readStringRaw(source), next.begin());
        }

        var n = next.nChildren();
        while (n-- != 0) {
            objects.add(DtoReaders.invokeReadMethod(class_, readJson, buffer));
        }

        if (!buffer.atEnd()) {
            next = buffer.next();
            throw new DtoReadUnexpectedToken(class_, this, "" +
                "expected end of data, found",
                next.readStringRaw(source), next.begin());
        }

        return objects;
    }

    private <T extends DtoReadable> Method getReadJsonMethod(final Class<T> class_) throws DtoReadException {
        try {
            return readMethods.computeIfAbsent(class_, ignored0 -> {
                try {
                    return class_.getDeclaredMethod("readJson", JsonTokenBuffer.class);
                }
                catch (final NoSuchMethodException exception) {
                    throw new UncheckedDtoException(new DtoReaderNotImplemented(this, class_, JsonReadable.class));
                }
            });
        }
        catch (final UncheckedDtoException exception) {
            throw (DtoReadException) exception.unwrap();
        }
    }

    @Override
    public <U extends DtoWritable> void writeOne(final U value, final BinaryWriter target) throws DtoWriteException {
        if (!(value instanceof JsonWritable)) {
            throw new DtoWriterNotImplemented(this, value, JsonWritable.class);
        }
        ((JsonWritable) value).writeJson(target);
    }

    @Override
    public <U extends DtoWritable> void writeMany(final List<U> values, final BinaryWriter target)
        throws DtoWriteException
    {
        if (values == null || values.isEmpty()) {
            target.write(EMPTY_ARRAY);
            return;
        }
        if (!(values.get(0) instanceof JsonWritable)) {
            throw new DtoWriterNotImplemented(this, values.get(0), JsonWritable.class);
        }
        target.write((byte) '[');
        final var t1 = values.size();
        ((JsonWritable) values.get(0)).writeJson(target);
        for (var t0 = 1; t0 < t1; ++t0) {
            target.write((byte) ',');
            ((JsonWritable) values.get(t0)).writeJson(target);
        }
        target.write((byte) ']');
    }
}
