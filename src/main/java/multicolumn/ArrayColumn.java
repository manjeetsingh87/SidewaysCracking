package multicolumn;

import java.util.Collections;
import java.util.List;

class ArrayColumn implements Cracking<Integer> {
    private List<Integer> col;

    ArrayColumn(List<Integer> l) {
        col = l;
    }

    @Override
    public Integer value(int i) {
        return col.get(i);
    }

    @Override
    public void exchange(int i, int j) {
        Collections.swap(col, i, j);
    }

    public List<Integer> range(int from, int to) {
        return col.subList(from, to + 1);
    }
}