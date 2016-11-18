package multicolumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;

import static java.util.Collections.swap;

/**
 * Two column index.
 */
public class CrackerMap<Head extends Comparable<Head>, Tail> implements Cracking<Head>, Iterable<Tuple<Head, Tail>> {
    private static final Logger LOG = LoggerFactory.getLogger(CrackerMap.class);
    private static final Marker QUERY_MARKER = MarkerFactory.getMarker("QUERY");
    //    private static final Marker MAP_MARKER = MarkerFactory.getMarker("CRACKER_MAP");
    private final boolean sorted;
    private final List<Tuple<Head, Tail>> map;
    private final TreeMap<Head, Integer> index;

    // private int tapePosition = 0;

    public CrackerMap(List<Head> head, List<Tail> tail) {
        this(head, tail, false);
    }

    public CrackerMap(List<Head> head, List<Tail> tail, boolean sorted) {
        this.map = new ArrayList<>(head.size());
        this.index = new TreeMap<>();
        this.sorted = sorted;

        Iterator<Head> headIterator = head.iterator();
        Iterator<Tail> tailIterator = tail.iterator();

        while (headIterator.hasNext() && tailIterator.hasNext()) {
            map.add(new Tuple<>(headIterator.next(), tailIterator.next()));
        }
    }

    public List<Tuple<Head, Tail>> scan(Head low, Head high) {
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
        Integer lowIdx = null, highIdx = null;
        try {
            if (sorted) {
                lowIdx = Collections.binarySearch(map, new Tuple<>(low, null));
                if (lowIdx < 0) lowIdx = -(lowIdx + 1);
                highIdx = Collections.binarySearch(map, new Tuple<>(high, null));
                if (highIdx < 0) highIdx = -(highIdx + 1);
            } else if (index.isEmpty()) {
                LOG.debug(QUERY_MARKER, "crackInThree: {} < H < {}", low, high);
                int[] pieces = crackInThree(0, map.size() - 1, low, high);
                index.put(low, Math.max(0, pieces[0] - 1));
                index.put(high, pieces[1]);
                lowIdx = pieces[0];
                highIdx = pieces[1];
            } else {
                Map.Entry<Head, Integer> lowCeil = index.ceilingEntry(low);
                Map.Entry<Head, Integer> lowFloor = index.floorEntry(low);
                Map.Entry<Head, Integer> highCeil = index.ceilingEntry(high);
                Map.Entry<Head, Integer> highFloor = index.floorEntry(high);

                if (Objects.equals(lowCeil, highCeil) && Objects.equals(lowFloor, highFloor)) {
                    LOG.debug(QUERY_MARKER, "crackInThree: {} < H < {}", low, high);
                    int pLow = lowFloor == null ? 0 : lowFloor.getValue();
                    int pHigh = lowCeil == null ? map.size() - 1 : lowCeil.getValue();
                    int[] pieces = crackInThree(pLow, pHigh, low, high);
                    index.put(low, Math.max(0, pieces[0] - 1));
                    index.put(high, pieces[1]);
                    lowIdx = pieces[0];
                    highIdx = pieces[1];
                } else {
                    lowIdx = findIndex(low, lowCeil, lowFloor);
                    highIdx = findIndex(high, highCeil, highFloor);
                }
            }
        } catch (Exception e) {
            LOG.error("{}", e);
        }
        return (lowIdx != null && highIdx != null && lowIdx < highIdx) ? map.subList(lowIdx, highIdx) : Collections.emptyList();
    }

    private Integer findIndex(Head key, Map.Entry<Head, Integer> ceil, Map.Entry<Head, Integer> floor) {
        Integer idx = null;

        if (ceil != null && floor != null) {
            int floorCompare = key.compareTo(floor.getKey());
            int ceilCompare = key.compareTo(ceil.getKey());
            if (floorCompare > 0 && ceilCompare < 0) { // in range
                LOG.debug(QUERY_MARKER, "crackInTwo: {}", key);
                int pieceIdx = crackInTwo(floor.getValue() + 1, ceil.getValue(), key);
                index.put(key, pieceIdx - 1);
                idx = pieceIdx;
            } else if (floorCompare == 0 && ceilCompare == 0) { // exists
                idx = floor.getValue() + 1;
            }
        } else if (ceil != null) {
            LOG.debug(QUERY_MARKER, "crackInTwo: {}", key);
            int pieceIdx = crackInTwo(0, ceil.getValue(), key);
            index.put(key, Math.max(0, pieceIdx - 1));
            idx = pieceIdx;
        } else if (floor != null) {
            LOG.debug(QUERY_MARKER, "crackInTwo: {}", key);
            int pieceIdx = crackInTwo(floor.getValue() + 1, map.size() - 1, key);
            index.put(key, Math.max(0, pieceIdx - 1));
            idx = pieceIdx;
        }
        return idx;
    }

    public void exchange(int i, int j) {
        if (i != j) swap(map, i, j);
    }

    public Head value(int i) {
        return map.get(i).head;
    }

    @Override
    public Iterator<Tuple<Head, Tail>> iterator() {
        return map.iterator();
    }

//    @Override
//    public String toString() {
//        return tuplesToString(map.iterator(), "MAP", index);
//    }
//
//    public static <H extends Comparable<H>, T> String tuplesToString(Iterator<Tuple<H, T>> iterator, String query) {
//        return tuplesToString(iterator, query, null);
//    }
//
//    private static <H extends Comparable<H>, T> String tuplesToString(Iterator<Tuple<H, T>> iterator, String query, TreeMap<H, Piece<H>> index) {
//        final StringBuilder sb = new StringBuilder();
//        sb.append("\n\n      ");
//        sb.append(query);
//        sb.append("\n      ___________\n");
//        int i = 0;
//        Map<Integer, List<Piece<H>>> indexPos = index != null ? index.values().stream().collect(Collectors.groupingBy(Piece::getPosition)) : Collections.emptyMap();
//        while (iterator.hasNext()) {
//            Tuple<H, T> t = iterator.next();
//            sb.append(MF.format(new Object[]{i, t.head, t.tail}));
//            if (!indexPos.isEmpty() && indexPos.containsKey(i)) {
//                sb.append(' ');
//                sb.append("< ");
//                String collect = indexPos.get(i).stream().map(Piece::getValue).map(Object::toString).collect(Collectors.joining(", "));
//                sb.append(collect);
//            }
//            sb.append('\n');
//            i++;
//        }
//        sb.append("      -----------\n");
//        return sb.toString();
//    }
//
//    private static final MessageFormat MF = new MessageFormat("{0, number, 00}: | {1, number, 00} | {2, number, 00} |", Locale.ENGLISH);
}