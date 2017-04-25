package ro.bismart.clustering.forecast;

import ro.bismart.clustering.model.TimeSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adrian on 16.02.2017.
 */
public class ARMA implements ForecastModel {

    private double[] data;
    private final int p;
    private final int q;

    public ARMA(double[] data, int p, int q){
        this.data = data;
        this.p = p;
        this.q = q;
    }

    @Override
    public List<double[]> getModel() {
        double[] autoRegress = ModelMetrics.computeARcorrelation(data, p, q);
        double[] autocorData= computeAutoCorrOfMA(p, q, data, autoRegress);

        double[] maPars = ModelMetrics.computeMApara(autocorData, q);
        List<double[]> result = new ArrayList<>();
        result.add(autoRegress);
        result.add(maPars);
        return result;

    }

    public double[] computeAutoCorrOfMA(int p, int q, double[] stdoriginalData, double[] autoRegress)
    {
        int temp=0;
        double[] errArray=new double[stdoriginalData.length-p];
        int count=0;
        for(int i=p;i<stdoriginalData.length;i++)
        {
            temp=0;
            for(int j=1;j<=p;j++)
                temp+=stdoriginalData[i-j]*autoRegress[j-1];
            errArray[count++]=stdoriginalData[i]-temp;//������Ʋв�����
        }
        return ModelMetrics.computeAutoCorrelation(errArray, q);
    }


}
