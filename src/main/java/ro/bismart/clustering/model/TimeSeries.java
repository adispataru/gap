package ro.bismart.clustering.model;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by adrian on 13.01.2017.
 */
public class TimeSeries {
    private Date start;
    private long step;
    private TimeUnit timeUnit;
    private double[] data;
    private String name;

    public static TimeSeries createBlankTimeSeries(Date start, long step, TimeUnit timeUnit, String name, int len){
        TimeSeries t = new TimeSeries();
        t.setStart(start);
        t.setStep(step);
        t.setTimeUnit(timeUnit);
        t.setName(name);
        double[] blankData = new double[len];
        t.setData(blankData);
        return t;
    }

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

    public double[] getData() {
        return data;
    }

    public void setData(double[] data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addData(double[] newData){
        double[] temp = new double[this.data.length + newData.length];
        System.arraycopy(this.data, 0, temp, 0, this.data.length);
        System.arraycopy(newData, 0, temp, this.data.length, newData.length);
        this.data = temp;
    }

    public TimeSeries clone(){
        TimeSeries ts = createBlankTimeSeries(this.getStart(), this.step, this.getTimeUnit(), this.name, this.data.length);
        System.arraycopy(this.data, 0, ts.getData(), 0, this.data.length);
        return ts;

    }
}
