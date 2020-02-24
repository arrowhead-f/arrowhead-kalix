package eu.arrowhead.kalix.processors.io;

import com.squareup.javapoet.*;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class DataTarget {
    private final TypeSpec typeSpec;
    private final String packageName;

    private DataTarget(final TypeSpec typeSpec, final String packageName) {
        this.typeSpec = typeSpec;
        this.packageName = packageName;
    }

    public static DataTarget createFrom(final Element interfaceElement) throws EncodableException {
        final var ie = validateAndCastInterface(interfaceElement);

        final var getters = new ArrayList<ExecutableElement>();
        final var setters = new ArrayList<ExecutableElement>();
        collectAndValidateGettersAndSetters(ie, getters, setters);

        return new DataTarget(
            createTypeSpec(ie, getters, setters),
            getTargetPackageName(ie)
        );
    }

    private static TypeElement validateAndCastInterface(final Element element) throws EncodableException {
        if (!(element instanceof TypeElement) ||
            element.getKind() != ElementKind.INTERFACE
        ) {
            throw new EncodableException(element, "@Encodable subject must " +
                "be an interface");
        }
        final var ie = (TypeElement) element;
        if (ie.getTypeParameters().size() != 0) {
            throw new EncodableException(element, "@Encodable interface may " +
                "not have type parameters");
        }
        if (element.getSimpleName().toString().startsWith("Data")) {
            throw new EncodableException(element, "@Encodable interface may " +
                "not have name starting with `Data`");
        }
        return ie;
    }

    private static void collectAndValidateGettersAndSetters(
        final TypeElement ie,
        final List<ExecutableElement> getters,
        final List<ExecutableElement> setters
    ) throws EncodableException {
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
            throw new EncodableException(ie, "@Encodable interface method " +
                "must be getter, setter, static or default");
        }

        for (final var setter : setters) {
            if (!getterNames.contains(setter.getSimpleName())) {
                throw new EncodableException(setter, "@Encodable contains " +
                    "setter without corresponding getter");
            }
        }
    }

    private static TypeSpec createTypeSpec(
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

        return typeSpecBuilder.build();
    }

    private static String getTargetSimpleName(final TypeElement ie) {
        return "Data" + ie.getSimpleName();
    }

    private static String getTargetPackageName(final TypeElement ie) {
        final var simpleName = ie.getSimpleName().toString();
        final var qualifiedName = ie.getQualifiedName().toString();
        return simpleName.length() >= qualifiedName.length()
            ? ""
            : qualifiedName.substring(0, qualifiedName.length() - simpleName.length() - 1);
    }

    public TypeSpec typeSpec() {
        return typeSpec;
    }

    public String packageName() {
        return packageName;
    }
}
