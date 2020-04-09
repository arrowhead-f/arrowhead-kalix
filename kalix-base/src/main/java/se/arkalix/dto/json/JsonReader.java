package se.arkalix.dto.json;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.DtoReadable;
import se.arkalix.dto.DtoReader;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.internal.dto.DtoReaders;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonReader implements DtoReader {
    private static final Map<Class<? extends DtoReadable>, Method> READ_METHODS = new ConcurrentHashMap<>();
    private static final JsonReader INSTANCE = new JsonReader();

    private JsonReader() {}

    public static JsonReader instance() {
        return INSTANCE;
    }

    @Override
    public <T extends DtoReadable> T readOne(final Class<T> class_, final BinaryReader source)
        throws DtoReadException
    {
        final var value = DtoReaders.invokeReadMethod(class_, getReadJsonMethod(class_), source);
        source.skipWhile(b -> b == '\t' || b == '\r' || b == '\n' || b == ' ');
        if (source.readableBytes() > 0) {
            throw new DtoReadException(DtoEncoding.JSON, "Expected end of data, found",
                Character.toString(source.readByte()), source.readOffset() - 1);
        }
        return value;
    }

    @Override
    public <T extends DtoReadable> List<T> readMany(final Class<T> class_, final BinaryReader source)
        throws DtoReadException
    {
        final var readJson = getReadJsonMethod(class_);
        final var objects = new ArrayList<T>();

        source.skipWhile(b -> b == '\t' || b == '\r' || b == '\n' || b == ' ');
        var next = source.readByte();
        if (next != '[') {
            throw new DtoReadException(DtoEncoding.JSON, "Expected \"[\", found", Character.toString(next),
                source.readOffset() - 1);
        }

        while (true) {
            objects.add(DtoReaders.invokeReadMethod(class_, readJson, source));
            source.skipWhile(b -> b == '\t' || b == '\r' || b == '\n' || b == ' ');
            next = source.readByte();
            if (next == ',') {
                continue;
            }
            if (next == ']') {
                break;
            }
            throw new DtoReadException(DtoEncoding.JSON, "Expected \",\" or \"]\", found",
                Character.toString(next), source.readOffset() - 1);
        }

        source.skipWhile(b -> b == '\t' || b == '\r' || b == '\n' || b == ' ');
        if (source.readableBytes() > 0) {
            throw new DtoReadException(DtoEncoding.JSON, "Expected end of data, found",
                Character.toString(source.readByte()), source.readOffset() - 1);
        }

        return objects;
    }

    private static <T extends DtoReadable> Method getReadJsonMethod(final Class<T> class_) {
        return READ_METHODS.computeIfAbsent(class_, ignored0 -> {
            try {
                return class_.getDeclaredMethod("readJson", BinaryReader.class);
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
