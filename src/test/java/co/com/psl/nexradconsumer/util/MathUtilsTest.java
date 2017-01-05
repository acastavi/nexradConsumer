package co.com.psl.nexradconsumer.util;

import co.com.psl.nexradconsumer.dtos.IndexForTimeSeries;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by angelica on 30/12/16.
 */
public class MathUtilsTest {

    TestUtils testUtils = new TestUtils();

    List<Long> getListWithLongs(int size) {
        List<Long> result = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1991 + testUtils.random.nextInt(25));
        calendar.set(Calendar.MONTH, testUtils.random.nextInt(12));
        calendar.set(Calendar.DAY_OF_MONTH, testUtils.random.nextInt(30));

        for (int i = 0; i < size; i++) {
            long timeInMillis = calendar.getTimeInMillis();
            result.add(timeInMillis);
            calendar.setTimeInMillis(timeInMillis + testUtils.random.nextInt(60000));
        }

        return result;
    }

    @Test
    public void whenMilliSecondsShouldReturnDifferences() {
        MathUtils mathUtils = new MathUtils();
        List<Long> millis = getListWithLongs(100);

        List<Long> result = mathUtils.getDifferences(millis);

        Assert.assertEquals(millis.size() - 1, result.size());
        for (int i = 0; i < result.size(); i++)
            Assert.assertEquals(Long.valueOf(millis.get(i + 1) - millis.get(i)), result.get(i));
    }

    @Test
    public void whenDifferencesShouldReturnMin() {
        MathUtils mathUtils = new MathUtils();

        long min = 100;
        List<Long> values = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            long next = testUtils.random.nextLong();
            values.add(next);
            min = Long.min(min, next);
        }

        long result = mathUtils.getMin(values);

        Assert.assertEquals(min, result);
    }

    @Test
    public void whenDifferencesShouldReturnMax() {
        MathUtils mathUtils = new MathUtils();

        long max = 0;
        List<Long> values = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            long next = testUtils.random.nextLong();
            values.add(next);
            max = Long.max(max, next);
        }

        long result = mathUtils.getMax(values);

        Assert.assertEquals(max, result);
    }

    @Test
    public void whenListShouldReturnMatchCount1() {
        MathUtils mathUtils = new MathUtils();

        int match = 1;
        long start = testUtils.random.nextInt(10) + 1;
        int interval = testUtils.random.nextInt(10) + 5;
        List<Long> values = new ArrayList<>();
        values.add(start);
        for (int i = 1; i < 100; i++) {
            int next = testUtils.random.nextInt(10);
            long uniform = i * interval + start;
            long ununiform = values.get(i - 1) + next;
            if (next % 2 == 0)
                values.add(uniform);
            else
                values.add(ununiform);
            if (next % 2 == 0 || Long.compare(uniform, ununiform) == 0)
                match++;
        }

        int result = mathUtils.getMatch(values, interval, start);

        Assert.assertEquals(match, result);
    }

    @Test
    public void whenListShouldReturnMatchCount2() {
        MathUtils mathUtils = new MathUtils();

        int match = 1;
        int interval = testUtils.random.nextInt(10) + 5;
        List<Long> values = new ArrayList<>();
        values.add(0l);
        for (int i = 1; i < 100; i++) {
            int next = testUtils.random.nextInt(10);
            long uniform = (long) i * interval;
            long ununiform = values.get(i - 1) + next;
            if (next % 2 == 0)
                values.add(uniform);
            else
                values.add(ununiform);
            if (next % 2 == 0 || Long.compare(uniform, ununiform) == 0)
                match++;
        }

        int result = mathUtils.getMatch(values, interval, 0);

        Assert.assertEquals(match, result);
    }

    @Test
    public void whenListShouldReturnBestOption1() {
        MathUtils mathUtils = new MathUtils();

        long start = testUtils.random.nextInt(10) + 1;
        int interval = testUtils.random.nextInt(10) + 5;
        List<Long> values = new ArrayList<>();
        values.add(start);
        for (int i = 1; i < 100; i++) {
            int next = testUtils.random.nextInt(10);
            if (next % 2 == 0)
                values.add(i * interval + start);
            else
                values.add(values.get(i - 1) + next);
        }
        List<Long> diff = mathUtils.getDifferences(values);
        long min = mathUtils.getMin(diff);
        long max = mathUtils.getMax(diff);

        IndexForTimeSeries result = mathUtils.getTheBestIndex(values, min, max);

        Assert.assertEquals(start, result.getStart());
        Assert.assertEquals(values.get(values.size() - 1), Long.valueOf(result.getEnd()));
        Assert.assertEquals(interval, result.getInterval());
    }

    @Test
    public void whenListShouldReturnBestOption2() {
        MathUtils mathUtils = new MathUtils();

        int interval = testUtils.random.nextInt(10) + 5;
        List<Long> values = new ArrayList<>();
        values.add(0l);
        for (int i = 1; i < 100; i++) {
            int next = testUtils.random.nextInt(10);
            if (next % 2 == 0)
                values.add((long) i * interval);
            else
                values.add(values.get(i - 1) + next);
        }

        List<Long> diff = mathUtils.getDifferences(values);
        long min = mathUtils.getMin(diff);
        long max = mathUtils.getMax(diff);

        IndexForTimeSeries result = mathUtils.getTheBestIndex(values, min, max);

        Assert.assertEquals(0l, result.getStart());
        Assert.assertEquals(values.get(values.size() - 1), Long.valueOf(result.getEnd()));
        Assert.assertEquals(interval, result.getInterval());
        ;
    }
}
