package se.arkalix.util._internal;

import se.arkalix.util.annotation.Internal;

import java.nio.ByteOrder;
import java.util.Optional;

@SuppressWarnings("unused")
@Internal
public class BinaryMath {
    private BinaryMath() {}

    /**
     * Determines if the given {@code value} is a positive power of two.
     *
     * @param value Tested value.
     * @return {@code true} only if {@code value} is a positive power of two.
     */
    public static boolean isPositivePowerOfTwo(final int value) {
        return value > 0 && Integer.bitCount(value) == 1;
    }

    /**
     * Determines if the range specified by {@code offset} and {@code length}
     * does not reside within the bounds between 0 and {@code capacity}, or if
     * any of {@code offset} or {@code length} is below 0.
     * <p>
     * The implementation of this function has been taken from
     * <i>io.netty.util.internal.MathUtil#isOutOfBounds(int, int, int)</i> of
     * version 4.1 of the <a href="https://netty.io">Netty library</a>, which
     * is covered by the <a href="https://apache.org/licenses/LICENSE-2.0">
     * Apache 2.0 license</a>.
     *
     * @param offset   Start of inner range.
     * @param length   Length of inner range.
     * @param capacity Length outer range.
     * @return {@code true} only if the specified range is out of bounds.
     */
    public static boolean isRangeOutOfBounds(final int offset, final int length, final int capacity) {
        return (offset | length | (offset + length) | (capacity - (offset + length))) < 0;
    }

    /**
     * Rounds up {@code value} to the next power of two.
     * <p>
     * If {@code value} is negative, zero, or its next power of two is beyond
     * the limit of what a signed 32-bit integer can express, an empty optional
     * returned.
     * <p>
     * The original implementation of this function was produced by Sean Eron
     * Anderson (seander@cs.stanford.edu), and can be found on his
     * <a href="https://graphics.stanford.edu/~seander/bithacks.html#RoundUpPowerOf2">website</a>.
     *
     * @param value The integer to round up to its next power of two.
     * @return Rounded up integer or default value.
     */
    public static Optional<Integer> roundUpToNextPowerOfTwo(int value) {
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        value++;
        return value <= 0
            ? Optional.empty()
            : Optional.of(value);
    }

    public static Optional<Integer> roundUpToMultipleOfPowerOfTwo(final int value, final int powerOfTwo) {
        if (powerOfTwo <= 0 || Integer.bitCount(powerOfTwo) != 1) {
            throw new IllegalArgumentException("powerOfTwo (" + powerOfTwo + ") not a positive power of 2");
        }
        if (value < 0) {
            return Optional.empty();
        }
        return Optional.of(value + (powerOfTwo - 1) & -powerOfTwo);
    }

    public static short getS16BeAt(final byte[] byteArray, final int offset) {
        return (short) (byteArray[offset] << 8 | byteArray[offset + 1] & 0xFF);
    }

    public static short getS16LeAt(final byte[] byteArray, final int offset) {
        return (short) (byteArray[offset] & 0xFF | byteArray[offset + 1] << 8);
    }

    public static short getS16NeAt(final byte[] byteArray, final int offset) {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? getS16LeAt(byteArray, offset)
            : getS16BeAt(byteArray, offset);
    }

    public static int getS24BeAt(final byte[] byteArray, final int offset) {
        var value = getU24BeAt(byteArray, 0);
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    public static int getS24LeAt(final byte[] byteArray, final int offset) {
        var value = getU24LeAt(byteArray, 0);
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    public static int getS24NeAt(final byte[] byteArray, final int offset) {
        var value = getU24NeAt(byteArray, 0);
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    public static int getS32BeAt(final byte[] byteArray, final int offset) {
        return (byteArray[offset] & 0xFF) << 24 |
            (byteArray[offset + 1] & 0xFF) << 16 |
            (byteArray[offset + 2] & 0xFF) << 8 |
            (byteArray[offset + 3] & 0xFF);
    }

    public static int getS32LeAt(final byte[] byteArray, final int offset) {
        return (byteArray[offset] & 0xFF) |
            (byteArray[offset + 1] & 0xFF) << 8 |
            (byteArray[offset + 2] & 0xFF) << 16 |
            (byteArray[offset + 3] & 0xFF) << 24;
    }

    public static int getS32NeAt(final byte[] byteArray, final int offset) {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? getS32LeAt(byteArray, offset)
            : getS32BeAt(byteArray, offset);
    }

    public static long getS48BeAt(final byte[] byteArray, final int offset) {
        var value = getU48BeAt(byteArray, 0);
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    public static long getS48LeAt(final byte[] byteArray, final int offset) {
        var value = getU48LeAt(byteArray, 0);
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    public static long getS48NeAt(final byte[] byteArray, final int offset) {
        var value = getU48NeAt(byteArray, 0);
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    public static long getS64BeAt(final byte[] byteArray, final int offset) {
        return ((long) byteArray[offset] & 0xFF) << 56 |
            ((long) byteArray[offset + 1] & 0xFF) << 48 |
            ((long) byteArray[offset + 2] & 0xFF) << 40 |
            ((long) byteArray[offset + 3] & 0xFF) << 32 |
            ((long) byteArray[offset + 4] & 0xFF) << 24 |
            ((long) byteArray[offset + 5] & 0xFF) << 16 |
            ((long) byteArray[offset + 6] & 0xFF) << 8 |
            ((long) byteArray[offset + 7] & 0xFF);
    }

    public static long getS64LeAt(final byte[] byteArray, final int offset) {
        return ((long) byteArray[offset] & 0xFF) |
            ((long) byteArray[offset + 1] & 0xFF) << 8 |
            ((long) byteArray[offset + 2] & 0xFF) << 16 |
            ((long) byteArray[offset + 3] & 0xFF) << 24 |
            ((long) byteArray[offset + 4] & 0xFF) << 32 |
            ((long) byteArray[offset + 5] & 0xFF) << 40 |
            ((long) byteArray[offset + 6] & 0xFF) << 48 |
            ((long) byteArray[offset + 7] & 0xFF) << 56;
    }

    public static long getS64NeAt(final byte[] byteArray, final int offset) {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? getS64LeAt(byteArray, offset)
            : getS64BeAt(byteArray, offset);
    }

    public static int getU24BeAt(final byte[] byteArray, final int offset) {
        return (byteArray[offset] & 0xFF) << 16 |
            (byteArray[offset + 1] & 0xFF) << 8 |
            (byteArray[offset + 2] & 0xFF);
    }

    public static int getU24LeAt(final byte[] byteArray, final int offset) {
        return (byteArray[offset] & 0xFF) |
            (byteArray[offset + 1] & 0xFF) << 8 |
            (byteArray[offset + 2] & 0xFF) << 16;
    }

    public static int getU24NeAt(final byte[] byteArray, final int offset) {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? getS24LeAt(byteArray, offset)
            : getS24BeAt(byteArray, offset);
    }

    public static long getU48BeAt(final byte[] byteArray, final int offset) {
        return ((long) byteArray[offset] & 0xFF) << 40 |
            ((long) byteArray[offset + 1] & 0xFF) << 32 |
            ((long) byteArray[offset + 2] & 0xFF) << 24 |
            ((long) byteArray[offset + 3] & 0xFF) << 16 |
            ((long) byteArray[offset + 4] & 0xFF) << 8 |
            ((long) byteArray[offset + 5] & 0xFF);
    }

    public static long getU48LeAt(final byte[] byteArray, final int offset) {
        return ((long) byteArray[offset] & 0xFF) |
            ((long) byteArray[offset + 1] & 0xFF) << 8 |
            ((long) byteArray[offset + 2] & 0xFF) << 16 |
            ((long) byteArray[offset + 3] & 0xFF) << 24 |
            ((long) byteArray[offset + 4] & 0xFF) << 32 |
            ((long) byteArray[offset + 5] & 0xFF) << 40;
    }

    public static long getU48NeAt(final byte[] byteArray, final int offset) {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? getS48LeAt(byteArray, offset)
            : getS48BeAt(byteArray, offset);
    }

    public static void setS16BeAt(final byte[] byteArray, final int offset, final short value) {
        byteArray[offset] = (byte) (value >>> 8);
        byteArray[offset + 1] = (byte) value;
    }

    public static void setS16LeAt(final byte[] byteArray, final int offset, final short value) {
        byteArray[offset] = (byte) value;
        byteArray[offset + 1] = (byte) (value >>> 8);
    }

    public static void setS16NeAt(final byte[] byteArray, final int offset, final short value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            setS16LeAt(byteArray, offset, value);
        }
        else {
            setS16BeAt(byteArray, offset, value);
        }
    }

    public static void setS24BeAt(final byte[] byteArray, final int offset, final int value) {
        byteArray[offset] = (byte) (value >>> 16);
        byteArray[offset + 1] = (byte) (value >>> 8);
        byteArray[offset + 2] = (byte) value;
    }

    public static void setS24LeAt(final byte[] byteArray, final int offset, final int value) {
        byteArray[offset] = (byte) value;
        byteArray[offset + 1] = (byte) (value >>> 8);
        byteArray[offset + 2] = (byte) (value >>> 16);
    }

    public static void setS24NeAt(final byte[] byteArray, final int offset, final int value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            setS24LeAt(byteArray, offset, value);
        }
        else {
            setS24BeAt(byteArray, offset, value);
        }
    }

    public static void setS32BeAt(final byte[] byteArray, final int offset, final int value) {
        byteArray[offset] = (byte) (value >>> 24);
        byteArray[offset + 1] = (byte) (value >>> 16);
        byteArray[offset + 2] = (byte) (value >>> 8);
        byteArray[offset + 3] = (byte) value;
    }

    public static void setS32LeAt(final byte[] byteArray, final int offset, final int value) {
        byteArray[offset] = (byte) value;
        byteArray[offset + 1] = (byte) (value >>> 8);
        byteArray[offset + 2] = (byte) (value >>> 16);
        byteArray[offset + 3] = (byte) (value >>> 24);
    }

    public static void setS32NeAt(final byte[] byteArray, final int offset, final int value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            setS32LeAt(byteArray, offset, value);
        }
        else {
            setS32BeAt(byteArray, offset, value);
        }
    }

    public static void setS48BeAt(final byte[] byteArray, final int offset, final long value) {
        byteArray[offset] = (byte) (value >>> 40);
        byteArray[offset + 1] = (byte) (value >>> 32);
        byteArray[offset + 2] = (byte) (value >>> 24);
        byteArray[offset + 3] = (byte) (value >>> 16);
        byteArray[offset + 4] = (byte) (value >>> 8);
        byteArray[offset + 5] = (byte) value;
    }

    public static void setS48LeAt(final byte[] byteArray, final int offset, final long value) {
        byteArray[offset] = (byte) value;
        byteArray[offset + 1] = (byte) (value >>> 8);
        byteArray[offset + 2] = (byte) (value >>> 16);
        byteArray[offset + 3] = (byte) (value >>> 24);
        byteArray[offset + 4] = (byte) (value >>> 32);
        byteArray[offset + 5] = (byte) (value >>> 40);
    }

    public static void setS48NeAt(final byte[] byteArray, final int offset, final long value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            setS48LeAt(byteArray, offset, value);
        }
        else {
            setS48BeAt(byteArray, offset, value);
        }
    }

    public static void setS64BeAt(final byte[] byteArray, final int offset, final long value) {
        byteArray[offset] = (byte) (value >>> 56);
        byteArray[offset + 1] = (byte) (value >>> 48);
        byteArray[offset + 2] = (byte) (value >>> 40);
        byteArray[offset + 3] = (byte) (value >>> 32);
        byteArray[offset + 4] = (byte) (value >>> 24);
        byteArray[offset + 5] = (byte) (value >>> 16);
        byteArray[offset + 6] = (byte) (value >>> 8);
        byteArray[offset + 7] = (byte) value;
    }

    public static void setS64LeAt(final byte[] byteArray, final int offset, final long value) {
        byteArray[offset] = (byte) value;
        byteArray[offset + 1] = (byte) (value >>> 8);
        byteArray[offset + 2] = (byte) (value >>> 16);
        byteArray[offset + 3] = (byte) (value >>> 24);
        byteArray[offset + 4] = (byte) (value >>> 32);
        byteArray[offset + 5] = (byte) (value >>> 40);
        byteArray[offset + 6] = (byte) (value >>> 48);
        byteArray[offset + 7] = (byte) (value >>> 56);
    }

    public static void setS64NeAt(final byte[] byteArray, final int offset, final long value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            setS64LeAt(byteArray, offset, value);
        }
        else {
            setS64BeAt(byteArray, offset, value);
        }
    }
}
