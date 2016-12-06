package bench;

import com.ub.columndb.ColumnDB;
import com.ub.columndb.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static bench.CrackerMapBenchmarks.buildRanges;
import static bench.CrackerMapBenchmarks.time;
import static bench.RangeUtils.data;


public class SelectivityBench {
    private static final Logger LOG = LoggerFactory.getLogger(SelectivityBench.class);

    private static final char SEPARATOR = ',';

    private static final String SELECTION_COL = "A0";
    private static final String[] PROJECTION_COLS = new String[]{"A1", "A2"};
    private static final int SELECTIVITY_ROWS = 10000;


    public static void main(String[] args) throws IOException {
        int warmUpSize = 1000;
        benchmark(warmUpSize, warmUpSize, 1000);

        int size = 1_000_000_0;
        long[][] responseTimes = benchmark(size, 50000, SELECTIVITY_ROWS);

        output(responseTimes, "S" + SELECTIVITY_ROWS + ".csv");
    }

    static long[][] benchmark(int size, int querySequenceSize, int selectivity) {
        int[][] queryRanges = buildRanges(querySequenceSize, selectivity);
        long[] queryResponseTimes = new long[querySequenceSize];
        long[] sortedQueryResponseTimes = new long[querySequenceSize];

        List<Integer> tail = Arrays.asList(new Integer[size]);

        ColumnDB db = new ColumnDB();
        List<Integer> head = data(size);
        db.addColumn(SELECTION_COL, head);
        for (String projectionCol : PROJECTION_COLS) {
            db.addColumn(projectionCol, tail);
        }
        query(db, queryRanges, PROJECTION_COLS, queryResponseTimes);


        ColumnDB sortedDB = new ColumnDB();
        List<Integer> sortedHead = new ArrayList<>(head);
        long sortingTime = time(() -> sortedHead.sort(Integer::compareTo));
        LOG.info("PreSorting time: {}", sortingTime);
        sortedDB.addColumn(SELECTION_COL, sortedHead, true);   // <-  SORTED!
        for (String projectionCol : PROJECTION_COLS) {
            sortedDB.addColumn(projectionCol, tail);
        }
        query(sortedDB, queryRanges, PROJECTION_COLS, sortedQueryResponseTimes);
        sortedQueryResponseTimes[0] += sortingTime; // add time for pre-sorting

        return new long[][]{queryResponseTimes, sortedQueryResponseTimes};
    }

    static void query(ColumnDB columnDB, int[][] queryRanges, String[] projectionCols, long[] queryResponseTimes) {
        for (int i = 0; i < queryRanges.length; i++) {
            recordScanTime(columnDB, queryRanges, projectionCols, queryResponseTimes, i);
        }
    }

    static List<List<Tuple<Integer, Integer>>> recordScanTime(ColumnDB columnDB, int[][] queryRanges, String[] projectionCols, long[] queryResponseTimes, int i) {
        return time(() -> columnDB.scan(SELECTION_COL, queryRanges[i][0], queryRanges[i][1], projectionCols), queryResponseTimes, i);
    }

    private static void output(long[][] responseTimes, String file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            writer.append("QuerySequence").append(SEPARATOR).append("ResponseTime").append(SEPARATOR).append("Type");
            writer.newLine();
            long[] cracked = responseTimes[0];
            long[] sorted = responseTimes[1];
            for (int j = 0, i = 1; j < cracked.length; j++, i++) {
                writer.append(String.valueOf(i));
                writer.append(SEPARATOR);
                writer.append(String.valueOf(cracked[j]));
                writer.append(SEPARATOR);
                writer.append("CRACKED");
                writer.newLine();

                writer.append(String.valueOf(i));
                writer.append(SEPARATOR);
                writer.append(String.valueOf(sorted[j]));
                writer.append(SEPARATOR);
                writer.append("SORTED");
                writer.newLine();
            }
        }
    }
}
