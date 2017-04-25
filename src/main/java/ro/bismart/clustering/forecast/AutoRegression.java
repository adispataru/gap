package ro.bismart.clustering.forecast;

import ro.bismart.clustering.model.TimeSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adrian on 16.02.2017.
 */
public class AutoRegression implements ForecastModel {

    private double[] data;
    int p;

    public AutoRegression(double[] data, int p){
        this.data = data;
        this.p = p;
    }

    @Override
    public List<double[]> getModel() {
        List<double[]> result = new ArrayList<>();

        double[] arParams = ModelMetrics.computeARcorrelation(data, p, 0);
        result.add(arParams);
        return result;
    }


}
