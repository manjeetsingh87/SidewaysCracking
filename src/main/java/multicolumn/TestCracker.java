package multicolumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TestCracker {
    private static final Logger LOG = LoggerFactory.getLogger(TestCracker.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static void main(String[] args) {
        int n = 10;
        List<Integer> h = new ArrayList<>();
        List<Integer> t = new ArrayList<>(Collections.nCopies(n, 0));

        for (int i = 0; i < n; i++) {
            h.add(SECURE_RANDOM.nextInt(n));
        }

//        List<Integer> h = new ArrayList<>(Arrays.asList(13, 16, 4, 9, 2, 12, 7, 1, 19, 3, 14, 11, 8, 6));
//        List<Integer> t = new ArrayList<>(Collections.nCopies(14, 0));

        CrackerMap<Integer, Integer> m = new CrackerMap<>(h, t);

        LOG.info(m.toString());
        for (int k = 0; k < 10; k++) {
            int i = SECURE_RANDOM.nextInt(n);
            int j = SECURE_RANDOM.nextInt(n);
            int low = Math.min(i, j);
            int high = Math.max(i, j);
            m.scan(low, high);
            LOG.info(low + " < H < " + high);
        }
        LOG.info(m.toString());


//        LOG.info(m.toString());
//        LOG.info(tuplesToString(m.scan(5, 16), "5 < H < 10"));
//        LOG.info(m.toString());
//        LOG.info(tuplesToString(m.scan(10, 14), "10 < H < 14"));
//        LOG.info(m.toString());
//        LOG.info(tuplesToString(m.scan(7, 16), "7 < H < 16"));
//        LOG.info(m.toString());
//
//        LOG.info(tuplesToString(m.scan(1, 4), "1 < H < 4"));
//        LOG.info(m.toString());

    }
}