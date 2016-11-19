package com.ub.columndb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Use for aligning CrackerMaps belonging to same map Set(head column is same).
 */
class CrackerTape<H> {
    private final List<Log<H>> tape = new ArrayList<>();

    List<Log<H>> fromPosition(int pos) {
        int size = tape.size();
        if (pos < size) return tape.subList(pos, size);
        else return Collections.emptyList();
    }

    void addCrackThree(H low, H high, int pLow, int pHigh) {
        tape.add(new Log<>(Log.CrackingType.THREE, low, high, pLow, pHigh));
    }

    void addCrackTwo(H key, int pLow, int pHigh) {
        tape.add(new Log<>(Log.CrackingType.TWO, key, null, pLow, pHigh));
    }

    static class Log<H> {
        CrackingType type;
        H low, high;
        int pLow;
        int pHigh;

        Log(CrackingType type, H low, H high, int pLow, int pHigh) {
            this.type = type;
            this.low = low;
            this.high = high;
            this.pLow = pLow;
            this.pHigh = pHigh;
        }

        enum CrackingType {TWO, THREE}
    }
}
