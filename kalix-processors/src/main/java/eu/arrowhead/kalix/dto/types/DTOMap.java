package eu.arrowhead.kalix.dto.types;

import eu.arrowhead.kalix.dto.Format;

import javax.lang.model.type.DeclaredType;

public class DTOMap implements DTOType {
    private final DeclaredType type;
    private final DTOType key;
    private final DTOType value;

    public DTOMap(final DeclaredType type, final DTOType key, final DTOType value) {
        assert !key.isCollection() && !(key instanceof DTOInterface);

        this.type = type;
        this.key = key;
        this.value = value;
    }

    public DTOType key() {
        return key;
    }

    public DTOType value() {
        return value;
    }

    @Override
    public String name() {
        return "Map<" + key.name() + ", " + value.name() + ">";
    }

    @Override
    public DeclaredType asTypeMirror() {
        return type;
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public boolean isReadable(final Format format) {
        if (format == Format.JSON) {
            if (!(key instanceof DTOEnum || key instanceof DTOString)) {
                return false;
            }
        }
        else if (!key.isReadable(format)) {
            return false;
        }
        return value.isReadable(format);
    }

    @Override
    public boolean isWritable(final Format format) {
        if (format == Format.JSON) {
            if (!(key instanceof DTOEnum || key instanceof DTOString)) {
                return false;
            }
        }
        else if (!key.isWritable(format)) {
            return false;
        }
        return value.isWritable(format);
    }
}