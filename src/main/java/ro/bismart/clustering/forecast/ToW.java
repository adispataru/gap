package ro.bismart.clustering.forecast;

import ro.bismart.clustering.math.DistanceMetrics;
import ro.bismart.clustering.model.TimeSeries;
import ro.bismart.clustering.util.IODevice;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by adrian on 26.02.2017.
 */
public class ToW {

    private static final int STEPS_IN_WEEK = 672;

    public static double predictValue(TimeSeries t, int numWeeks, int position, int step){
        int len = t.getData().length;
        double [] data = t.getData();
        int predictPosition = position + step;
        double avg = 0;
        for(int i = 1; i <= numWeeks; i++){
            avg += data[predictPosition - i * STEPS_IN_WEEK];
        }
        avg /= numWeeks;
        return avg;
    }

    public static TimeSeries createPrediction(TimeSeries ts, int step, double ratio, String name){

        List<Double> prediction = new LinkedList<>();
//        double[] originalData = ts.getData().clone();
        int position = Math.toIntExact(Math.round(ts.getData().length / ratio));

        for (int i = 0; i < ts.getData().length - position; i++) {

            double newValue = predictValue(ts, 10, position+i, step);
            prediction.add(newValue);
//                System.out.printf("%.4f\t|\t%.4f\n", newValue, first.getData()[position + i]);

        }

        //copy prediction content
        double[] t = new double[prediction.size()];
        for (int i = 0; i < prediction.size(); i++) {
            t[i] = prediction.get(i);
        }
        TimeSeries predTS = new TimeSeries();
        predTS.setName(String.format("ToW(%s,%d)", ts.getName(), step));
        predTS.setData(t);
        predTS.setStep(ts.getStep());
        predTS.setTimeUnit(ts.getTimeUnit());
        predTS.setStart(new Date(ts.getStart().getTime() + (position + step) * predTS.getStep()));

        Double[] measures = new Double[3];
        measures[0] = DistanceMetrics.MAPE(ts, predTS);
        measures[1] = DistanceMetrics.SMAPE(ts, predTS);
        measures[2] = DistanceMetrics.RMSD(ts, predTS);
        IODevice.writeToWResults(name, ts.getName(), step, measures[0], measures[1], measures[2]);

        return predTS;
    }

}
