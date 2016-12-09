package com.ub.columndb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ColumnDB {
    private static final Logger LOG = LoggerFactory.getLogger(ColumnDB.class);

    private final Map<String, List<? extends Comparable>> columns = new HashMap<>();
    private final Map<String, Boolean> sortedColumns = new HashMap<>();
    private final Map<String, Map<String, CrackerMap<? extends Comparable, ?>>> mapSets = new HashMap<>();
    private final Map<String, CrackerTape<? extends Comparable>> mapTapes = new HashMap<>();
    private final int sortingThreshold;
    private final boolean sortingEnabled;

    public ColumnDB() {
        this(false, 0);
    }

    public ColumnDB(boolean sortingEnabled, int sortingThreshold) {
        this.sortingEnabled = sortingEnabled;
        this.sortingThreshold = sortingThreshold;
    }

    public <T extends Comparable<T>> void addColumn(String col, List<T> colData) {
        addColumn(col, colData, false);
    }

    public <T extends Comparable<T>> void addColumn(String col, List<T> colData, boolean isSorted) {
        columns.put(col, colData);
        sortedColumns.put(col, isSorted);
    }

    @SuppressWarnings("unchecked")
    public <H extends Comparable<H>, T> List<Tuple<H, T>> scan(String selectionCol, H low, H high, String projectionCol) {
        CrackerMap<H, T> crackerMap =
                (CrackerMap<H, T>) mapSets
                        .computeIfAbsent(selectionCol, s -> new HashMap<>())
                        .computeIfAbsent(projectionCol,
                                s -> {
                                    long start = System.nanoTime();
                                    CrackerMap<H, T> map = new CrackerMap<>(
                                            (List<H>) columns.get(selectionCol),
                                            (List<T>) columns.get(projectionCol),
                                            (CrackerTape<H>) mapTapes.computeIfAbsent(selectionCol, x -> new CrackerTape<H>()),
                                            sortedColumns.get(selectionCol),
                                            sortingEnabled, sortingThreshold);
                                    LOG.info("Building CrackerMap[{}, {}] took {} millis", selectionCol, projectionCol,
                                            TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
                                    return map;
                                });
        return crackerMap
                .scan(low, high);
    }

    // multi-projection queries, using multiple CrackerMaps
    // Select B, C from R where A < k, needs M-AB and M-AC
    public <H extends Comparable<H>, T> List<List<Tuple<H, T>>> scan(String selectionCol, H low, H high, String... projectionCols) {
        List<List<Tuple<H, T>>> projectionTuples = new ArrayList<>(projectionCols.length);
        for (String projectionCol : projectionCols) {
            List<Tuple<H, T>> r = scan(selectionCol, low, high, projectionCol);
            if (!r.isEmpty()) projectionTuples.add(r);
        }
        return projectionTuples;
    }

}
