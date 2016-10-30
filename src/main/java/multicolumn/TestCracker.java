package multicolumn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCracker {
	public static void main(String[] args) {
        List<Integer> h = new ArrayList<>(20);
        List<Integer> t = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            h.add(i);
            t.add(i);
        }
        Collections.shuffle(h);
        Collections.shuffle(t);
        CrackerMap<Integer, Integer> m = new CrackerMap<>(h, t);
        
        //input set to sort the head of the map
        m.scan(1, 4);
        m.scan(1, 2);
        m.scan(1, 3);
        m.scan(2, 4);
        m.scan(4, 6);
        m.scan(5, 6);
        m.scan(6, 7);
        m.scan(7, 16);
        m.scan(10, 14);
        m.scan(10, 12);
        m.scan(10, 11);
        m.scan(11, 12);
        m.scan(12, 14);
        m.scan(13, 14);
        m.scan(15, 19);
        m.scan(16, 18);
        m.scan(18, 19);
        m.scan(8, 10);
        m.scan(9, 10);
        int i = 0;
        for (Tuple<Integer, Integer> tu : m) {
            System.out.println((i++) + "L: " + tu);
        }
    }
}