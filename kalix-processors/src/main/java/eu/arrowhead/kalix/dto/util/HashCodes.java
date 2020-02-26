package eu.arrowhead.kalix.dto.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static eu.arrowhead.kalix.internal.util.BinaryMath.findNextPositivePowerOfTwo;

public class HashCodes {
    private HashCodes() {}

    public static <T> Optional<Unique> findSmallestUniqueHashCodesFor(final List<T> objects) {
        final var o1 = objects.size();
        final var hashCodes = new int[o1];
        for (var o0 = 0; o0 < o1; ++o0) {
            hashCodes[o0] = objects.get(o0).hashCode();
        }
        final var testedCodes = new int[o1];
        final var testedCodesSorted = new int[o1];
        var mask = findNextPositivePowerOfTwo(o1) - 1;
        var shiftMax = Integer.numberOfLeadingZeros(mask);
        for (var attempts = 0; attempts < 4 && mask < 0x1FFFFFFF; ++attempts) {
            shift:
            for (var shift = 0; shift < shiftMax; ++shift) {
                for (var o0 = 0; o0 < o1; ++o0) {
                    final var hashCode  = (mask << shift) & hashCodes[o0];
                    testedCodes[o0] = hashCode >>> shift;
                    testedCodesSorted[o0] = hashCode;
                }
                Arrays.sort(testedCodesSorted);
                var last = -1;
                for (var o0 = 0; o0 < o1; ++o0) {
                    final var code = testedCodesSorted[o0];
                    if (code == last) {
                        continue shift;
                    }
                    last = code;
                }
                return Optional.of(new Unique(mask, shift, testedCodes));
            }
            mask = findNextPositivePowerOfTwo(mask + 2) - 1;
        }
        return Optional.empty();
    }

    public static class Unique {
        private final int mask;
        private final int shiftRight;
        private final int[] hashCodes;

        public Unique(final int mask, final int shiftRight, final int[] codes) {
            this.mask = mask;
            this.shiftRight = shiftRight;
            hashCodes = codes;
        }

        public int mask() {
            return mask;
        }

        public int shiftRight() {
            return shiftRight;
        }

        public int[] hashCodes() {
            return hashCodes;
        }
    }
}
