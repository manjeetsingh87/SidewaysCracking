package multicolumn;

import java.util.*;


public class TestCracker {
    public static void main(String[] args) {
        List<Integer> h = new ArrayList<>(Arrays.asList(13, 16, 4, 9, 2, 12, 7, 1, 19, 3, 14, 11, 8, 6));
        List<Integer> t = new ArrayList<>(Collections.nCopies(14, 0));

        CrackerMap<Integer, Integer> m = new CrackerMap<>(h, t);


        //input set to sort the head of the map
        printI(m.scan(10, 14, false, false));
        System.out.print(m);

        printI(m.scan(7, 16, false, true));
        System.out.print(m);

        printI(m.scan(8,14, true, true));
        System.out.print(m);
    }

    private static void printI(Iterator<Tuple<Integer, Integer>> scan) {

        System.out.println("----------------");
        int i = 0;
        while (scan.hasNext()) {
            System.out.println((i++) + "L: " + scan.next());
        }
        System.out.println("----------------");
    }
}