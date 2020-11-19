package se.arkalix.dto.types;

public interface DtoDescriptorRouter<T> {
    default T route(final DtoDescriptor descriptor) {
        switch (descriptor) {
        case ARRAY:
            return onArray(descriptor);
        case BIG_DECIMAL:
            return onBigDecimal(descriptor);
        case BIG_INTEGER:
            return onBigInteger(descriptor);
        case BOOLEAN_BOXED:
            return onBooleanBoxed(descriptor);
        case BOOLEAN_UNBOXED:
            return onBooleanUnboxed(descriptor);
        case BYTE_BOXED:
            return onByteBoxed(descriptor);
        case BYTE_UNBOXED:
            return onByteUnboxed(descriptor);
        case CHARACTER_BOXED:
            return onCharacterBoxed(descriptor);
        case CHARACTER_UNBOXED:
            return onCharacterUnboxed(descriptor);
        case CUSTOM:
            return onCustom(descriptor);
        case DOUBLE_BOXED:
            return onDoubleBoxed(descriptor);
        case DOUBLE_UNBOXED:
            return onDoubleUnboxed(descriptor);
        case DURATION:
            return onDuration(descriptor);
        case ENUM:
            return onEnum(descriptor);
        case FLOAT_BOXED:
            return onFloatBoxed(descriptor);
        case FLOAT_UNBOXED:
            return onFloatUnboxed(descriptor);
        case INTEGER_BOXED:
            return onIntegerBoxed(descriptor);
        case INTEGER_UNBOXED:
            return onIntegerUnboxed(descriptor);
        case INTERFACE:
            return onInterface(descriptor);
        case INSTANT:
            return onInstant(descriptor);
        case LIST:
            return onList(descriptor);
        case LONG_BOXED:
            return onLongBoxed(descriptor);
        case LONG_UNBOXED:
            return onLongUnboxed(descriptor);
        case MAP:
            return onMap(descriptor);
        case MONTH_DAY:
            return onMonthDay(descriptor);
        case OFFSET_DATE_TIME:
            return onOffsetDateTime(descriptor);
        case OFFSET_TIME:
            return onOffsetTime(descriptor);
        case OPTIONAL:
            return onOptional(descriptor);
        case PERIOD:
            return onPeriod(descriptor);
        case SHORT_BOXED:
            return onShortBoxed(descriptor);
        case SHORT_UNBOXED:
            return onShortUnboxed(descriptor);
        case STRING:
            return onString(descriptor);
        case YEAR:
            return onYear(descriptor);
        case YEAR_MONTH:
            return onYearMonth(descriptor);
        case ZONED_DATE_TIME:
            return onZonedDateTime(descriptor);
        case ZONE_ID:
            return onZoneId(descriptor);
        case ZONE_OFFSET:
            return onZoneOffset(descriptor);
        }
        throw new IllegalStateException();
    }

    default T onAny(final DtoDescriptor descriptor) {
        throw new IllegalStateException("Unhandled descriptor " + descriptor);
    }

    default T onArray(final DtoDescriptor descriptor) {
        return onSequence(descriptor);
    }
    
    default T onBigInteger(final DtoDescriptor descriptor) {
        return onNumber(descriptor);
    }

    default T onBigDecimal(final DtoDescriptor descriptor) {
        return onNumber(descriptor);
    }
    
    default T onBoolean(final DtoDescriptor descriptor) {
        return onAny(descriptor);
    }
    
    default T onBooleanBoxed(final DtoDescriptor descriptor) {
        return onBoolean(descriptor);
    }

    default T onBooleanUnboxed(final DtoDescriptor descriptor) {
        return onBoolean(descriptor);
    }

    default T onByte(final DtoDescriptor descriptor) {
        return onNumber(descriptor);
    }
    
    default T onByteBoxed(final DtoDescriptor descriptor) {
        return onByte(descriptor);
    }

    default T onByteUnboxed(final DtoDescriptor descriptor) {
        return onByte(descriptor);
    }

    default T onCharacter(final DtoDescriptor descriptor) {
        return onAny(descriptor);
    }
    
    default T onCharacterBoxed(final DtoDescriptor descriptor) {
        return onCharacter(descriptor);
    }

    default T onCharacterUnboxed(final DtoDescriptor descriptor) {
        return onCharacter(descriptor);
    }

    default T onCollection(final DtoDescriptor descriptor) {
        return onAny(descriptor);
    }

    default T onCustom(final DtoDescriptor descriptor) {
        return onAny(descriptor);
    }

    default T onDouble(final DtoDescriptor descriptor) {
        return onNumber(descriptor);
    }
    
    default T onDoubleBoxed(final DtoDescriptor descriptor) {
        return onDouble(descriptor);
    }

    default T onDoubleUnboxed(final DtoDescriptor descriptor) {
        return onDouble(descriptor);
    }

    default T onDuration(final DtoDescriptor descriptor) {
        return onTemporal(descriptor);
    }

    default T onEnum(final DtoDescriptor descriptor) {
        return onAny(descriptor);
    }

    default T onFloat(final DtoDescriptor descriptor) {
        return onNumber(descriptor);
    }
    
    default T onFloatBoxed(final DtoDescriptor descriptor) {
        return onFloat(descriptor);
    }

    default T onFloatUnboxed(final DtoDescriptor descriptor) {
        return onFloat(descriptor);
    }

    default T onInteger(final DtoDescriptor descriptor) {
        return onNumber(descriptor);
    }
    
    default T onIntegerBoxed(final DtoDescriptor descriptor) {
        return onInteger(descriptor);
    }

    default T onIntegerUnboxed(final DtoDescriptor descriptor) {
        return onInteger(descriptor);
    }

    default T onInstant(final DtoDescriptor descriptor) {
        return onTemporal(descriptor);
    }

    default T onInterface(final DtoDescriptor descriptor) {
        return onAny(descriptor);
    }

    default T onList(final DtoDescriptor descriptor) {
        return onSequence(descriptor);
    }

    default T onLong(final DtoDescriptor descriptor) {
        return onNumber(descriptor);
    }
    
    default T onLongBoxed(final DtoDescriptor descriptor) {
        return onLong(descriptor);
    }

    default T onLongUnboxed(final DtoDescriptor descriptor) {
        return onLong(descriptor);
    }

    default T onMap(final DtoDescriptor descriptor) {
        return onCollection(descriptor);
    }
    
    default T onMonthDay(final DtoDescriptor descriptor) {
        return onTemporal(descriptor);
    }

    default T onNative(final DtoDescriptor descriptor) {
        return onAny(descriptor);
    }

    default T onNumber(final DtoDescriptor descriptor) {
        return onAny(descriptor);
    }

    default T onOffsetDateTime(final DtoDescriptor descriptor) {
        return onTemporal(descriptor);
    }

    default T onOffsetTime(final DtoDescriptor descriptor) {
        return onTemporal(descriptor);
    }

    default T onOptional(final DtoDescriptor descriptor) {
        return onCollection(descriptor);
    }

    default T onPeriod(final DtoDescriptor descriptor) {
        return onTemporal(descriptor);
    }

    default T onSequence(final DtoDescriptor descriptor) {
        return onCollection(descriptor);
    }

    default T onShort(final DtoDescriptor descriptor) {
        return onNumber(descriptor);
    }
    
    default T onShortBoxed(final DtoDescriptor descriptor) {
        return onShort(descriptor);
    }

    default T onShortUnboxed(final DtoDescriptor descriptor) {
        return onShort(descriptor);
    }

    default T onString(final DtoDescriptor descriptor) {
        return onAny(descriptor);
    }

    default T onTemporal(final DtoDescriptor descriptor) {
        return onAny(descriptor);
    }
    
    default T onYear(final DtoDescriptor descriptor) {
        return onTemporal(descriptor);
    }

    default T onYearMonth(final DtoDescriptor descriptor) {
        return onTemporal(descriptor);
    }

    default T onZonedDateTime(final DtoDescriptor descriptor) {
        return onTemporal(descriptor);
    }

    default T onZoneId(final DtoDescriptor descriptor) {
        return onTemporal(descriptor);
    }

    default T onZoneOffset(final DtoDescriptor descriptor) {
        return onTemporal(descriptor);
    }
}
