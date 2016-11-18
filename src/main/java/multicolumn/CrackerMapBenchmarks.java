package multicolumn;


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

import static multicolumn.RangeTest.randomList;
import static multicolumn.RangeTest.randomRange;

public class CrackerMapBenchmarks {
    private static final Logger LOG = LoggerFactory.getLogger(CrackerMapBenchmarks.class);

    private static final char SEPERATOR = ',';

    public static void main(String[] args) throws IOException {
        LOG.info("Started.");
        long start = System.nanoTime();

        int warmUpSize = 1000;
        benchmark(warmUpSize);

        int size = 1_000_0;
        long[][] responseTimes = benchmark(size);

        output(responseTimes, "response_times.csv");
        LOG.info("Finished, total-time {}", TimeUnit.MINUTES.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
    }

    private static long[][] benchmark(int size) {
        List<Integer> tail = Arrays.asList(new Integer[size]);
        List<Integer> head = randomList(size, size);
        CrackerMap<Integer, Integer> crackerMap = new CrackerMap<>(head, tail);

        List<Integer> sortedHead = new ArrayList<>(head);
        sortedHead.sort(Integer::compareTo);
        CrackerMap<Integer, Integer> sortedCrackerMap = new CrackerMap<>(sortedHead, tail, true);

        int querySequenceSize = size;
        int[][] queryRanges = buildRanges(querySequenceSize);
        long[] queryResponseTimes = new long[querySequenceSize];
        long[] sortedQueryResponseTimes = new long[querySequenceSize];

        query(crackerMap, queryRanges, queryResponseTimes);
        query(sortedCrackerMap, queryRanges, sortedQueryResponseTimes);

        return new long[][]{queryResponseTimes, sortedQueryResponseTimes};
    }

    private static void query(CrackerMap<Integer, Integer> crackerMap, int[][] queryRanges, long[] queryResponseTimes) {
        for (int i = 0; i < queryRanges.length; i++) {
            recordScanTime(crackerMap, queryRanges, queryResponseTimes, i);
        }
    }

    private static List<Tuple<Integer, Integer>> recordScanTime(CrackerMap<Integer, Integer> crackerMap, int[][] queryRanges, long[] queryResponseTimes, int i) {
        long startTime = System.nanoTime();
        List<Tuple<Integer, Integer>> result = crackerMap.scan(queryRanges[i][0], queryRanges[i][1]);
        queryResponseTimes[i] = TimeUnit.NANOSECONDS.convert((System.nanoTime() - startTime), TimeUnit.NANOSECONDS);
        return result;
    }

    private static void output(long[][] responseTimes, String file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            writer.append("QuerySequence").append(SEPERATOR).append("ResponseTime").append(SEPERATOR).append("Type");
            writer.newLine();
            long[] cracked = responseTimes[0];
            long[] sorted = responseTimes[1];
            for (int j = 0, i = 1; j < cracked.length; j++, i++) {
                writer.append(String.valueOf(i));
                writer.append(SEPERATOR);
                writer.append(String.valueOf(cracked[j]));
                writer.append(SEPERATOR);
                writer.append("CRACKED");
                writer.newLine();

                writer.append(String.valueOf(i));
                writer.append(SEPERATOR);
                writer.append(String.valueOf(sorted[j]));
                writer.append(SEPERATOR);
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
