import junit.framework.TestCase;
import ro.bismart.clustering.model.TimeSeries;
import ro.bismart.clustering.math.DistanceMetrics;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by adrian on 16.01.2017.
 */
public class TimeSeriesOperationsTest extends TestCase{
    public static final double epsilon = 0.00001;

    public void testDistanceMeasuresOnSameTimeSeries(){
        double[] data = {1., 2., 3., 4., 5.};

        TimeSeries a = new TimeSeries();
        TimeSeries b = new TimeSeries();

        a.setStart(new Date(0));
        a.setTimeUnit(TimeUnit.MILLISECONDS);
        a.setStep(15*60*1000);
        a.setData(data);

        b.setStart(new Date(0));
        b.setTimeUnit(TimeUnit.MILLISECONDS);
        b.setStep(15*60*1000);
        b.setData(data);

        DistanceMetrics computer = new DistanceMetrics();
        double distance = computer.STSD(a, b);
        assertEquals("STSD", 0.0, distance);
        distance = computer.computeDistance(DistanceMetrics.EUCLIDEAN, a, b);
        assertEquals(DistanceMetrics.EUCLIDEAN, 0.0, distance);
        distance = computer.computeDistance(DistanceMetrics.MAPE, a, b);
        assertEquals(DistanceMetrics.MAPE, 0.0, distance);
        distance = DistanceMetrics.SMAPE(a, b);
        assertEquals("SMAPE", 0.0, distance);
        distance = computer.computeDistance(DistanceMetrics.RMSD, a, b);
        assertEquals(DistanceMetrics.RMSD, 0.0, distance);
        distance = computer.pearsonCorrelation(a, b);
        assertTrue("Pearson Correlation", 1.0 - distance < epsilon );

    }

    public void testDistanceMeasures(){
        double[] data = {1., 2., 3., 4., 5.};
        double[] data2 = {2., 3., 4., 5.};

        TimeSeries a = new TimeSeries();
        TimeSeries b = new TimeSeries();

        a.setStart(new Date(0));
        a.setTimeUnit(TimeUnit.MILLISECONDS);
        a.setStep(15*60*1000);
        a.setData(data);

        b.setStart(new Date(a.getStep()));
        b.setTimeUnit(TimeUnit.MILLISECONDS);
        b.setStep(15*60*1000);
        b.setData(data2);

        DistanceMetrics computer = new DistanceMetrics();
        double distance = computer.STSD(a, b);
        assertEquals("STSD", 0.0, distance);
        distance = computer.computeDistance(DistanceMetrics.EUCLIDEAN, a, b);
        assertEquals(DistanceMetrics.EUCLIDEAN, 0.0, distance);
        distance = computer.computeDistance(DistanceMetrics.MAPE, a, b);
        assertEquals(DistanceMetrics.MAPE, 0.0, distance);
        distance = DistanceMetrics.SMAPE(a, b);
        assertEquals("SMAPE", 0.0, distance);
        distance = computer.computeDistance(DistanceMetrics.RMSD, a, b);
        assertEquals(DistanceMetrics.RMSD, 0.0, distance);
        distance = computer.pearsonCorrelation(a, b);
        assertTrue("Pearson Correlation", 1.0 - distance < epsilon );

    }

}
