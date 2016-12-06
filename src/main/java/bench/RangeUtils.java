package bench;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class RangeUtils {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static int randomInt(int N) {
        return SECURE_RANDOM.nextInt(N);
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
            int[] ints = SECURE_RANDOM.ints(2, 0, N).toArray();
            low = Math.min(ints[0], ints[1]);
            high = Math.max(ints[0], ints[1]);
        }
        r[0] = low;
        r[1] = high;
    }

    public static void randomRange(int N, float selectivity, int[] r) {
        int n = (int) (N * selectivity);
        int low = SECURE_RANDOM.nextInt(Math.max(N - n, 1));
        int high = low + n;
        r[0] = low;
        r[1] = high;
    }

    public static void randomRange(int N, int selectivityRows, int[] r) {
        int low = SECURE_RANDOM.nextInt(Math.max(N - selectivityRows, 1));
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
        Collections.shuffle(list, SECURE_RANDOM);
        return list;
    }
}
