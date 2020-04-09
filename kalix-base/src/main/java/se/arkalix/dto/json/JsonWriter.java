package se.arkalix.dto.json;

import se.arkalix.dto.DtoWritable;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.dto.DtoWriter;
import se.arkalix.dto.binary.BinaryWriter;

import java.util.List;

public class JsonWriter implements DtoWriter {
    private static final JsonWriter INSTANCE = new JsonWriter();

    private JsonWriter() {}

    public static JsonWriter instance() {
        return INSTANCE;
    }

    @Override
    public <U extends DtoWritable> void writeOne(final U value, final BinaryWriter target) throws DtoWriteException {
        if (!(value instanceof JsonWritable)) {
            throw jsonNotSupportedBy(value.getClass());
        }
        ((JsonWritable) value).writeJson(target);
    }

    @Override
    public <U extends DtoWritable> void writeMany(final List<U> values, final BinaryWriter target) throws DtoWriteException {
        if (!values.isEmpty() && !(values.get(0) instanceof JsonWritable)) {
            throw jsonNotSupportedBy(values.get(0).getClass());
        }
        target.write((byte) '[');
        final var t1 = values.size();
        for (var t0 = 0; t0 < t1; ++t0) {
            if (t0 != 0) {
                target.write((byte) ',');
            }
            ((JsonWritable) values.get(t0)).writeJson(target);
        }
        target.write((byte) ']');
    }

    private static RuntimeException jsonNotSupportedBy(final Class<?> class_) {
        return new UnsupportedOperationException("\"" + class_ + "\" does " +
            "not implement JsonWritable; if the class was produced by the " +
            "DTO code generator, this is likely caused by its input " +
            "interface not having DtoEncoding.JSON as argument to its " +
            "@DtoWritableAs annotation");
    }
}
