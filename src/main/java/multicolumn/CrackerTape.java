package multicolumn;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Use for aligning CrackerMaps belonging to same map Set(head column is same).
 */
public class CrackerTape {
    List<Node> log = new LinkedList<>();

    public ListIterator<Node> alignFrom(int prevPosition) {
        return log.listIterator(prevPosition);
    }



    public static class Node {
        public long low;
        public long high;
    }
}
