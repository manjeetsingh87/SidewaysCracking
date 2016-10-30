package multicolumn;

import static java.util.Collections.swap;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

/**
 * Two column index.
 */
public class CrackerMap<Head extends Comparable<Head>, Tail> implements Iterable<Tuple<Head, Tail>> {
    private List<Tuple<Head, Tail>> map;
    @SuppressWarnings("rawtypes")
	private TreeMap<Head, Piece> index;

    // private int tapePosition = 0;

    public CrackerMap(List<Head> head, List<Tail> tail) {
        this.map = new ArrayList<>(head.size());
        this.index = new TreeMap<>();

        Iterator<Head> headIterator = head.iterator();
        Iterator<Tail> tailIterator = tail.iterator();

        while (headIterator.hasNext() && tailIterator.hasNext()) {
            map.add(new Tuple<>(headIterator.next(), tailIterator.next()));
        }
    }

    public Iterator<Tuple<Head, Tail>> scan(Head low, Head high) {
//        Align to Tape.
//        CrackerTape crackerTape = registry.tapeFor();
//        ListIterator<CrackerTape.Node> pendingAlignments = crackerTape.alignFrom(tapePosition);
//        while (pendingAlignments.hasNext()) {
//            CrackerTape.Node node = pendingAlignments.next();
//            crack(node.low, node.high);
//        }

        // query map to find contiguous area.
        // if exits return iterator
        // else : crack & update index
        if (index.isEmpty()) {
            int[] pieces = crackInThree(0, map.size() - 1, low, high, true, true);
            index.put(low, new Piece<>(low, pieces[0], true, true));
            index.put(high, new Piece<>(high, pieces[1], true, true));
            return map.subList(pieces[0], pieces[1] + 1).iterator();
        }

        Integer lowIdx = findIndex(low);
        Integer highIdx = findIndex(high);
        if (lowIdx != null && highIdx != null) {
            return map.subList(lowIdx, highIdx).iterator();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
	private Integer findIndex(Head key) {
        Integer idx = null;
        Map.Entry<Head, Piece> ceil = index.ceilingEntry(key);
        Map.Entry<Head, Piece> floor = index.floorEntry(key);
        if (ceil != null && floor != null) {
            Head hF = floor.getKey(), hC = ceil.getKey();
            if (inRange(key, hF, hC)) {
                int pieceIdx = crackInTwo(floor.getValue().position, ceil.getValue().position, key, true);
                index.put(key, new Piece<>(key, pieceIdx, true, true));
                idx = pieceIdx;
            } else {
                // TODO: always in the range?
                // TODO: ceil & floor equals key?
                idx = ceil.getValue().position;
            }
        } else if (ceil != null && floor == null) {
            int pieceIdx = crackInTwo(0, ceil.getValue().position, key, true);
            index.put(key, new Piece<>(key, pieceIdx, true, true));
            idx = pieceIdx;
        } else if (floor != null && ceil == null) {
            int pieceIdx = crackInTwo(floor.getValue().position, map.size() - 1, key, true);
            index.put(key, new Piece<>(key, pieceIdx, true, true));
            idx = pieceIdx;
        } else {
            // TODO: both null , 3-way? should not occur?
            System.out.println("findIndex: floor & ceil are null. Missing case?");
        }
        return idx;
    }

    private boolean inRange(Head low, Head floor, Head ceil) {
        int floorCompare = low.compareTo(floor);
        int ceilCompare = low.compareTo(ceil);
        return floorCompare > 0 && ceilCompare < 0;
    }

    // For multi-select queries.
    // Ex: SELECT D from R where 3<A<10 AND 4<B<9 AND 1<C<8
    public BitSet create_bv(long low, long high) {
        BitSet bitSet = new BitSet(map.size());
        // scan and set bits.

        return bitSet;
    }

    @SuppressWarnings("unused")
	private void crack(long low, long high) {

    }


    private int crackInTwo(int pLow, int pHigh, Head med, boolean inc) {
        int x1 = pLow, x2 = pHigh;

        BiFunction<Head, Head, Boolean> theta1 = (head1, head2) -> {
            int compareTo = head1.compareTo(head2);
            return !inc ? compareTo < 0 : compareTo <= 0;
        };

        BiFunction<Head, Head, Boolean> theta2 = (head1, head2) -> {
            int compareTo = head1.compareTo(head2);
            return !inc ? compareTo >= 0 : compareTo > 0;
        };

        while (x1 < x2) {
            if (theta1.apply(value(x1), med)) {
                x1++;
            } else {
                while (theta2.apply(value(x2), med) && x2 > x1) {
                    x2--;
                }
                exchange(x1, x2);
                x1++;
                x2--;
            }
        }
        // TODO:

        return x2;
    }

    private int[] crackInThree(int pLow, int pHigh, Head low, Head high, boolean incL, boolean incH) {
        int x1 = pLow, x2 = pHigh;

        while (theta1(value(x2), high, incL, incH) && x2 > x1) {
            x2--;
        }

        int x3 = x2;

        while (theta2(value(x3), low, incL, incH) && x3 > x1) {
            if (theta1(value(x3), high, incL, incH)) {
                exchange(x2, x3);
                x2--;
            }
            x3--;
        }

        while (x1 <= x3) {
            if (theta3(value(x1), low, incL, incH)) {
                x1++;
            } else {
                exchange(x1, x3);
                while (theta2(value(x3), low, incL, incH) && x3 > x1) {
                    if (theta1(value(x3), high, incL, incH)) {
                        exchange(x2, x3);
                        x2--;
                    }
                    x3--;
                }
            }
        }

        return new int[]{x1, x2};
    }

    private void exchange(int i, int j) {
        swap(map, i, j);
    }

    private Head value(int i) {
        return map.get(i).head;
    }

    private boolean theta1(Head head1, Head head2, boolean incL, boolean incH) {
        int compareTo = head1.compareTo(head2);
        if (incL && incH) {
            return compareTo > 0;
        } else if (incL && !incH) {
            return compareTo >= 0;
        } else if (!incL && incH) {
            return compareTo > 0;
        } else {
            return compareTo >= 0;
        }
    }

    private boolean theta2(Head head1, Head head2, boolean incL, boolean incH) {
        int compareTo = head1.compareTo(head2);
        if (incL && incH) {
            return compareTo >= 0;
        } else if (incL && !incH) {
            return compareTo >= 0;
        } else if (!incL && incH) {
            return compareTo > 0;
        } else {
            return compareTo > 0;
        }
    }

    private boolean theta3(Head head1, Head head2, boolean incL, boolean incH) {
        int compareTo = head1.compareTo(head2);
        if (incL && incH) {
            return compareTo < 0;
        } else if (incL && !incH) {
            return compareTo < 0;
        } else if (!incL && incH) {
            return compareTo <= 0;
        } else {
            return compareTo < 0;
        }
    }

    @Override
    public Iterator<Tuple<Head, Tail>> iterator() {
        return map.iterator();
    }
}