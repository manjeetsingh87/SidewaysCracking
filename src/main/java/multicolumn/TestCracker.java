package multicolumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class TestCracker {
    private static final Logger LOG = LoggerFactory.getLogger(TestCracker.class);

    public static void main(String[] args) {
        List<Integer> h = new ArrayList<>(Arrays.asList(13, 16, 4, 9, 2, 12, 7, 1, 19, 3, 14, 11, 8, 6));
        List<Integer> t = new ArrayList<>(Collections.nCopies(14, 0));

        CrackerMap<Integer, Integer> m = new CrackerMap<>(h, t);


//        m.scan(4, 11);
//        m.scan(6, 9);
//        m.scan(1, 3);

        m.scan(10, 14);
        m.scan(7, 16);
        m.scan(1, 3);
        //input set to sort the head of the map
//        LOG.info(m.toString());
//        LOG.info((m.scan(5, 16), "5 < H < 10"));
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