package com.ub.columndb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnDB<T extends Comparable<T>> {
    private final Map<String, List<T>> columns = new HashMap<>();
    private final Map<String, Boolean> sortedColumns = new HashMap<>();
    private final Map<String, Map<String, CrackerMap<T>>> mapSets = new HashMap<>();
    private final Map<String, CrackerTape<T>> mapTapes = new HashMap<>();

    public ColumnDB() {
    }

    public void addColumn(String col, List<T> colData) {
        addColumn(col, colData, false);
    }

    public void addColumn(String col, List<T> colData, boolean isSorted) {
        columns.put(col, colData);
        sortedColumns.put(col, isSorted);
    }

    public List<Tuple<T, T>> scan(String selectionCol, T low, T high, String projectionCol) {
        return mapSets
                .computeIfAbsent(selectionCol, s -> new HashMap<>())
                .computeIfAbsent(projectionCol,
                        s -> new CrackerMap<>(
                                columns.get(selectionCol),
                                columns.get(projectionCol),
                                mapTapes.computeIfAbsent(selectionCol, x -> new CrackerTape<>()),
                                sortedColumns.get(selectionCol)))
                .scan(low, high);
    }

    // multi-projection queries, using multiple CrackerMaps
    // Select B, C from R where A < k, needs M-AB and M-AC
    public List<List<Tuple<T, T>>> scan(String selectionCol, T low, T high, String... projectionCols) {
        List<List<Tuple<T, T>>> projectionTuples = new ArrayList<>();
        for (String projectionCol : projectionCols) {
            List<Tuple<T, T>> r = scan(selectionCol, low, high, projectionCol);
            if (!r.isEmpty()) projectionTuples.add(r);
        }
        return projectionTuples;
    }

    // For multi-select queries, using multiple CrackerMaps and BitVectors.
    // Ex: SELECT D from R where 3<A<10 AND 4<B<9 AND 1<C<8
    public List<List<T>> scan(String col, String colA, long lowA, long highA, String colB, long lowB, long highB) {
        return null;
    }
}
