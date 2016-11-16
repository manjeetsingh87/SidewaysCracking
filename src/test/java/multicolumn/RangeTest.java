package multicolumn;


import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;


public class RangeTest {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static int randomInt(int N) {
        return SECURE_RANDOM.nextInt(N);
    }

    public static int[] randomRange(int N) {
        int low = 0, high = 0;
        while (low >= high) {
            int i = SECURE_RANDOM.nextInt(N);
            int j = SECURE_RANDOM.nextInt(N);
            low = Math.min(i, j);
            high = Math.max(i, j);
        }
        return new int[]{low, high};
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
