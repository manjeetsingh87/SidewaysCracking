package bench;

import java.security.SecureRandom;
import java.util.Arrays;
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

    public static void randomRange(int N, int[] r) {
        int low = 0, high = 0;
        while (low >= high) {
            int[] ints = SECURE_RANDOM.ints(2, 0, N).toArray();
            low = Math.min(ints[0], ints[1]);
            high = Math.max(ints[0], ints[1]);
        }
        r[0] = low;
        r[1] = high;
    }

    public static int[] randomArray(int size, int max) {
        int[] col = new int[size];
        for (int i = 0; i < size; i++) {
            col[i] = (SECURE_RANDOM.nextInt(max));
        }
        return col;
    }

    public static List<Integer> randomList(int size, int max) {
        Integer[] col = new Integer[size];
        for (int i = 0; i < size; i++) {
            col[i] = (SECURE_RANDOM.nextInt(max));
        }
        return Arrays.asList(col);
    }
}
