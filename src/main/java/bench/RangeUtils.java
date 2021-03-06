package bench;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


public class RangeUtils {
    private static final Random RANDOM;
    private static final TimeUnit TIME_UNIT = TimeUnit.NANOSECONDS;

    static {
        RANDOM = new Random(Long.MAX_VALUE);
    }

    public static int randomInt(int N) {
        return RANDOM.nextInt(N);
    }

    public static int[] randomRange(int N) {
        int[] r = new int[2];
        randomRange(N, r);
        return r;
    }

    public static int[] randomRange(int N, int selectivityRows) {
        int[] r = new int[2];
        randomRange(N, selectivityRows, r);
        return r;
    }

    public static int[] randomRange(int N, float selectivity) {
        int[] r = new int[2];
        randomRange(N, selectivity, r);
        return r;
    }

    public static void randomRange(int N, int[] r) {
        int low = 0, high = 0;
        while (low == 0 || low >= high) {
            int[] ints = RANDOM.ints(2, 0, N).toArray();
            low = Math.min(ints[0], ints[1]);
            high = Math.max(ints[0], ints[1]);
        }
        r[0] = low;
        r[1] = high;
    }

    public static void randomRange(int N, float selectivity, int[] r) {
        int n = (int) (N * selectivity);
        int low = RANDOM.nextInt(Math.max(N - n, 1));
        int high = low + n;
        r[0] = low;
        r[1] = high;
    }

    public static void randomRange(int N, int selectivityRows, int[] r) {
        int low = RANDOM.nextInt(Math.max(N - selectivityRows, 1));
        int high = low + selectivityRows;
        r[0] = low;
        r[1] = high;
    }

//    public static void main(String[] args) {
//        float selectivity = 0.0f;
//        while (selectivity <= 1.0f) {
//            System.out.println("S: " + selectivity + " " + Arrays.toString(randomRange(1000, selectivity)));
//            selectivity += 0.1f;
//        }
//    }
//    public static void main(String[] args) {
//        int selectivityRows = 0;
//        while (selectivityRows <= 10000) {
//            System.out.println("S: " + selectivityRows + " " + Arrays.toString(randomRange(10000, selectivityRows)));
//            selectivityRows += 100;
//        }
//    }


    public static List<Integer> data(int size) {
        Integer[] col = new Integer[size];
        for (int i = 0; i < size; i++) {
            col[i] = i + 1;
        }
        List<Integer> list = Arrays.asList(col);
        Collections.shuffle(list, RANDOM);
        return list;
    }

    static int[][] buildRanges(int N, int size) {
        int[][] ranges = new int[size][2];
        for (int[] range : ranges) {
            randomRange(N, range);
        }
        return ranges;
    }

    static int[][] buildRanges(int N, int size, float selectivity) {
        int[][] ranges = new int[size][2];
        for (int[] range : ranges) {
            randomRange(N, selectivity, range);
        }
        return ranges;
    }

    static int[][] buildRanges(int N, int size, int selectivityRows) {
        int[][] ranges = new int[size][2];
        for (int[] range : ranges) {
            randomRange(N, selectivityRows, range);
        }
        return ranges;
    }

    static long time(Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        return TIME_UNIT.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
    }

    static <T> T time(Supplier<T> supplier, long[] dest, int destIndex) {
        long start = System.nanoTime();
        T t = supplier.get();
        dest[destIndex] = TIME_UNIT.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        return t;
    }
}
