package se.arkalix.dto;

import se.arkalix.dto.json.DtoJsonName;
import se.arkalix.dto.types.*;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DtoPropertyFactory {
    private final Elements elementUtils;
    private final Types typeUtils;

    // Primitive types.
    private final DeclaredType booleanType;
    private final DeclaredType byteType;
    private final DeclaredType characterType;
    private final DeclaredType doubleType;
    private final DeclaredType floatType;
    private final DeclaredType integerType;
    private final DeclaredType longType;
    private final DeclaredType shortType;

    // Collection types.
    private final DeclaredType listType;
    private final DeclaredType mapType;

    // Big number types.
    private final DeclaredType bigDecimalType;
    private final DeclaredType bigIntegerType;

    // Temporal types.
    private final DeclaredType durationType;
    private final DeclaredType instantType;
    private final DeclaredType monthDayType;
    private final DeclaredType offsetDateTimeType;
    private final DeclaredType offsetTimeType;
    private final DeclaredType periodType;
    private final DeclaredType yearType;
    private final DeclaredType yearMonthType;
    private final DeclaredType zonedDateTimeType;
    private final DeclaredType zoneIdType;
    private final DeclaredType zoneOffsetType;

    // Other types.
    private final DeclaredType optionalType;
    private final DeclaredType stringType;

    private final Set<Modifier> publicStaticModifiers;

    public DtoPropertyFactory(final Elements elementUtils, final Types typeUtils) {
        this.elementUtils = Objects.requireNonNull(elementUtils, "Expected elementUtils");
        this.typeUtils = Objects.requireNonNull(typeUtils, "Expected typeUtils");

        final Function<Class<?>, DeclaredType> getDeclaredType = (class_) ->
            (DeclaredType) elementUtils.getTypeElement(class_.getCanonicalName()).asType();

        booleanType = getDeclaredType.apply(Boolean.class);
        byteType = getDeclaredType.apply(Byte.class);
        characterType = getDeclaredType.apply(Character.class);
        doubleType = getDeclaredType.apply(Double.class);
        floatType = getDeclaredType.apply(Float.class);
        integerType = getDeclaredType.apply(Integer.class);
        longType = getDeclaredType.apply(Long.class);
        shortType = getDeclaredType.apply(Short.class);

        listType = getDeclaredType.apply(List.class);
        mapType = getDeclaredType.apply(Map.class);

        bigDecimalType = getDeclaredType.apply(BigDecimal.class);
        bigIntegerType = getDeclaredType.apply(BigInteger.class);

        durationType = getDeclaredType.apply(Duration.class);
        instantType = getDeclaredType.apply(Instant.class);
        monthDayType = getDeclaredType.apply(MonthDay.class);
        offsetDateTimeType = getDeclaredType.apply(OffsetDateTime.class);
        offsetTimeType = getDeclaredType.apply(OffsetTime.class);
        periodType = getDeclaredType.apply(Period.class);
        yearType = getDeclaredType.apply(Year.class);
        yearMonthType = getDeclaredType.apply(YearMonth.class);
        zonedDateTimeType = getDeclaredType.apply(ZonedDateTime.class);
        zoneIdType = getDeclaredType.apply(ZoneId.class);
        zoneOffsetType = getDeclaredType.apply(ZoneOffset.class);

        optionalType = getDeclaredType.apply(Optional.class);
        stringType = getDeclaredType.apply(String.class);

        publicStaticModifiers = Stream.of(Modifier.PUBLIC, Modifier.STATIC)
            .collect(Collectors.toSet());
    }

    public DtoProperty createFromMethod(final ExecutableElement method) {
        assert method.getKind() == ElementKind.METHOD;
        assert method.getEnclosingElement().getKind() == ElementKind.INTERFACE;
        assert !method.getModifiers().contains(Modifier.DEFAULT);
        assert !method.getModifiers().contains(Modifier.STATIC);

        if (method.getReturnType().getKind() == TypeKind.VOID ||
            method.getParameters().size() != 0 ||
            method.getTypeParameters().size() != 0
        )
        {
            throw new DtoException(method, "@DtoReadableAs/@DtoWritableAs " +
                "interface methods must either be static, provide a default " +
                "implementation, or be simple getters, which means that " +
                "they have a non-void return type, take no arguments and " +
                "do not have any type parameters");
        }

        final var builder = new DtoProperty.Builder()
            .parentElement(method)
            .name(method.getSimpleName().toString())
            .dtoCodecToName(collectDtoCodecNamesFrom(method));

        var type = method.getReturnType();

        if (type.getKind().isPrimitive()) {
            return builder
                .type(toElementType(type, DtoDescriptor.valueOf(type.getKind())))
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
            final var dtoType = resolveDtoType(method, argumentType);
            return builder
                .type(dtoType)
                .isOptional(true)
                .build();
        }

        return builder
            .type(resolveDtoType(method, type))
            .isOptional(false)
            .build();
    }

    private Map<DtoCodec, String> collectDtoCodecNamesFrom(final Element method) {
        final var codecNames = new HashMap<DtoCodec, String>();
        final var nameJSON = method.getAnnotation(DtoJsonName.class);
        if (nameJSON != null) {
            codecNames.put(DtoCodec.JSON, nameJSON.value());
        }
        return codecNames;
    }

    private DtoType resolveDtoType(final ExecutableElement method, final TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return toElementType(type, DtoDescriptor.valueOf(type.getKind()));
        }
        if (type.getKind() == TypeKind.ARRAY) {
            return toArrayType(method, type);
        }
        if (typeUtils.isSameType(bigDecimalType, type)) {
            return toElementType(type, DtoDescriptor.BIG_DECIMAL);
        }
        if (typeUtils.isSameType(bigIntegerType, type)) {
            return toElementType(type, DtoDescriptor.BIG_INTEGER);
        }
        if (typeUtils.isSameType(booleanType, type)) {
            return toElementType(type, DtoDescriptor.BOOLEAN_BOXED);
        }
        if (typeUtils.isSameType(byteType, type)) {
            return toElementType(type, DtoDescriptor.BYTE_BOXED);
        }
        if (typeUtils.isSameType(characterType, type)) {
            return toElementType(type, DtoDescriptor.CHARACTER_BOXED);
        }
        if (typeUtils.isSameType(doubleType, type)) {
            return toElementType(type, DtoDescriptor.DOUBLE_BOXED);
        }
        if (typeUtils.isSameType(durationType, type)) {
            return toElementType(type, DtoDescriptor.DURATION);
        }
        if (typeUtils.isSameType(floatType, type)) {
            return toElementType(type, DtoDescriptor.FLOAT_BOXED);
        }
        if (typeUtils.isSameType(instantType, type)) {
            return toElementType(type, DtoDescriptor.INSTANT);
        }
        if (typeUtils.isSameType(integerType, type)) {
            return toElementType(type, DtoDescriptor.INTEGER_BOXED);
        }
        if (typeUtils.isSameType(longType, type)) {
            return toElementType(type, DtoDescriptor.LONG_BOXED);
        }
        if (typeUtils.isSameType(monthDayType, type)) {
            return toElementType(type, DtoDescriptor.MONTH_DAY);
        }
        if (typeUtils.isSameType(offsetDateTimeType, type)) {
            return toElementType(type, DtoDescriptor.OFFSET_DATE_TIME);
        }
        if (typeUtils.isSameType(offsetTimeType, type)) {
            return toElementType(type, DtoDescriptor.OFFSET_TIME);
        }
        if (typeUtils.isSameType(periodType, type)) {
            return toElementType(type, DtoDescriptor.PERIOD);
        }
        if (typeUtils.isSameType(shortType, type)) {
            return toElementType(type, DtoDescriptor.SHORT_BOXED);
        }
        if (typeUtils.asElement(type).getKind() == ElementKind.ENUM) {
            return toElementType(type, DtoDescriptor.ENUM);
        }
        if (typeUtils.isAssignable(typeUtils.erasure(type), listType)) {
            return toListType(method, type);
        }
        if (typeUtils.isAssignable(typeUtils.erasure(type), mapType)) {
            return toMapType(method, type);
        }
        if (typeUtils.isSameType(stringType, type)) {
            return toElementType(type, DtoDescriptor.STRING);
        }
        if (typeUtils.isSameType(yearType, type)) {
            return toElementType(type, DtoDescriptor.YEAR);
        }
        if (typeUtils.isSameType(yearMonthType, type)) {
            return toElementType(type, DtoDescriptor.YEAR_MONTH);
        }
        if (typeUtils.isSameType(zonedDateTimeType, type)) {
            return toElementType(type, DtoDescriptor.ZONED_DATE_TIME);
        }
        if (typeUtils.isSameType(zoneIdType, type)) {
            return toElementType(type, DtoDescriptor.ZONE_ID);
        }
        if (typeUtils.isSameType(zoneOffsetType, type)) {
            return toElementType(type, DtoDescriptor.ZONE_OFFSET);
        }
        if (isEnumLike(type)) {
            return toElementType(type, DtoDescriptor.ENUM);
        }
        return toInterfaceOrCustomType(method, type);
    }

    private boolean isEnumLike(final TypeMirror type) {
        final var element = typeUtils.asElement(type);
        if (element == null || element.getKind() != ElementKind.CLASS) {
            return false;
        }

        var hasEquals = false;
        var hasHashCode = false;
        var hasToString = false;
        var hasValueOf = false;

        final var typeElement = (TypeElement) element;
        for (final var enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.METHOD) {
                continue;
            }
            final var executable = (ExecutableElement) enclosed;
            final var name = executable.getSimpleName().toString();
            if (!hasEquals && Objects.equals(name, "equals")) {
                final var modifiers = executable.getModifiers();
                if (modifiers.size() != 1 || !modifiers.contains(Modifier.PUBLIC)) {
                    continue;
                }
                final var parameters = executable.getParameters();
                if (parameters.size() != 1 || parameters.get(0).getSimpleName().contentEquals("Object")) {
                    continue;
                }
                hasEquals = true;
            }
            if (!hasHashCode && Objects.equals(name, "hashCode")) {
                final var modifiers = executable.getModifiers();
                if (modifiers.size() != 1 || !modifiers.contains(Modifier.PUBLIC)) {
                    continue;
                }
                final var parameters = executable.getParameters();
                if (parameters.size() != 0) {
                    continue;
                }
                hasHashCode = true;
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
        }
        return hasEquals && hasHashCode && hasToString && hasValueOf;
    }

    private DtoSequence toArrayType(final ExecutableElement method, final TypeMirror type) {
        final var arrayType = (ArrayType) type;
        final var element = resolveDtoType(method, arrayType.getComponentType());
        return DtoSequence.newArray(arrayType, element);
    }

    private DtoType toInterfaceOrCustomType(final ExecutableElement method, final TypeMirror type) {
        var element = typeUtils.asElement(type);
        var readable = element.getAnnotation(DtoReadableAs.class);
        var writable = element.getAnnotation(DtoWritableAs.class);

        if (readable == null && writable == null) {
            if (element.getSimpleName().toString().endsWith(DtoTarget.DATA_SUFFIX)) {
                throw new DtoException(method, "Generated DTO classes may " +
                    "not be used in interfaces annotated with @DtoReadableAs " +
                    "or @DtoWritableAs; rather use the interface types from " +
                    "which those DTO classes were generated");
            }
            if (!element.getKind().isClass() && !element.getKind().isInterface()) {
                throw new DtoException(method, "Only class and interface " +
                    "types may be used as custom DTO classes");
            }
            return new DtoCustom(type, (TypeElement) element, typeUtils, elementUtils);
        }

        return new DtoInterface(elementUtils, element);
    }

    private DtoSequence toListType(final ExecutableElement method, final TypeMirror type) {
        final var declaredType = (DeclaredType) type;
        final var element = resolveDtoType(method, declaredType.getTypeArguments().get(0));
        return DtoSequence.newList(declaredType, element);
    }

    private DtoMap toMapType(final ExecutableElement method, final TypeMirror type) {
        final var declaredType = (DeclaredType) type;
        final var typeArguments = declaredType.getTypeArguments();
        final var key = resolveDtoType(method, typeArguments.get(0));
        if (key.descriptor().isCollection() || key.descriptor() == DtoDescriptor.INTERFACE) {
            throw new DtoException(method, "Only boxed primitives, enums, " +
                "enum-likes and strings may be used as Map keys");
        }
        final var value = resolveDtoType(method, typeArguments.get(1));
        return new DtoMap(declaredType, key, value);
    }

    private DtoType toElementType(final TypeMirror type, final DtoDescriptor descriptor) {
        return new DtoElement(type, descriptor);
    }
}
