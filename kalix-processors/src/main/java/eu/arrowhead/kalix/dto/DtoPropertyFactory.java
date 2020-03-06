package eu.arrowhead.kalix.dto;

import eu.arrowhead.kalix.dto.json.JsonName;
import eu.arrowhead.kalix.dto.types.*;
import eu.arrowhead.kalix.dto.types.DtoInterface;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DtoPropertyFactory {
    private final Types typeUtils;

    private final DeclaredType booleanType;
    private final DeclaredType byteType;
    private final DeclaredType characterType;
    private final DeclaredType doubleType;
    private final DeclaredType floatType;
    private final DeclaredType integerType;
    private final DeclaredType listType;
    private final DeclaredType longType;
    private final DeclaredType mapType;
    private final DeclaredType optionalType;
    private final DeclaredType shortType;
    private final DeclaredType stringType;

    private final Set<Modifier> publicStaticModifiers;

    public DtoPropertyFactory(final Elements elementUtils, final Types typeUtils) {
        this.typeUtils = typeUtils;

        booleanType = getDeclaredTypeOf(elementUtils, Boolean.class);
        byteType = getDeclaredTypeOf(elementUtils, Byte.class);
        characterType = getDeclaredTypeOf(elementUtils, Character.class);
        doubleType = getDeclaredTypeOf(elementUtils, Double.class);
        floatType = getDeclaredTypeOf(elementUtils, Float.class);
        integerType = getDeclaredTypeOf(elementUtils, Integer.class);
        listType = getDeclaredTypeOf(elementUtils, List.class);
        longType = getDeclaredTypeOf(elementUtils, Long.class);
        mapType = getDeclaredTypeOf(elementUtils, Map.class);
        optionalType = getDeclaredTypeOf(elementUtils, Optional.class);
        shortType = getDeclaredTypeOf(elementUtils, Short.class);
        stringType = getDeclaredTypeOf(elementUtils, String.class);

        publicStaticModifiers = Stream.of(Modifier.PUBLIC, Modifier.STATIC)
            .collect(Collectors.toSet());
    }

    private static DeclaredType getDeclaredTypeOf(final Elements elementUtils, final Class<?> class_) {
        return (DeclaredType) elementUtils.getTypeElement(class_.getCanonicalName()).asType();
    }

    public DtoProperty createFromMethod(final ExecutableElement method) throws DtoException {
        assert method.getKind() == ElementKind.METHOD;
        assert method.getEnclosingElement().getKind() == ElementKind.INTERFACE;
        assert !method.getModifiers().contains(Modifier.DEFAULT);
        assert !method.getModifiers().contains(Modifier.STATIC);

        if (method.getReturnType().getKind() == TypeKind.VOID ||
            method.getParameters().size() != 0 ||
            method.getTypeParameters().size() != 0
        ) {
            throw new DtoException(method, "@Readable/@Writable interface " +
                "methods must either be static, provide a default " +
                "implementation, or be simple getters, which means that " +
                "they have a non-void return type, takes no arguments and " +
                "does not require any type parameters");
        }

        final var builder = new DtoProperty.Builder()
            .parentElement(method)
            .name(method.getSimpleName().toString())
            .encodingNames(collectEncodingNamesFrom(method));

        var type = method.getReturnType();

        if (type.getKind().isPrimitive()) {
            return builder
                .type(toPrimitiveType(type, DtoDescriptor.valueOf(type.getKind())))
                .isOptional(false)
                .build();
        }
        if (type.getKind() == TypeKind.ARRAY) {
            return builder
                .type(toArrayType(method, type))
                .isOptional(false)
                .build();
        }

        if (typeUtils.isAssignable(typeUtils.erasure(type), optionalType)) {
            final var declaredType = (DeclaredType) type;
            final var argumentType = declaredType.getTypeArguments().get(0);
            return builder
                .type(resolveType(method, argumentType))
                .isOptional(true)
                .build();
        }

        return builder
            .type(resolveType(method, type))
            .isOptional(false)
            .build();
    }

    private Map<DataEncoding, String> collectEncodingNamesFrom(final Element method) {
        final var encodingNames = new HashMap<DataEncoding, String>();
        final var nameJSON = method.getAnnotation(JsonName.class);
        if (nameJSON != null) {
            encodingNames.put(DataEncoding.JSON, nameJSON.value());
        }
        return encodingNames;
    }

    private DtoType resolveType(final ExecutableElement method, final TypeMirror type) throws DtoException {
        if (type.getKind().isPrimitive()) {
            return toPrimitiveType(type, DtoDescriptor.valueOf(type.getKind()));
        }
        if (type.getKind() == TypeKind.ARRAY) {
            return toArrayType(method, type);
        }
        if (typeUtils.isSameType(booleanType, type)) {
            return toPrimitiveBoxedType(type, DtoDescriptor.BOOLEAN_BOXED);
        }
        if (typeUtils.isSameType(byteType, type)) {
            return toPrimitiveBoxedType(type, DtoDescriptor.BYTE_BOXED);
        }
        if (typeUtils.isSameType(characterType, type)) {
            return toPrimitiveBoxedType(type, DtoDescriptor.CHARACTER_BOXED);
        }
        if (typeUtils.isSameType(doubleType, type)) {
            return toPrimitiveBoxedType(type, DtoDescriptor.DOUBLE_BOXED);
        }
        if (typeUtils.isSameType(floatType, type)) {
            return toPrimitiveBoxedType(type, DtoDescriptor.FLOAT_BOXED);
        }
        if (typeUtils.isSameType(integerType, type)) {
            return toPrimitiveBoxedType(type, DtoDescriptor.INTEGER_BOXED);
        }
        if (typeUtils.isSameType(longType, type)) {
            return toPrimitiveBoxedType(type, DtoDescriptor.LONG_BOXED);
        }
        if (typeUtils.isSameType(shortType, type)) {
            return toPrimitiveBoxedType(type, DtoDescriptor.SHORT_BOXED);
        }
        if (typeUtils.asElement(type).getKind() == ElementKind.ENUM) {
            return toEnumType(type);
        }
        if (typeUtils.isAssignable(typeUtils.erasure(type), listType)) {
            return toListType(method, type);
        }
        if (typeUtils.isAssignable(typeUtils.erasure(type), mapType)) {
            return toMapType(method, type);
        }
        if (typeUtils.isSameType(stringType, type)) {
            return toStringType(type);
        }
        if (isEnumLike(type)) {
            return toEnumType(type);
        }
        return toInterfaceType(method, type);
    }

    private boolean isEnumLike(final TypeMirror type) {
        final var element = typeUtils.asElement(type);
        if (element == null || element.getKind() != ElementKind.CLASS) {
            return false;
        }

        var hasValueOf = false;
        var hasToString = false;

        final var typeElement = (TypeElement) element;
        for (final var enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.METHOD) {
                continue;
            }
            final var executable = (ExecutableElement) enclosed;
            final var name = executable.getSimpleName().toString();
            if (!hasValueOf && Objects.equals(name, "valueOf")) {
                if (!executable.getModifiers().containsAll(publicStaticModifiers)) {
                    continue;
                }
                if (executable.getThrownTypes().size() != 0) {
                    continue;
                }
                final var parameters = executable.getParameters();
                if (parameters.size() != 1 || !typeUtils.isSameType(parameters.get(0).asType(), stringType)) {
                    continue;
                }
                hasValueOf = true;
            }
            if (!hasToString && Objects.equals(name, "toString")) {
                final var modifiers = executable.getModifiers();
                if (modifiers.size() != 1 || !modifiers.contains(Modifier.PUBLIC)) {
                    continue;
                }
                final var parameters = executable.getParameters();
                if (parameters.size() != 0) {
                    continue;
                }
                hasToString = true;
            }
        }
        return hasValueOf && hasToString;
    }

    private DtoArray toArrayType(final ExecutableElement method, final TypeMirror type) throws DtoException {
        final var arrayType = (ArrayType) type;
        final var element = resolveType(method, arrayType.getComponentType());
        return new DtoArray(arrayType, element);
    }

    private DtoInterface toInterfaceType(final ExecutableElement method, final TypeMirror type) throws DtoException {
        final var declaredType = (DeclaredType) type;
        final var element = declaredType.asElement();

        final var readable = element.getAnnotation(Readable.class);
        final var writable = element.getAnnotation(Writable.class);

        if (readable == null && writable == null) {
            if (element.getSimpleName().toString().endsWith(DtoTarget.NAME_SUFFIX)) {
                throw new DtoException(method, "Generated DTO classes may " +
                    "not be used in interfaces annotated with @Readable or " +
                    "@Writable; rather use the interface types from which " +
                    "those DTO classes were generated");
            }
            throw new DtoException(method, "Getter return type must be a " +
                "primitive, a boxed primitive, a String, an array (T[]), " +
                "a List<T>, a Map<K, V>, an enum, an enum-like class, which " +
                "overrides toString() and has a public static " +
                "valueOf(String) method, or be annotated with @Readable " +
                "and/or @Writable; if an array, list or map, their " +
                "parameters must conform to the same requirements");
        }

        final var readableEncodings = readable != null ? readable.value() : new DataEncoding[0];
        final var writableEncodings = writable != null ? writable.value() : new DataEncoding[0];

        return new DtoInterface(declaredType, readableEncodings, writableEncodings);
    }

    private DtoEnum toEnumType(final TypeMirror type) {
        return new DtoEnum((DeclaredType) type);
    }

    private DtoList toListType(final ExecutableElement method, final TypeMirror type) throws DtoException {
        final var declaredType = (DeclaredType) type;
        final var element = resolveType(method, declaredType.getTypeArguments().get(0));
        return new DtoList(declaredType, element);
    }

    private DtoMap toMapType(final ExecutableElement method, final TypeMirror type) throws DtoException {
        final var declaredType = (DeclaredType) type;
        final var typeArguments = declaredType.getTypeArguments();
        final var key = resolveType(method, typeArguments.get(0));
        if (key.descriptor().isCollection() || key.descriptor() == DtoDescriptor.INTERFACE) {
            throw new DtoException(method, "Only boxed primitives, enums, " +
                "enum-likes and strings may be used as Map keys");
        }
        final var value = resolveType(method, typeArguments.get(1));
        return new DtoMap(declaredType, key, value);
    }

    private DtoPrimitiveUnboxed toPrimitiveType(final TypeMirror type, final DtoDescriptor primitiveType) {
        return new DtoPrimitiveUnboxed((PrimitiveType) type, primitiveType);
    }

    private DtoPrimitiveBoxed toPrimitiveBoxedType(final TypeMirror type, final DtoDescriptor primitiveType) {
        return new DtoPrimitiveBoxed((DeclaredType) type, primitiveType);
    }

    private DtoString toStringType(final TypeMirror type) {
        return new DtoString((DeclaredType) type);
    }
}
