package com.ub.columndb;


public interface Cracking<T extends Comparable<T>> {

    /**
     * Crack in three
     * @return 2-len pivot index array [pivL, pivH] such that
     *                  arr[pLow, pivL] < low && low <= arr[pivL + 1, pivH] <= high && arr[pivH + 1, pHigh] >= high
     */
    default int[] crackInThree(int pLow, int pHigh, T low, T high) {
        int x1 = pLow, x2 = pHigh;

        while (x2 > x1 && value(x2).compareTo(high) >= 0)
            x2--;

        int x3 = x2;
        while (x3 > x1 && value(x3).compareTo(low) >= 0) {
            if (value(x3).compareTo(high) >= 0) {
                exchange(x2, x3);
                x2--;
            }
            x3--;
        }

        while (x1 <= x3) {
            if (value(x1).compareTo(low) < 0)
                x1++;
            else {
                exchange(x1, x3);
                while (x3 > x1 && value(x3).compareTo(low) >= 0) {
                    if (value(x3).compareTo(high) >= 0) {
                        exchange(x2, x3);
                        x2--;
                    }
                    x3--;
                }
            }
        }

        return new int[]{x3, x2};
    }


    /**
     * Crack in two
     *
     * @return pivot index such that arr[pLow, pivot] < med && arr[pivot, pHigh] >= med
     */
    default int crackInTwo(int pLow, int pHigh, T med) {
        int x1 = pLow, x2 = pHigh;
        while (x1 <= x2) {
            if (value(x1).compareTo(med) < 0)
                x1++;
            else {
                while (x2 >= x1 && value(x2).compareTo(med) >= 0) x2--;
                if (x1 < x2) {
                    exchange(x1, x2);
                    x1++;
                    x2--;
                }
            }
        }
        return --x1;
    }

    T value(int i);

    void exchange(int i, int j);
}
