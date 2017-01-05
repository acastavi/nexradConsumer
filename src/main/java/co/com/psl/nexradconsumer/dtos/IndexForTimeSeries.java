package co.com.psl.nexradconsumer.dtos;

/**
 * Created by acastanedav on 5/01/17.
 */
public class IndexForTimeSeries {

    private long start;
    private long end;
    private long interval;

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }
}
