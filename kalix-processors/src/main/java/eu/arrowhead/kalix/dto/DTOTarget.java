package eu.arrowhead.kalix.dto;

import eu.arrowhead.kalix.dto.types.DTOInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DTOTarget {
    private final DTOInterface interfaceType;
    private final List<DTOProperty> properties;
    private final String simpleName;
    private final String qualifiedName;

    public DTOTarget(final Builder builder) {
        interfaceType = Objects.requireNonNull(builder.interfaceType);
        properties = Objects.requireNonNull(builder.properties);
        simpleName = Objects.requireNonNull(builder.simpleName);
        qualifiedName = Objects.requireNonNull(builder.qualifiedName);
    }

    public DTOInterface interfaceType() {
        return interfaceType;
    }

    public List<DTOProperty> properties() {
        return properties;
    }

    public String simpleName() {
        return simpleName;
    }

    public String qualifiedName() {
        return qualifiedName;
    }

    public static class Builder {
        private DTOInterface interfaceType;
        private List<DTOProperty> properties = new ArrayList<>();
        private String simpleName;
        private String qualifiedName;

        public Builder interfaceType(final DTOInterface interfaceType) {
            this.interfaceType = interfaceType;
            return this;
        }

        public Builder addProperty(final DTOProperty property) {
            properties.add(property);
            return this;
        }

        public Builder simpleName(final String simpleName) {
            this.simpleName = simpleName;
            return this;
        }

        public Builder qualifiedName(final String qualifiedName) {
            this.qualifiedName = qualifiedName;
            return this;
        }

        public DTOTarget build() {
            return new DTOTarget(this);
        }
    }
}
