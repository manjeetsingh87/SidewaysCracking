package multicolumn;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import repeat.Repeat;
import repeat.RepeatRule;

import static multicolumn.RangeTest.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CrackingTest {
    private ArrayColumn c;
    private final int N = 10000;

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Before
    public void setUp() throws Exception {
        c = new ArrayColumn(randomList(N, N));
    }

    @Test
    @Repeat(1000)
    public void crackInThree() throws Exception {
        int[] range = randomRange(N);
        int low = range[0], high = range[1];

        int[] pos = c.crackInThree(0, N - 1, low, high);

        // assert crackInThree invariant
        assertThat(c.range(0, pos[0] - 1)).as("h < %d", low).allMatch(h -> h < low);
        assertThat(c.range(pos[0], pos[1])).as("%d <= h <= %d", low, high).allMatch(h -> h >= low && h <= high);
        assertThat(c.range(pos[1] + 1, N - 1)).as("h > %d", high).allMatch(h -> h >= high);
    }

    @Test
    @Repeat(1000)
    public void crackInTwo() throws Exception {
        int med = randomInt(N);

        int pos = c.crackInTwo(0, N - 1, med);

        // assert crackInTwo invariant
        assertThat(c.range(0, pos - 1)).as("h < %d", med).allMatch(h -> h < med);
        assertThat(c.range(pos, N - 1)).as("h >= %d", med).allMatch(h -> h >= med);
    }
}