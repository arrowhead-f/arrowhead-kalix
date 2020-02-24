package eu.arrowhead.kalix.processors.io;

import com.squareup.javapoet.*;
import eu.arrowhead.kalix.util.io.Decodable;
import eu.arrowhead.kalix.util.io.Encodable;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class DTOTarget {
    private final TypeElement interfaceElement;
    private final TypeSpec typeSpec;
    private final String packageName;

    private DTOTarget(final TypeElement interfaceElement, final TypeSpec typeSpec, final String packageName) {
        this.interfaceElement = interfaceElement;
        this.typeSpec = typeSpec;
        this.packageName = packageName;
    }

    public static DTOTarget createFrom(final Element interfaceElement) throws DTOException {
        final var ie = validateAndCastInterface(interfaceElement);

        final var getters = new ArrayList<ExecutableElement>();
        final var setters = new ArrayList<ExecutableElement>();
        collectAndValidateGettersAndSetters(ie, getters, setters);

        final var ts = createTypeSpecBuilder(ie, getters, setters);
        if (interfaceElement.getAnnotation(Decodable.class) != null) {
            specifyDecoders(ie, ts);
        }
        if (interfaceElement.getAnnotation(Encodable.class) != null) {
            specifyEncoders(ie, ts);
        }
        return new DTOTarget(ie, ts.build(), getTargetPackageName(ie));
    }

    private static TypeElement validateAndCastInterface(final Element element) throws DTOException {
        if (!(element instanceof TypeElement) ||
            element.getKind() != ElementKind.INTERFACE
        ) {
            throw new DTOException(element, "Only interfaces may be " +
                "annotated with @Decodable or @Encodable");
        }
        final var ie = (TypeElement) element;
        if (ie.getTypeParameters().size() != 0) {
            throw new DTOException(element, "@Decodable and @Encodable " +
                "interfaces may not have type parameters");
        }
        if (element.getSimpleName().toString().endsWith("DTO")) {
            throw new DTOException(element, "@Decodable and @Encodable " +
                "interfaces may not have names ending with `DTO`");
        }
        return ie;
    }

    private static void collectAndValidateGettersAndSetters(
        final TypeElement ie,
        final List<ExecutableElement> getters,
        final List<ExecutableElement> setters
    ) throws DTOException {
        final var getterNames = new HashSet<Name>();

        for (final var element : ie.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD ||
                element.getModifiers().contains(Modifier.DEFAULT)
            ) {
                continue;
            }
            final var element0 = (ExecutableElement) element;
            if (element0.getReturnType().getKind() != TypeKind.VOID &&
                element0.getParameters().size() == 0
            ) {
                getters.add(element0);
                getterNames.add(element0.getSimpleName());
                continue;
            }
            if (element0.getReturnType().getKind() == TypeKind.VOID &&
                element0.getParameters().size() == 1
            ) {
                setters.add(element0);
                continue;
            }
            throw new DTOException(ie, "@Encodable interface method " +
                "must be getter, setter, static or default");
        }

        for (final var setter : setters) {
            if (!getterNames.contains(setter.getSimpleName())) {
                throw new DTOException(setter, "@Encodable contains " +
                    "setter without corresponding getter");
            }
        }
    }

    private static TypeSpec.Builder createTypeSpecBuilder(
        final TypeElement ie,
        final ArrayList<ExecutableElement> getters,
        final ArrayList<ExecutableElement> setters
    ) {

        final var typeSpecBuilder = TypeSpec.classBuilder(getTargetSimpleName(ie))
            .addJavadoc("{@link " + ie.getSimpleName() + "} Data Transfer Object (DTO).")
            .addJavadoc("\n<p>\n")
            .addJavadoc("Allows creation of concrete {@code " +
                ie.getSimpleName() + "}" + " instances, which may either\n" +
                "be encodable, decodable or both, depending on which of " +
                "{@code @Encodable}\nand {@code @Decodable} it the " +
                "interface is annotated with.")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(TypeName.get(ie.asType()));

        getters.forEach(getter -> {
            final var name = getter.getSimpleName().toString();
            final var type = TypeName.get(getter.getReturnType());

            typeSpecBuilder.addField(FieldSpec.builder(type, name, Modifier.PRIVATE).build());
            typeSpecBuilder.addMethod(MethodSpec.methodBuilder(name)
                .addJavadoc("{@inheritDoc}")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(type)
                .addCode("return " + name + ";")
                .build());
        });

        setters.forEach(setter -> {
            final var name = setter.getSimpleName().toString();
            final var parameter = setter.getParameters().get(0);
            final var parameterType = TypeName.get(parameter.asType());
            final var parameterName = parameter.getSimpleName().toString();

            typeSpecBuilder.addMethod(MethodSpec.methodBuilder(name)
                .addJavadoc("{@inheritDoc}")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(parameterType, parameterName, Modifier.FINAL).build())
                .addCode("this." + name + " = " + parameterName + ";")
                .build());
        });

        return typeSpecBuilder;
    }

    private static String getTargetSimpleName(final TypeElement ie) {
        return ie.getSimpleName() + "DTO";
    }

    private static String getTargetPackageName(final TypeElement ie) {
        final var simpleName = ie.getSimpleName().toString();
        final var qualifiedName = ie.getQualifiedName().toString();
        return simpleName.length() >= qualifiedName.length()
            ? ""
            : qualifiedName.substring(0, qualifiedName.length() - simpleName.length() - 1);
    }

    private static void specifyDecoders(final TypeElement ie, TypeSpec.Builder ts) throws DTOException {
        for (final var coding : ie.getAnnotation(Decodable.class).value()) {
            if ("json".equals(coding)) {
                DTOTargetJson.specifyJsonDecoder(ie, ts);
            }
            else {
                throw new DTOException(ie, "Unsupported @Decodable data " +
                    "format `" + coding + "`");
            }
        }
    }

    private static void specifyEncoders(final TypeElement ie, TypeSpec.Builder ts) throws DTOException {
        for (final var coding : ie.getAnnotation(Decodable.class).value()) {
            if ("json".equals(coding)) {
                DTOTargetJson.specifyJsonEncoder(ie, ts);
            }
            else {
                throw new DTOException(ie, "Unsupported @Encodable data " +
                    "format `" + coding + "`");
            }
        }
    }

    public TypeElement interfaceElement() {
        return interfaceElement;
    }

    public TypeSpec typeSpec() {
        return typeSpec;
    }

    public String packageName() {
        return packageName;
    }
}
