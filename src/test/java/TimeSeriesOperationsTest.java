import junit.framework.TestCase;
import ro.bismart.clustering.model.TimeSeries;
import ro.bismart.clustering.math.DistanceMetrics;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by adrian on 16.01.2017.
 */
public class TimeSeriesOperationsTest extends TestCase{

    public void testSDST(){
        Double[] data = {1., 2., 3., 4., 5.};

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
        assertEquals(0.0, distance);


    }

}
