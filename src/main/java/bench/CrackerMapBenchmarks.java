package bench;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static bench.RangeUtils.randomRange;

public class CrackerMapBenchmarks {
    private static final Logger LOG = LoggerFactory.getLogger(CrackerMapBenchmarks.class);

    private static final char SEPARATOR = ',';

    private static final String SELECTION_COL = "A";
    private static final String[] PROJECTION_COL = new String[]{"B"};

    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;

//    public static void main(String[] args) throws IOException {
//        LOG.info("Started.");
//        long start = System.nanoTime();
//
//        int warmUpSize = 1000;
//        benchmark(warmUpSize, warmUpSize);
//
//        int size = 1_000_000_0;
//        long[][] responseTimes = benchmark(size, 1000);
//
//        output(responseTimes, "t.csv");
//        LOG.info("Finished, total-time {}", TimeUnit.MINUTES.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
//    }
//
//    static long[][] benchmark(int size, int querySequenceSize) {
//        List<Integer> tail = Arrays.asList(new Integer[size]);
//
//        ColumnDB db = new ColumnDB();
//        List<Integer> head = data(size);
//        db.addColumn(SELECTION_COL, head);
//        for (String col : PROJECTION_COL) {
//            db.addColumn(col, tail);
//        }
//
//
//        ColumnDB sortedDB = new ColumnDB();
//        List<Integer> sortedHead = new ArrayList<>(head);
//        long sortingTime = time(() -> sortedHead.sort(Integer::compareTo));
//        LOG.info("PreSorting time: {}", sortingTime);
//        sortedDB.addColumn(SELECTION_COL, sortedHead, true);   // <-  SORTED!
//        for (String col : PROJECTION_COL) {
//            sortedDB.addColumn(col, tail);
//        }
//
//        int[][] queryRanges = buildRanges(querySequenceSize);
//        long[] queryResponseTimes = new long[querySequenceSize];
//        long[] sortedQueryResponseTimes = new long[querySequenceSize];
//
//        query(db, queryRanges, queryResponseTimes);
//        query(sortedDB, queryRanges, sortedQueryResponseTimes);
//        sortedQueryResponseTimes[0] += sortingTime; // add time for pre-sorting
//
//        return new long[][]{queryResponseTimes, sortedQueryResponseTimes};
//    }
//
//    static void query(ColumnDB columnDB, int[][] queryRanges, long[] queryResponseTimes) {
//        for (int i = 0; i < queryRanges.length; i++) {
//            recordScanTime(columnDB, queryRanges, queryResponseTimes, i);
//        }
//    }
//
//    static List<List<Tuple<Integer, Integer>>> recordScanTime(ColumnDB columnDB, int[][] queryRanges, long[] queryResponseTimes, int i) {
//        return time(() -> columnDB.scan(SELECTION_COL, queryRanges[i][0], queryRanges[i][1], PROJECTION_COL), queryResponseTimes, i);
//    }
//
//    static void output(long[][] responseTimes, String file) throws IOException {
//        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
//            writer.append("QuerySequence").append(SEPARATOR).append("ResponseTime").append(SEPARATOR).append("Type");
//            writer.newLine();
//            long[] cracked = responseTimes[0];
//            long[] sorted = responseTimes[1];
//            for (int j = 0, i = 1; j < cracked.length; j++, i++) {
//                writer.append(String.valueOf(i));
//                writer.append(SEPARATOR);
//                writer.append(String.valueOf(cracked[j]));
//                writer.append(SEPARATOR);
//                writer.append("CRACKED");
//                writer.newLine();
//
//                writer.append(String.valueOf(i));
//                writer.append(SEPARATOR);
//                writer.append(String.valueOf(sorted[j]));
//                writer.append(SEPARATOR);
//                writer.append("SORTED");
//                writer.newLine();
//            }
//        }
//    }

    static int[][] buildRanges(int size) {
        int[][] ranges = new int[size][2];
        for (int[] range : ranges) {
            randomRange(size, range);
        }
        return ranges;
    }

    static int[][] buildRanges(int size, float selectivity) {
        int[][] ranges = new int[size][2];
        for (int[] range : ranges) {
            randomRange(size, selectivity, range);
        }
        return ranges;
    }

    static int[][] buildRanges(int size, int selectivityRows) {
        int[][] ranges = new int[size][2];
        for (int[] range : ranges) {
            randomRange(size, selectivityRows, range);
        }
        return ranges;
    }

    static long time(Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        return TIME_UNIT.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
    }

    static <T> T time(Supplier<T> supplier, long[] dest, int destIndex) {
        long start = System.nanoTime();
        T t = supplier.get();
        dest[destIndex] = TIME_UNIT.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        return t;
    }
}
