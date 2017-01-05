package co.com.psl.nexradconsumer.util;

import co.com.psl.nexradconsumer.dtos.IndexForTimeSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by angelica on 30/12/16.
 */
public class MathUtils {

    List<Long> getDifferences(List<Long> values) {
        List<Long> differences = new ArrayList<>();
        for (int i = 0; i < (values.size() - 1); i++) {
            differences.add(values.get(i + 1) - values.get(i));
        }
        return differences;
    }

    long getMin(List<Long> values) {
        long min = values.get(0);
        for (Long value : values)
            min = Long.min(min, value);
        return min;
    }

    long getMax(List<Long> values) {
        long max = values.get(0);
        for (Long value : values)
            max = Long.max(max, value);
        return max;
    }

    int getMatch(List<Long> originalValues, long interval, long start) {
        int match = 0;
        long series = start;
        for (Long originalValue : originalValues) {
            if (Long.compare(originalValue, series) == 0)
                match++;
            series = series + interval;
        }
        return match;
    }

    IndexForTimeSeries getTheBestIndex(List<Long> originalValues, long min, long max) {
        int match;
        long interval = min;

        int bestMatch = 0;
        long bestStart = 0;
        long bestInterval = 0;

        while (interval <= max) {
            long start = Long.max(originalValues.get(0) - max, 0);
            while (start <= (originalValues.get(0) + max)) {
                match = getMatch(originalValues, interval, start);
                if (match > bestMatch) {
                    bestStart = start;
                    bestInterval = interval;
                    bestMatch = match;
                }
                start++;
            }
            interval++;
        }
        IndexForTimeSeries indexForTimeSeries = new IndexForTimeSeries();
        indexForTimeSeries.setStart(bestStart);
        indexForTimeSeries.setEnd(originalValues.get(originalValues.size() - 1));
        indexForTimeSeries.setInterval(bestInterval);
        return indexForTimeSeries;
    }

    public IndexForTimeSeries getBestIndexForDates(List<Long> dates) {
        List<Long> differences = getDifferences(dates);
        long min = getMin(differences);
        long max = getMax(differences);
        return getTheBestIndex(dates, min, max);
    }
}
