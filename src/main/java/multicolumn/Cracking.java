package multicolumn;


public interface Cracking<T extends Comparable<T>> {

    /**
     * Crack in three
     * Invariant: Arr[pLow, lt - 1] < low, low <= Arr[lt, gt] <= high, Arr[gt + 1, pHigh] >= high
     */
    default int[] crackInThree(int pLow, int pHigh, T low, T high) {
        int lt = pLow, gt = pHigh, i = lt;
        while (i <= gt) {
            if (value(i).compareTo(low) < 0) {
                exchange(i, lt);
                ++lt;
            } else if (value(i).compareTo(high) >= 0) {
                while (value(gt).compareTo(high) > 0 && i < gt) --gt;
                exchange(i, gt);
                --gt;
                if (value(i).compareTo(low) < 0) {
                    exchange(i, lt);
                    ++lt;
                }
            }
            ++i;
        }
        return new int[]{lt, gt};
    }

    /**
     * Crack in two
     * Invariant: Arr[pLow, lt - 1] < med, Arr[lt, gt] = med, Arr[gt + 1, pHigh] > med
     */
    default int crackInTwo(int pLow, int pHigh, T med) {
        int i = pLow;
        int lt = pLow, gt = pHigh;

        while (i <= gt) {
            int compareTo = value(i).compareTo(med);
            if (compareTo < 0) {
                exchange(lt++, i++);
            } else if (compareTo > 0) {
                exchange(i, gt--);
            } else {
                i++;
            }
        }

        return lt;
    }

    T value(int i);

    void exchange(int i, int j);
}
