package ro.bismart.clustering.math;

import ro.bismart.clustering.model.TimeSeries;

/**
 * Created by adrian on 14.01.2017.
 */
public class DistanceMetrics {

    public static final String EUCLIDEAN = "euclidean";
    public static final String STS = "STS";
    public static final String RMSD = "RMSD";
    public static final String MAPE = "MAPE";

    public static double euclidianDistance(TimeSeries t1, TimeSeries t2){
        double s = 0;
        int l1 = t1.getData().length;
        int l2 = t2.getData().length;
        int offset1 = 0;
        int offset2 = 0;

        if(t1.getStart().compareTo(t2.getStart()) != 0){
            long timeDiff = t2.getStart().getTime() - t1.getStart().getTime();

            //assuming they send data at the same interval
            int diff = Math.toIntExact(timeDiff / t1.getStep());
            offset1 = diff > 0 ? diff : 0;
            offset2 = diff < 0 ? -1 * diff : 0;
        }
        int range = Math.min(l1-offset1, l2-offset2);
        double[] t1Data = t1.getData();
        double[] t2Data = t2.getData();
        for(int i = 0; i < range; i++){
            s += Math.pow(t1Data[i+offset1] - t2Data[i+offset2], 2);
        }
        s = Math.sqrt(s);

        return s;
    }

    public static double RMSD(TimeSeries t1, TimeSeries t2){
        int n = Math.min(t1.getData().length, t2.getData().length);
        double e = euclidianDistance(t1, t2);
        return e/n;
    }

    public static double MAPE(TimeSeries actual, TimeSeries forecast){
        double s = 0;
        int l1 = actual.getData().length;
        int l2 = forecast.getData().length;
        int offset1 = 0;
        int offset2 = 0;

        if(actual.getStart().compareTo(forecast.getStart()) != 0){
            long timeDiff = forecast.getStart().getTime() - actual.getStart().getTime();

            //assuming they send data at the same interval
            int diff = Math.toIntExact(timeDiff / actual.getStep());
            offset1 = diff > 0 ? diff : 0;
            offset2 = diff < 0 ? -1 * diff : 0;
        }
        int range = Math.min(l1-offset1, l2-offset2);
        double[] t1Data = actual.getData();
        double[] t2Data = forecast.getData();
        int zeroes = 0;
        for(int i = 0; i < range; i++){
            if(t1Data[i+offset1] != 0) {
                double r = Math.abs((t1Data[i + offset1] - t2Data[i + offset2]) / t1Data[i + offset1]);
                s += r;
            }else{
                zeroes++;
            }
        }

        return s / (range - zeroes);
    }

    public static double SMAPE(TimeSeries actual, TimeSeries forecast){
        double s = 0;
        int l1 = actual.getData().length;
        int l2 = forecast.getData().length;
        int offset1 = 0;
        int offset2 = 0;

        if(actual.getStart().compareTo(forecast.getStart()) != 0){
            long timeDiff = forecast.getStart().getTime() - actual.getStart().getTime();

            //assuming they send data at the same interval
            int diff = Math.toIntExact(timeDiff / actual.getStep());
            offset1 = diff > 0 ? diff : 0;
            offset2 = diff < 0 ? -1 * diff : 0;
        }
        int range = Math.min(l1-offset1, l2-offset2);
        double[] t1Data = actual.getData();
        double[] t2Data = forecast.getData();
        int zeroes = 0;
        for(int i = 0; i < range; i++){
            if(t1Data[i+offset1] != 0) {
                s += Math.abs(t2Data[i + offset2] - t1Data[i + offset1]) /
                        (Math.abs(t1Data[i + offset1] + t2Data[i + offset2]) / 2);
            }else{
                zeroes++;
            }
        }

        return s / (range - zeroes);
    }


    public double pearsonCorrelation(TimeSeries t1, TimeSeries t2){
        double p = 0;
        int l1 = t1.getData().length;
        int l2 = t2.getData().length;
        int offset1 = 0;
        int offset2 = 0;

        if(t1.getStart().compareTo(t2.getStart()) != 0){
            long timeDiff = t2.getStart().getTime() - t1.getStart().getTime();

            //assuming they send data at the same interval
            int diff = Math.toIntExact(timeDiff / t1.getStep());
            offset1 = diff > 0 ? diff : 0;
            offset2 = diff < 0 ? -1 * diff : 0;
        }
        int range = Math.min(l1-offset1, l2-offset2);

        double[] t1Data = t1.getData();
        double[] t2Data = t2.getData();

        double t1Mean = mean(t1, offset1, range);
        double t2Mean = mean(t2, offset2, range);
        for(int i = 0; i < range; i++){
            p += (t1Data[i+offset1] - t1Mean) * (t2Data[i+offset2] - t2Mean);
        }

        double t1Scat = scatter(t1, offset1, range, t1Mean);
        double t2Scat = scatter(t2, offset2, range, t2Mean);
//        System.out.printf("S1 = %g\tS2 = %g\n", t1Scat, t2Scat);

        p = p / (t1Scat * t2Scat);

        return p;
    }


    /**
     * Short Time Series Distance
     * @param t1 - first timeseries
     * @param t2 - second timeseries
     * @return the short time series distance
     */
    public double STSD(TimeSeries t1, TimeSeries t2){
        double stsd = 0;
        int l1 = t1.getData().length;
        int l2 = t2.getData().length;
        int offset1 = 0;
        int offset2 = 0;

        if(t1.getStart().compareTo(t2.getStart()) != 0){
            long timeDiff = t2.getStart().getTime() - t1.getStart().getTime();

            //assuming they send data at the same interval
            int diff = Math.toIntExact(timeDiff / t1.getStep());
            offset1 = diff > 0 ? diff : 0;
            offset2 = diff < 0 ? -1 * diff : 0;
        }
        int range = Math.min(l1-offset1, l2-offset2);

        double[] t2Data = t2.getData();
        double[] t1Data = t1.getData();
        for(int i = 0; i < range - 1; i++){
            Double dt2 = t2Data[i+1+offset2] - t2Data[i+offset2];
            Double dt1 = t1Data[i+1+offset1] - t1Data[i+offset1];
            Double distance = dt2 - dt1;
            stsd += Math.pow(distance, 2);
        }

        return Math.sqrt(stsd);
    }

    private double mean(TimeSeries t, int offset, int length){
        double mu = 0;
        double[] td = t.getData();
        for(int i = offset; i < offset + length; i++){
            mu += td[i];
        }
        mu /= length;
        return mu;
    }

    
    private double scatter(TimeSeries t, int offset, int length, double mean){
        double s = 0;
//        double mean = mean(t, offset, length);

        double[] td = t.getData();
        for(int i = offset; i < offset + length; i++){
            s += Math.pow(td[i] - mean, 2);
        }
//        System.out.printf("Scatter before sqrt %g\n", s);
        s = Math.pow(s, 0.5);
        return s;
    }


    public double computeDistance(String distanceMeasure, TimeSeries first, TimeSeries second) {
        if(EUCLIDEAN.equals(distanceMeasure))
            return euclidianDistance(first, second);
        if(STS.equals(distanceMeasure))
            return STSD(first, second);
        if(RMSD.equals(distanceMeasure))
            return RMSD(first, second);
        if(MAPE.equals(distanceMeasure))
            return 100 * MAPE(first, second);
        return 0;
    }
}
