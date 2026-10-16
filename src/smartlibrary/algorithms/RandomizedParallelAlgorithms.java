package smartlibrary.algorithms;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/** Randomized algorithms and parallel-work primitives for Module 6. */
public final class RandomizedParallelAlgorithms {
    private static final int REDUCE_SEQUENTIAL_THRESHOLD = 512;
    private RandomizedParallelAlgorithms() { }

    /** In-place randomized three-way QuickSort. */
    public static <T> void randomizedQuickSort(
            T[] values, Comparator<? super T> comparator, Random random) {
        Objects.requireNonNull(values, "values cannot be null");
        Objects.requireNonNull(comparator, "comparator cannot be null");
        Objects.requireNonNull(random, "random cannot be null");
        quickSort(values, 0, values.length - 1, comparator, random);
    }

    private static <T> void quickSort(T[] values, int low, int high,
                                      Comparator<? super T> comparator,
                                      Random random) {
        while (low < high) {
            int pivotIndex = low + random.nextInt(high - low + 1);
            T pivot = values[pivotIndex];
            int less = low;
            int current = low;
            int greater = high;
            while (current <= greater) {
                int order = comparator.compare(values[current], pivot);
                if (order < 0) {
                    swap(values, less++, current++);
                } else if (order > 0) {
                    swap(values, current, greater--);
                } else {
                    current++;
                }
            }
            // Recurse on the smaller side; process the larger side iteratively.
            if (less - low < high - greater) {
                quickSort(values, low, less - 1, comparator, random);
                low = greater + 1;
            } else {
                quickSort(values, greater + 1, high, comparator, random);
                high = less - 1;
            }
        }
    }

    private static <T> void swap(T[] values, int first, int second) {
        T temporary = values[first];
        values[first] = values[second];
        values[second] = temporary;
    }

    /** Uniformly samples up to k elements from a one-pass stream. */
    public static <T> List<T> reservoirSample(
            Iterable<T> stream, int sampleSize, Random random) {
        if (sampleSize < 0) {
            throw new IllegalArgumentException("sample size cannot be negative");
        }
        Objects.requireNonNull(stream, "stream cannot be null");
        Objects.requireNonNull(random, "random cannot be null");
        List<T> sample = new ArrayList<>(Math.min(sampleSize, 1024));
        long seen = 0;
        for (T item : stream) {
            if (seen == Long.MAX_VALUE) {
                throw new IllegalArgumentException("stream exceeds supported length");
            }
            seen++;
            if (seen <= sampleSize) {
                sample.add(item);
            } else if (sampleSize > 0) {
                long position = nextLong(random, seen);
                if (position < sampleSize) {
                    sample.set((int) position, item);
                }
            }
        }
        return sample;
    }

    private static long nextLong(Random random, long bound) {
        long mask = bound - 1;
        long candidate = random.nextLong() >>> 1;
        if ((bound & mask) == 0) {
            return candidate & mask;
        }
        long result = candidate % bound;
        while (candidate + mask - result < 0) {
            candidate = random.nextLong() >>> 1;
            result = candidate % bound;
        }
        return result;
    }

    /** Deterministic Miller–Rabin for all positive signed 64-bit integers. */
    public static boolean isPrime(long value) {
        if (value < 2) {
            return false;
        }
        int[] smallPrimes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37};
        for (int prime : smallPrimes) {
            if (value == prime) {
                return true;
            }
            if (value % prime == 0) {
                return false;
            }
        }

        long oddPart = value - 1;
        int powersOfTwo = Long.numberOfTrailingZeros(oddPart);
        oddPart >>>= powersOfTwo;
        // This base set is deterministic for unsigned 64-bit integers.
        long[] bases = {2, 325, 9375, 28178, 450775, 9780504, 1795265022};
        for (long base : bases) {
            long reducedBase = base % value;
            if (reducedBase == 0) {
                continue;
            }
            long witness = modPow(reducedBase, oddPart, value);
            if (witness == 1 || witness == value - 1) {
                continue;
            }
            boolean reachesMinusOne = false;
            for (int round = 1; round < powersOfTwo; round++) {
                witness = multiplyMod(witness, witness, value);
                if (witness == value - 1) {
                    reachesMinusOne = true;
                    break;
                }
            }
            if (!reachesMinusOne) {
                return false;
            }
        }
        return true;
    }

    private static long modPow(long base, long exponent, long modulus) {
        long result = 1;
        long factor = base % modulus;
        long remaining = exponent;
        while (remaining > 0) {
            if ((remaining & 1) != 0) {
                result = multiplyMod(result, factor, modulus);
            }
            factor = multiplyMod(factor, factor, modulus);
            remaining >>>= 1;
        }
        return result;
    }

    private static long multiplyMod(long first, long second, long modulus) {
        return BigInteger.valueOf(first).multiply(BigInteger.valueOf(second))
                .mod(BigInteger.valueOf(modulus)).longValue();
    }

    /** Blelloch exclusive prefix scan. The output has the same length as input. */
    public static long[] blellochExclusiveScan(long[] input) {
        Objects.requireNonNull(input, "input cannot be null");
        if (input.length == 0) {
            return new long[0];
        }
        int paddedLength = 1;
        while (paddedLength < input.length) {
            if (paddedLength >= (1 << 30)) {
                throw new IllegalArgumentException("input is too large for array scan");
            }
            paddedLength <<= 1;
        }
        long[] tree = Arrays.copyOf(input, paddedLength);

        // Upsweep: partial sums rise to the root.
        for (int stride = 1; stride < paddedLength; stride <<= 1) {
            int step = stride << 1;
            for (int index = step - 1; index < paddedLength; index += step) {
                tree[index] = Math.addExact(tree[index], tree[index - stride]);
            }
        }

        // Downsweep: turn partial sums into exclusive prefixes.
        tree[paddedLength - 1] = 0;
        for (int stride = paddedLength >>> 1; stride >= 1; stride >>>= 1) {
            int step = stride << 1;
            for (int index = step - 1; index < paddedLength; index += step) {
                long leftTotal = tree[index - stride];
                tree[index - stride] = tree[index];
                tree[index] = Math.addExact(tree[index], leftTotal);
            }
        }
        return Arrays.copyOf(tree, input.length);
    }

    /** Parallel sum using the shared ForkJoin pool; addition is overflow-checked. */
    public static long parallelReduce(long[] values) {
        Objects.requireNonNull(values, "values cannot be null");
        return ForkJoinPool.commonPool().invoke(
                new SumTask(values, 0, values.length));
    }

    private static final class SumTask extends RecursiveTask<Long> {
        private final long[] values;
        private final int low;
        private final int high;

        SumTask(long[] values, int low, int high) {
            this.values = values;
            this.low = low;
            this.high = high;
        }

        @Override
        protected Long compute() {
            if (high - low <= REDUCE_SEQUENTIAL_THRESHOLD) {
                long sum = 0;
                for (int i = low; i < high; i++) {
                    sum = Math.addExact(sum, values[i]);
                }
                return sum;
            }
            int middle = (low + high) >>> 1;
            SumTask left = new SumTask(values, low, middle);
            SumTask right = new SumTask(values, middle, high);
            left.fork();
            long rightSum = right.compute();
            return Math.addExact(left.join(), rightSum);
        }
    }

    /** Brent's theorem bound W/P + D for work W, depth D, P processors. */
    public static double brentBound(long work, long depth, int processors) {
        if (work < 0 || depth < 0 || processors < 1) {
            throw new IllegalArgumentException(
                    "work/depth must be nonnegative and processors positive");
        }
        return (double) work / processors + depth;
    }
}
