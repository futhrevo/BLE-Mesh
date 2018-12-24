package in.hedera.reku.swimclock.utils;

import java.io.Serializable;

/**
 * Created by rakeshkalyankar on 14/12/18.
 * Contact k.rakeshlal@gmail.com for licence
 */
public class LapSet implements Serializable {
    private static final long serialVersionUID = -5963670920408756945L;
    private Integer minute;
    private Integer second;
    private Integer milliseconds;
    private Integer lapcount;

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public Integer getSecond() {
        return second;
    }

    public void setSecond(Integer second) {
        this.second = second;
    }

    public Integer getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Integer milliseconds) {
        this.milliseconds = milliseconds;
    }

    public String getLapcount() {
        return String.format("%1$02X", lapcount);
    }

    public int getlapcountInt() {
        return lapcount;
    }
    public void setLapcount(Integer lapcount) {
        this.lapcount = lapcount;
    }

    public LapSet(Integer minute, Integer second, Integer milliseconds, Integer lapcount) {
        this.minute = minute;
        this.second = second;
        this.milliseconds = milliseconds;
        this.lapcount = lapcount;
    }

    public LapSet() {
        this.minute = 0;
        this.second = 1;
        this.milliseconds = 0;
        this.lapcount = 1;
    }

    public String getMillisHex() {
        long millis = getTotalMills();
        return String.format("%1$06X",millis);
    }

    public long getTotalMills() {
        return (milliseconds * 10) + (1000 * second) + (1000 * 60 * minute);
    }
}
