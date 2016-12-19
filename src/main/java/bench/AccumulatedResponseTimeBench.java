package bench;


import com.ub.columndb.ColumnDB;
import com.ub.columndb.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static bench.RangeUtils.*;

public class AccumulatedResponseTimeBench {
    private static final Logger LOG = LoggerFactory.getLogger(AccumulatedResponseTimeBench.class);

    private static final char SEPARATOR = ',';

    private static final String SELECTION_COL = "A0";
    private static final String[] PROJECTION_COLS = new String[]{"A1", "A2"};
    private static final String TYPE = "SORTED";

    public static void main(String[] args) throws IOException, InterruptedException {
        LOG.info("Warm-up");
        int warmUpSize = 10000;
        int warmQuerySequenceSize = 10000;
        benchmark(warmUpSize, warmQuerySequenceSize, buildRanges(warmUpSize, warmQuerySequenceSize, 100));

        System.gc();
        Thread.sleep(5000);
        LOG.info("Starting");

        int size = 1_000_000_0;
        int querySequenceSize = 30000;
        long[][] responseTimes = benchmark(size, querySequenceSize, buildRanges(size, querySequenceSize, 10000));

        output(responseTimes, "acc" + TYPE + ".csv");
    }

    static long[][] benchmark(int size, int querySequenceSize, int[][] queryRanges) throws InterruptedException {
        List<Integer> head = data(size);
        List<Integer> tail = Arrays.asList(new Integer[size]);

        switch (TYPE) {
            case "CRACKED": {
                long[] queryResponseTimes = run(querySequenceSize, false, false, 0, queryRanges, head, tail);
                return new long[][]{queryResponseTimes};
            }
            case "HYBRID": {
                long[] hybridQueryResponseTimes = run(querySequenceSize, false, true, 10001, queryRanges, new ArrayList<>(head), tail);
                return new long[][]{hybridQueryResponseTimes};
            }
            case "SORTED": {
                long sortingTime = time(() -> head.sort(Integer::compareTo));
                LOG.info("PreSorting time: {}", sortingTime);
                long[] sortedQueryResponseTimes = run(querySequenceSize, true, false, 0, queryRanges, head, tail);
                sortedQueryResponseTimes[0] += sortingTime; // add time for pre-sorting
                return new long[][]{sortedQueryResponseTimes};
            }
        }
        return new long[][]{};
    }

    private static long[] run(int querySequenceSize, boolean isSorted, boolean enableSorting, int sortingThresh,
                              int[][] queryRanges, List<Integer> head, List<Integer> tail) {
        ColumnDB db = new ColumnDB(enableSorting, sortingThresh);
        db.addColumn(SELECTION_COL, head, isSorted);
        for (String projectionCol : PROJECTION_COLS) {
            db.addColumn(projectionCol, tail);
        }
        long[] queryResponseTimes = new long[querySequenceSize];
        query(db, queryRanges, PROJECTION_COLS, queryResponseTimes);
        return queryResponseTimes;
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
        BigInteger acc = BigInteger.ZERO;
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            writer.append("QuerySequence").append(SEPARATOR).append("ResponseTime").append(SEPARATOR).append("Type");
            writer.newLine();
            long[] times = responseTimes[0];
            for (int j = 0, i = 1; j < times.length; j++, i++) {
                acc = acc.add(BigInteger.valueOf(times[j]));

                writer.append(String.valueOf(i));
                writer.append(SEPARATOR);
                writer.append(acc.toString());
                writer.append(SEPARATOR);
                writer.append(TYPE);
                writer.newLine();
            }
        }
    }
}
