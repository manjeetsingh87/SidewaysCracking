package com.ub.columndb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;

import static java.util.Collections.swap;

/**
 * Two column index.
 */
class CrackerMap<T extends Comparable<T>> implements Cracking<T>, Iterable<Tuple<T, T>> {
    private static final Logger LOG = LoggerFactory.getLogger(CrackerMap.class);
    private static final Marker QUERY_MARKER = MarkerFactory.getMarker("QUERY");

    private final CrackerTape<T> tape;
    private int tapePosition = 0;


    private final boolean sorted;
    private final List<Tuple<T, T>> map;
    private final TreeMap<T, Integer> index;

    CrackerMap(List<T> head, List<T> tail, CrackerTape<T> tape) {
        this(head, tail, tape, false);
    }

    CrackerMap(List<T> head, List<T> tail, CrackerTape<T> tape, boolean isSorted) {
        this.tape = tape;
        this.map = new ArrayList<>(head.size());
        this.index = new TreeMap<>();
        this.sorted = isSorted;

        Iterator<T> headIterator = head.iterator();
        Iterator<T> tailIterator = tail.iterator();

        while (headIterator.hasNext() && tailIterator.hasNext()) {
            map.add(new Tuple<>(headIterator.next(), tailIterator.next()));
        }
    }

    List<Tuple<T, T>> scan(T low, T high) {
        // align to tape.
        for (CrackerTape.Log<T> log : tape.fromPosition(tapePosition)) {
            tapePosition++;
            switch (log.type) {
                case TWO: {
                    crackTwo(log.low, log.pLow, log.pHigh, false);
                    break;
                }
                case THREE: {
                    crackThree(log.low, log.high, log.pLow, log.pHigh, false);
                    break;
                }
            }
        }

        int lowIdx, highIdx;

        if (sorted) {
            lowIdx = Collections.binarySearch(map, new Tuple<>(low, null));
            if (lowIdx < 0) lowIdx = -(lowIdx + 1);
            highIdx = Collections.binarySearch(map, new Tuple<>(high, null));
            if (highIdx < 0) highIdx = -(highIdx + 1);
        } else if (index.isEmpty()) {
            int[] pieces = crackThree(low, high, 0, map.size() - 1, true);
            lowIdx = pieces[0];
            highIdx = pieces[1];
        } else {
            Map.Entry<T, Integer> lowCeil = index.ceilingEntry(low);
            Map.Entry<T, Integer> lowFloor = index.floorEntry(low);
            Map.Entry<T, Integer> highCeil = index.ceilingEntry(high);
            Map.Entry<T, Integer> highFloor = index.floorEntry(high);

            if (Objects.equals(lowCeil, highCeil) && Objects.equals(lowFloor, highFloor)) {
                int pLow = lowFloor == null ? 0 : lowFloor.getValue();
                int pHigh = lowCeil == null ? map.size() - 1 : lowCeil.getValue();
                int[] pieces = crackThree(low, high, pLow, pHigh, true);
                lowIdx = pieces[0];
                highIdx = pieces[1];
            } else {
                lowIdx = findIndex(low, lowCeil, lowFloor);
                highIdx = findIndex(high, highCeil, highFloor);
            }
        }

        return (lowIdx < highIdx && lowIdx != -1 && highIdx != -1) ? map.subList(lowIdx, highIdx) : Collections.emptyList();
    }

    private int findIndex(T key, Map.Entry<T, Integer> ceil, Map.Entry<T, Integer> floor) {
        if (ceil != null && floor != null) {
            int floorCompare = key.compareTo(floor.getKey());
            int ceilCompare = key.compareTo(ceil.getKey());
            if (floorCompare > 0 && ceilCompare < 0) { // in range
                return crackTwo(key, floor.getValue() + 1, ceil.getValue(), true);
            } else if (floorCompare == 0 && ceilCompare == 0) { // exists
                return floor.getValue() + 1;
            }
        } else if (ceil != null) {
            return crackTwo(key, 0, ceil.getValue(), true);
        } else if (floor != null) {
            return crackTwo(key, floor.getValue() + 1, map.size() - 1, true);
        }
        return -1;
    }

    private int[] crackThree(T low, T high, int pLow, int pHigh, boolean updateTape) {
        LOG.debug(QUERY_MARKER, "crackInThree: {} < H < {}", low, high);

        int[] pieces = crackInThree(pLow, pHigh, low, high);
        if (updateTape) {
            // add tape entry.
            tape.addCrackThree(low, high, pLow, pHigh);
            tapePosition++;
        }
        // update index.
        index.put(low, Math.max(0, pieces[0] - 1));
        index.put(high, pieces[1]);
        return pieces;
    }

    private int crackTwo(T key, int pLow, int pHigh, boolean updateTape) {
        LOG.debug(QUERY_MARKER, "crackInTwo: {}", key);
        int pieceIdx = crackInTwo(pLow, pHigh, key);
        if (updateTape) {
            // add tape entry.
            tape.addCrackTwo(key, pLow, pHigh);
            tapePosition++;
        }
        // update index.
        index.put(key, Math.max(0, pieceIdx - 1));
        return pieceIdx;
    }

    public void exchange(int i, int j) {
        if (i != j) swap(map, i, j);
    }

    public T value(int i) {
        return map.get(i).head;
    }

    @Override
    public Iterator<Tuple<T, T>> iterator() {
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