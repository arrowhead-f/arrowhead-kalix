package eu.arrowhead.kalix.internal.util;

public class BinaryMath {
    private BinaryMath() {}

    /**
     * Calculates the smallest power of two greater than or equal to
     * {@code value}, which must not be greater than 2^30.
     *
     * @param value Value from which search begins.
     * @return Power of two. Result is undefined if {@code value < 1}.
     * @throws IllegalArgumentException If {@code value} is larger than 2^30.
     *
     * @see <a href="https://graphics.stanford.edu/~seander/bithacks.html#RoundUpPowerOf2">Bit Twiddling Hacks - Round up to the next highest power of 2</a>
     */
    public static int findNextPositivePowerOfTwo(int value) {
        if (value > 0x40000000) {
            throw new IllegalArgumentException(value + " outside permitted range");
        }
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        return ++value;
    }
}
