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

public class HybridThresholdBench {
    private static final Logger LOG = LoggerFactory.getLogger(HybridThresholdBench.class);

    private static final char SEPARATOR = ',';

    private static final String SELECTION_COL = "A0";
    private static final String[] PROJECTION_COLS = new String[]{"A1", "A2"};
    private static final int SELECTIVITY = 100000;

    private static final int SORTING_THRESHOLD = 1000_000;


    public static void main(String[] args) throws IOException, InterruptedException {
        int warmUpSize = 10000;
        int warmQuerySequenceSize = 1000;
        benchmark(warmUpSize, warmQuerySequenceSize, buildRanges(warmUpSize, warmQuerySequenceSize, 100));

        System.gc();
        Thread.sleep(5000);
        LOG.info("Starting");

        int size = 1_000_000_0;
        int querySequenceSize = 30000;
        long[][] responseTimes = benchmark(size, querySequenceSize, buildRanges(size, querySequenceSize, SELECTIVITY));

        output(responseTimes, "T" + SORTING_THRESHOLD + ".csv");
    }

    static long[][] benchmark(int size, int querySequenceSize, int[][] queryRanges) throws InterruptedException {
        List<Integer> head = data(size);
        List<Integer> tail = Arrays.asList(new Integer[size]);

        long[] hybridQueryResponseTimes = run(querySequenceSize, queryRanges, new ArrayList<>(head), tail);

        return new long[][]{hybridQueryResponseTimes};
    }

    private static long[] run(int querySequenceSize,
                              int[][] queryRanges, List<Integer> head, List<Integer> tail) {
        ColumnDB db = new ColumnDB(true, SORTING_THRESHOLD);
        db.addColumn(SELECTION_COL, head, false);
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
            long[] cracked = responseTimes[0];
            for (int j = 0, i = 1; j < cracked.length; j++, i++) {
                acc = acc.add(BigInteger.valueOf(cracked[j]));

                writer.append(String.valueOf(i));
                writer.append(SEPARATOR);
                writer.append(acc.toString());
                writer.append(SEPARATOR);
                writer.append(Integer.toString(SORTING_THRESHOLD));
                writer.newLine();

            }
        }
    }
}
