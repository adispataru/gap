package ro.bismart.clustering.forecast;

import ro.bismart.clustering.model.TimeSeries;

import java.util.List;
import java.util.Random;

/**
 * Created by adrian on 07.02.2017.
 */
public class ARIMA {
    private static final String DIFFERENCING_TYPE = "seasonal";
    public TimeSeries originalData;
    private final int diff;
    private double stdErr = 0;
    private double avgData = 0;
    private List<double[]> ARMACoefs;
    private static final int[][] model=new int[][]{{2,0},{2,2},{1,2},{2,1},{3,0},{3,1},{1,3},{3,2},{2,3},{4,0},{4,1},{1,4},{4,2},{2,4},{4,3},{3,4},{4,4}};
    private double minAIC;

    public List<double[]> getBestARMACoefs() {
        return bestARMACoefs;
    }

    private List<double[]> bestARMACoefs;


    /**
     * Initializes the ARIMA model.
     * @param t - the time series
     * @param diff - seasonal difference in the time series
     */
    public ARIMA(TimeSeries t, int diff){
        this.originalData = t;
        this.diff = diff;
    }

    public int[] getARIMAmodel(int position)
    {
        double[] stdoriginalData=this.preDealDif(position);

        int paraType=0;
        double minAIC = Double.POSITIVE_INFINITY;
        double maxNAIC = Double.NEGATIVE_INFINITY;
        List<double[]> nbestARMACoefs = null;
        int bestModelindex=0;
        int bestNModelindex=0;

        int x = 0;
        for(int i=0;i<model.length;i++)
        {
            if(model[i][0]==0)
            {
                MovingAverage ma= new MovingAverage(stdoriginalData, model[i][1]);
                ARMACoefs = ma.getModel();
                paraType=1;
            }
            else if(model[i][1]==0)
            {
                AutoRegression ar = new AutoRegression(stdoriginalData, model[i][0]);
                ARMACoefs = ar.getModel();
                paraType=2;
            }
            else
            {
                ARMA arma=new ARMA(stdoriginalData, model[i][0], model[i][1]);
                ARMACoefs = arma.getModel();
                paraType=3;
            }

            x++;
            double temp = computeAIC(ARMACoefs, stdoriginalData, paraType);
            if (temp > 0 && temp<minAIC)
            {
                bestModelindex=i;
                minAIC=temp;
                bestARMACoefs = ARMACoefs;
            }else if(temp < 0 && temp > maxNAIC){
                bestNModelindex=i;
                maxNAIC=temp;
                nbestARMACoefs = ARMACoefs;
            }
        }
        System.out.printf("Minimum AIC: %.4f : %.4f out of %d compared", minAIC, maxNAIC, x);

        if(bestModelindex < 1 && nbestARMACoefs != null){
            //if no positive AIC is found, then return the highest negative.
            bestARMACoefs = nbestARMACoefs;
            return model[bestNModelindex];

        }
        this.minAIC = minAIC;

        return model[bestModelindex];
    }

    public double aftDeal(double predictValue, int position){
        //System.out.println("predictBeforeDiff="+predictValue);
        double [] data = originalData.getData();
        if(DIFFERENCING_TYPE.equals("seasonal")) {
            if(data[position - diff] != 0) {
                return (predictValue + data[position - diff]);
            }else{
                int i = diff;
                while (position - diff - i > 0){
                    if(data[position - diff - i] != 0) {
                        return (predictValue + data[position - diff - i]);
                    }
                    i+=diff;
                }
                return 0;
            }
        }else{
            return (predictValue - 2*data[position - 1] + data[position - 2]);
        }
    }

    public double[] preDealDif(int position)
    {
        if(DIFFERENCING_TYPE.equals("seasonal")) {
            double[] data = originalData.getData();
            double[] tempData = new double[position - diff];
            for (int i = 0; i < position - diff; i++) {
                tempData[i] = data[i + diff] - data[i];
            }

            return tempData;
        }else {
            double[] data = originalData.getData();
            double[] tempData = new double[position - 2];
            for (int i = 0; i < position - 2; i++) {
                tempData[i] = data[i + 2] + 2*data[i+1] - data[i];
            }

            return tempData;
        }
    }

    public double computeAIC(List<double[]> coefs, double[] data, int type)
    {
        double temp=0;
        double temp2=0;
        double sumerr=0;
        int p=0;//ar1,ar2,...,sig2
        int q=0;//sig2,ma1,ma2...
        int n=data.length;
        Random random=new Random();

        if(type==1)
        {
            double[] maPara=coefs.get(0);
            q=maPara.length;
            double[] err=new double[q];  //error(t),error(t-1),error(t-2)...
            for(int k=q-1;k<n;k++)
            {
                temp=0;

                for(int i=1;i<q;i++)
                {
                    temp+=maPara[i]*err[i];
                }

                //shift array to right
                System.arraycopy(err, 0, err, 1, q - 1);

                err[0]=random.nextGaussian()*Math.sqrt(maPara[0]);

                sumerr+=(data[k]-(temp))*(data[k]-(temp));

            }

            return (n-(q-1))*Math.log(sumerr/(n-(q-1)))+(q+1)*2;
        }
        else if(type==2)
        {
            double[] arPara=coefs.get(0);
            p=arPara.length;
            for(int k=p-1;k<n;k++)
            {
                temp=0;
                for(int i=0;i<p-1;i++)
                {
                    temp+=arPara[i]*data[k-i-1];
                }

                sumerr+=(data[k]-temp)*(data[k]-temp);
            }
            return (n-(q-1))*Math.log(sumerr/(n-(q-1)))+(p+1)*2;
            //return (n-(p-1))*Math.log(sumerr/(n-(p-1)))+(p)*Math.log(n-(p-1));
        }
        else
        {
            double[] arPara=coefs.get(0);
            double[] maPara=coefs.get(1);
            p=arPara.length;
            q=maPara.length;
            double[] err=new double[q];  //error(t),error(t-1),error(t-2)...

            for(int k=p-1;k<n;k++)
            {
                temp=0;
                temp2=0;
                for(int i=0;i<p-1;i++)
                {
                    temp+=arPara[i]*data[k-i-1];
                }

                for(int i=1;i<q;i++)
                {
                    temp2+=maPara[i]*err[i];
                }


                System.arraycopy(err, 0, err, 1, q - 1);

                err[0]=random.nextGaussian()*Math.sqrt(maPara[0]);

                sumerr+=(data[k]-(temp2+temp))*(data[k]-(temp2+temp));
            }
            return (n-(q-1))*Math.log(sumerr/(n-(q-1)))+(p+q)*2;
            //return (n-(p-1))*Math.log(sumerr/(n-(p-1)))+(p+q-1)*Math.log(n-(p-1));
        }
    }

    public double predictValue(int p, int q, int position)
    {
        double predict=0;
        double[] stdoriginalData=this.preDealDif(position);
        int n=stdoriginalData.length;
        double temp=0,temp2=0;
        double[] err=new double[q+1];

        Random random=new Random();
        if(p==0)
        {
            double[] maPara = bestARMACoefs.get(0);
            for(int k=q;k<n;k++)
            {
                temp=0;
                for(int i=1;i<=q;i++)
                {
                    temp+=maPara[i]*err[i];
                }

                System.arraycopy(err, 0, err, 1, q);
                err[0]=random.nextGaussian()*Math.sqrt(maPara[0]);
            }
            predict= temp ;
        }
        else if(q==0)
        {
            double[] arPara = bestARMACoefs.get(0);
            for(int k=p;k<n;k++)
            {
                temp=0;
                for(int i=0;i<p;i++)
                {
                    temp+= arPara[i]*stdoriginalData[k-i-1];
                }
            }
            predict= (temp);
        }
        else
        {

            double[] arPara= bestARMACoefs.get(0);
            double[] maPara= bestARMACoefs.get(1);
            err=new double[q+1];  //error(t),error(t-1),error(t-2)...
            for(int k=p;k<n;k++)
            {
                temp=0;
                temp2=0;
                for(int i=0;i<p;i++)
                {
                    temp+=arPara[i]*stdoriginalData[k-i-1];
                }

                for(int i=1;i<=q;i++)
                {
                    temp2+=maPara[i]*err[i];
                }

                System.arraycopy(err, 0, err, 1, q);

                err[0]=random.nextGaussian()*Math.sqrt(maPara[0]);
            }

            predict=temp2+temp;

        }


        return predict;
    }


}
