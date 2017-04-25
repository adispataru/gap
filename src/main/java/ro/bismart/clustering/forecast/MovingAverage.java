package ro.bismart.clustering.forecast;

import ro.bismart.clustering.model.TimeSeries;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by adrian on 16.02.2017.
 */
public class MovingAverage implements ForecastModel {

    private double[] data;
    int q;

    public MovingAverage(double[] data, int q){
        this.data = data;
        this.q = q;
    }

    @Override
    public List<double[]> getModel() {

        double[] autocorData = ModelMetrics.computeAutoCorrelation(data, q);

        List<double[]> result = new ArrayList<>();
        double[] params = ModelMetrics.computeMApara(autocorData, q);
        result.add(params);
        return result;

    }


}
