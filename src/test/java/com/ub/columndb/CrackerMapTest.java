package com.ub.columndb;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import repeat.Repeat;
import repeat.RepeatRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static bench.RangeUtils.data;
import static bench.RangeUtils.randomRange;
import static org.assertj.core.api.Assertions.assertThat;

public class CrackerMapTest {
    private static final int N = 1000;

    private CrackerMap<Integer, Integer> crackerMap;

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Before
    public void setUp() throws Exception {
        List<Integer> h = data(N);
        List<Integer> t = new ArrayList<>(Collections.nCopies(N, 0));

        crackerMap = new CrackerMap<>(h, t, new CrackerTape<>());
    }

    @Test
    @Repeat(100)
    public void scan() throws Exception {
        for (int k = 0; k < N; k++) {
            int[] range = randomRange(N);
            int low = range[0], high = range[1];

            List<Tuple<Integer, Integer>> scan = crackerMap.scan(low, high);
//            boolean allMatch = crackerMap.index.entrySet().stream().allMatch(e -> crackerMap.map.subList(0, e.getValue()).stream().allMatch(t -> t.head.compareTo(e.getKey()) < 0));
//            crackerMap.index.entrySet().forEach(e -> {
//                assertThat(crackerMap.map.subList(0, e.getValue()))
//                        .allMatch(t -> t.head.compareTo(e.getKey()) < 0);
//            });

            assertThat(scan)
                    .extracting(t -> t.head)
                    .as("%d <= h <= %d", low, high)
                    .allMatch(h -> h >= low && h <= high);
        }
    }


}