package eu.arrowhead.kalix.dto;

import com.squareup.javapoet.TypeSpec;

public class DTOUtilitySpecification {
    private final String packageName;
    private final TypeSpec typeSpec;

    public DTOUtilitySpecification(final String packageName, final TypeSpec typeSpec) {
        this.packageName = packageName;
        this.typeSpec = typeSpec;
    }

    public String packageName() {
        return packageName;
    }

    public TypeSpec typeSpec() {
        return typeSpec;
    }
}
