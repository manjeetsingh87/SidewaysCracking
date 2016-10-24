package multicolumn;

import java.util.*;

/**
 * Two column index.
 */
public class CrackerMap {

    private List<long[]> map;               // head = long[0], tail = long[1]
    private TreeMap<Long, IndexNode> index; // RB-tree for now. Why TreeMap? lowerEntry, floorEntry, ceilingEntry, and higherEntry

    private int tapePosition = 0;
    private final Registry registry;

    public CrackerMap(List<Long> head, List<Long> tail, Registry registry) {
        this.map = new ArrayList<>();
        this.index = new TreeMap<>();
        this.registry = registry;

        Iterator headIterator = head.iterator();
        Iterator tailIterator = tail.iterator();

        while (headIterator.hasNext() && tailIterator.hasNext()) {
            map.add(new long[]{(long) headIterator.next(), (long) headIterator.next()});
        }
    }

    public Iterator<Long> scan(long low, long high) {
        // Align to Tape.
        CrackerTape crackerTape = registry.tapeFor();
        ListIterator<CrackerTape.Node> pendingAlignments = crackerTape.alignFrom(tapePosition);
        while (pendingAlignments.hasNext()) {
            CrackerTape.Node node = pendingAlignments.next();
            crack(node.low, node.high);
        }

        // query map to find contiguous area.
        // if exits return iterator
        // else : crack & update index
        crack(low, high);
        return null;
    }

    // For multi-select queries.
    // Ex: SELECT D from R where 3<A<10 AND 4<B<9 AND 1<C<8
    public BitSet create_bv(long low, long high) {
        BitSet bitSet = new BitSet(map.size());
        // scan and set bits.

        return bitSet;
    }

    private void crack(long low, long high) {

    }

    /**
     * Stores a position 'p' referring to the cracker column.
     * all values before position p are smaller than v and all values after p are greater.
     */
    private static class IndexNode implements Comparable<Long> {
        long value;                             // value in head
        long position;    // or index in map    // position of value in head
        boolean leftInclusive, rightInclusive;  // <= left-inclusive? or >= right-inclusive

        @Override
        public int compareTo(Long o) {
            return Long.compare(value, o);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IndexNode indexNode = (IndexNode) o;
            return value == indexNode.value &&
                    position == indexNode.position &&
                    leftInclusive == indexNode.leftInclusive &&
                    rightInclusive == indexNode.rightInclusive;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, position, leftInclusive, rightInclusive);
        }
    }
}
