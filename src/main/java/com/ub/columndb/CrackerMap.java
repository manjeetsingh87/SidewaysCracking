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

    private final boolean enableSorting;
    private final int sortingThreshold;

    private final CrackerTape<Head> tape;
    private int tapePosition = 0;

    private boolean sorted;
    private final List<Tuple<Head, Tail>> map;
    private final TreeMap<Head, Partition> index;
    private int numSortedPartitions = 0;

    CrackerMap(List<Head> head, List<Tail> tail, CrackerTape<Head> tape) {
        this(head, tail, tape, false, false, 0);
    }

    CrackerMap(List<Head> head, List<Tail> tail, CrackerTape<Head> tape, boolean isSorted, boolean enableSorting, int sortingThreshold) {
        assert head.size() == tail.size();

        this.tape = tape;
        this.map = new ArrayList<>(head.size());
        this.index = new TreeMap<>();
        this.sorted = isSorted;
        this.enableSorting = enableSorting;
        this.sortingThreshold = sortingThreshold;

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

        int lowIdx = -1, highIdx = -1;

        if (sorted) {
            lowIdx = binarySearch(low);
            highIdx = binarySearch(high);
        } else if (index.isEmpty()) {
            int[] pieces = crackThree(low, high, 0, map.size() - 1, true);
            lowIdx = pieces[0] + 1;
            highIdx = pieces[1] + 1;
        } else {
            Map.Entry<Head, Partition> lowCeil = index.ceilingEntry(low);
            Map.Entry<Head, Partition> lowFloor = index.floorEntry(low);
            Map.Entry<Head, Partition> highCeil = index.ceilingEntry(high);
            Map.Entry<Head, Partition> highFloor = index.floorEntry(high);

            lowIdx = searchIfSorted(low, lowCeil, lowFloor);
            highIdx = searchIfSorted(high, highCeil, highFloor);

            if (lowIdx == -1 && highIdx == -1 && Objects.equals(lowCeil, highCeil) && Objects.equals(lowFloor, highFloor)) {
                int pLow = lowFloor == null ? 0 : lowFloor.getValue().pHigh;
                int pHigh = lowCeil == null ? this.map.size() - 1 : lowCeil.getValue().pHigh;
                int[] pieces = crackThree(low, high, pLow, pHigh, true);
                lowIdx = pieces[0] + 1;
                highIdx = pieces[1] + 1;
            } else {
                if (lowIdx == -1) lowIdx = findIndex(low, lowCeil, lowFloor) + 1;
                if (highIdx == -1) highIdx = findIndex(high, highCeil, highFloor) + 1;
            }
        }

        return (lowIdx < highIdx && lowIdx != -1 && highIdx != -1) ? map.subList(lowIdx, highIdx) : Collections.emptyList();
    }

    private int searchIfSorted(Head key, Map.Entry<Head, Partition> ceil, Map.Entry<Head, Partition> floor) {
        if (ceil != null && ceil.getValue().sorted && key.compareTo(ceil.getKey()) <= 0) {
            return binarySearch(ceil.getValue().pLow, ceil.getValue().pHigh, key);
        } else if (floor != null && floor.getValue().sorted && floor.getKey().equals(key)) {
            return binarySearch(floor.getValue().pLow, floor.getValue().pHigh, key);
        }
        return -1;
    }

    private int findIndex(Head key, Map.Entry<Head, Partition> ceil, Map.Entry<Head, Partition> floor) {
        if (ceil != null && floor != null) {
            int floorCompare = key.compareTo(floor.getKey());
            int ceilCompare = key.compareTo(ceil.getKey());
            if (floorCompare > 0 && ceilCompare < 0) { // in range
                return crackTwo(key, floor.getValue().pHigh, ceil.getValue().pHigh, true);
            } else if (floorCompare == 0 && ceilCompare == 0) { // exists
                return floor.getValue().pHigh;
            }
        } else if (ceil != null) {
            return crackTwo(key, 0, ceil.getValue().pHigh, true);
        } else if (floor != null) {
            return crackTwo(key, floor.getValue().pHigh, map.size() - 1, true);
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
        if (pieces[0] > 0) {
            index.put(low, new Partition(pLow, pieces[0], sortInThreshold(pLow, pieces[0])));
        }
        if (pieces[1] > 0) {
            index.put(high, new Partition(pieces[0] + 1, pieces[1], sortInThreshold(pieces[0] + 1, pieces[1])));
        }
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
        if (pieceIdx > 0) {
            index.put(key, new Partition(pLow, pieceIdx, sortInThreshold(pLow, pieceIdx)));
        }

        return pieceIdx;
    }

    private boolean sortInThreshold(int pLow, int pHigh) {
        if (enableSorting && Math.abs(pHigh - pLow) <= sortingThreshold) {
            map.subList(pLow, pHigh + 1)
                    .sort(Tuple::compareTo);
            numSortedPartitions++;
//            if (index.size() == numSortedPartitions
//                    && index.values().stream().allMatch(Partition::isSorted)) {
//                sorted = true;
//            }
            return true;
        }
        return false;
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

    private int binarySearch(Head key) {
        return binarySearch(0, map.size() - 1, key);
    }

    private int binarySearch(int fromIndex, int toIndex, Head key) {
        int low = fromIndex;
        int high = toIndex;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = map.get(mid).head.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }
        return low;
    }

    private static class Partition {
        int pLow; //<- update
        int pHigh;
        boolean sorted;

        Partition(int pLow, int pHigh, boolean sorted) {
            this.pLow = pLow;
            this.pHigh = pHigh;
            this.sorted = sorted;
        }

        boolean isSorted() {
            return sorted;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Partition{");
            sb.append("pLow=").append(pLow);
            sb.append(", pHigh=").append(pHigh);
            sb.append(", sorted=").append(sorted);
            sb.append('}');
            return sb.toString();
        }
    }
}