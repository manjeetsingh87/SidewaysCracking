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
import java.util.concurrent.TimeUnit;

import static bench.RangeUtils.randomList;
import static bench.RangeUtils.randomRange;

public class CrackerMapBenchmarks {
    private static final Logger LOG = LoggerFactory.getLogger(CrackerMapBenchmarks.class);

    private static final char SEPARATOR = ',';
    private static final String SELECTION_COL = "A";
    private static final String[] PROJECTION_COL = new String[]{"B"};

    public static void main(String[] args) throws IOException {
        LOG.info("Started.");
        long start = System.nanoTime();

        int warmUpSize = 1000;
        benchmark(warmUpSize);

        int size = 1_000_00;
        long[][] responseTimes = benchmark(size);

        output(responseTimes, "1.csv");
        LOG.info("Finished, total-time {}", TimeUnit.MINUTES.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
    }

    private static long[][] benchmark(int size) {

        List<Integer> colA = randomList(size, size);
        List<Integer> colP = Arrays.asList(new Integer[size]);
        ColumnDB<Integer> db = new ColumnDB<>();
        db.addColumn(SELECTION_COL, colA);
        for (String col : PROJECTION_COL) {
            db.addColumn(col, colP);
        }


        List<Integer> sortedA = new ArrayList<>(colA);
        sortedA.sort(Integer::compareTo);
        ColumnDB<Integer> sortedDB = new ColumnDB<>();
        sortedDB.addColumn(SELECTION_COL, sortedA, true);   // <-  SORTED!
        for (String col : PROJECTION_COL) {
            sortedDB.addColumn(col, colP);
        }

        int querySequenceSize = size;
        int[][] queryRanges = buildRanges(querySequenceSize);
        long[] queryResponseTimes = new long[querySequenceSize];
        long[] sortedQueryResponseTimes = new long[querySequenceSize];

        query(db, queryRanges, queryResponseTimes);
        query(sortedDB, queryRanges, sortedQueryResponseTimes);

        return new long[][]{queryResponseTimes, sortedQueryResponseTimes};
    }

    private static void query(ColumnDB<Integer> columnDB, int[][] queryRanges, long[] queryResponseTimes) {
        for (int i = 0; i < queryRanges.length; i++) {
            recordScanTime(columnDB, queryRanges, queryResponseTimes, i);
        }
    }

    private static List<List<Tuple<Integer, Integer>>> recordScanTime(ColumnDB<Integer> columnDB, int[][] queryRanges, long[] queryResponseTimes, int i) {
        long startTime = System.nanoTime();
        List<List<Tuple<Integer, Integer>>> result = columnDB.scan(SELECTION_COL, queryRanges[i][0], queryRanges[i][1], PROJECTION_COL);
        queryResponseTimes[i] = TimeUnit.MICROSECONDS.convert((System.nanoTime() - startTime), TimeUnit.NANOSECONDS);
        return result;
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

    private static int[][] buildRanges(int size) {
        int[][] ranges = new int[size][2];
        for (int[] range : ranges) {
            randomRange(size, range);
        }
        return ranges;
    }
}
