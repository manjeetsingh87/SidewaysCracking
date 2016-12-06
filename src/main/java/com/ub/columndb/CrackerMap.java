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
class CrackerMap<Head extends Comparable<Head>, Tail> implements Cracking<Head>, Iterable<Tuple<Head, Tail>> {
    private static final Logger LOG = LoggerFactory.getLogger(CrackerMap.class);
    private static final Marker QUERY_MARKER = MarkerFactory.getMarker("QUERY");

    private final CrackerTape<Head> tape;
    private int tapePosition = 0;

    private final boolean sorted;
    private final List<Tuple<Head, Tail>> map;
    private final TreeMap<Head, Integer> index;

    CrackerMap(List<Head> head, List<Tail> tail, CrackerTape<Head> tape) {
        this(head, tail, tape, false);
    }

    CrackerMap(List<Head> head, List<Tail> tail, CrackerTape<Head> tape, boolean isSorted) {
        assert head.size() == tail.size();

        this.tape = tape;
        this.map = new ArrayList<>(head.size());
        this.index = new TreeMap<>();
        this.sorted = isSorted;

        for (int i = 0; i < head.size(); i++) {
            map.add(new Tuple<>(head.get(i), tail.get(i)));
        }
    }

    List<Tuple<Head, Tail>> scan(Head low, Head high) {
        // align to tape.
        for (CrackerTape.Log<Head> log : tape.fromPosition(tapePosition)) {
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
            Map.Entry<Head, Integer> lowCeil = index.ceilingEntry(low);
            Map.Entry<Head, Integer> lowFloor = index.floorEntry(low);
            Map.Entry<Head, Integer> highCeil = index.ceilingEntry(high);
            Map.Entry<Head, Integer> highFloor = index.floorEntry(high);

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

        return (lowIdx < highIdx && lowIdx != -1 && highIdx != -1) ? map.subList(++lowIdx, ++highIdx) : Collections.emptyList();
    }

    private int findIndex(Head key, Map.Entry<Head, Integer> ceil, Map.Entry<Head, Integer> floor) {
        if (ceil != null && floor != null) {
            int floorCompare = key.compareTo(floor.getKey());
            int ceilCompare = key.compareTo(ceil.getKey());
            if (floorCompare > 0 && ceilCompare < 0) { // in range
                return crackTwo(key, floor.getValue(), ceil.getValue(), true);
            } else if (floorCompare == 0 && ceilCompare == 0) { // exists
                return floor.getValue();
            }
        } else if (ceil != null) {
            return crackTwo(key, 0, ceil.getValue(), true);
        } else if (floor != null) {
            return crackTwo(key, floor.getValue(), map.size() - 1, true);
        }
        return -1;
    }

    private int[] crackThree(Head low, Head high, int pLow, int pHigh, boolean updateTape) {
        LOG.debug(QUERY_MARKER, "crackInThree: {} < H < {}", low, high);

        int[] pieces = crackInThree(pLow, pHigh, low, high);
        if (updateTape) {
            // add tape entry.
            tape.addCrackThree(low, high, pLow, pHigh);
            tapePosition++;
        }
        // update index.
        if (pieces[0] > 0) index.put(low, pieces[0]);
        if (pieces[1] > 0) index.put(high, pieces[1]);
        return pieces;
    }

    private int crackTwo(Head key, int pLow, int pHigh, boolean updateTape) {
        LOG.debug(QUERY_MARKER, "crackInTwo: {}", key);
        int pieceIdx = crackInTwo(pLow, pHigh, key);
        if (updateTape) {
            // add tape entry.
            tape.addCrackTwo(key, pLow, pHigh);
            tapePosition++;
        }
        // update index.
        if (pieceIdx > 0) index.put(key, pieceIdx);

        return pieceIdx;
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
}