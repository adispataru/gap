package ro.bismart.clustering.model;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by adrian on 13.01.2017.
 */
public class TimeSeries {
    private Date start;
    private long step;
    private TimeUnit timeUnit;
    private Double[] data;
    private String name;

    public TimeSeries(){

    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public Double[] getData() {
        return data;
    }

    public void setData(Double[] data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
