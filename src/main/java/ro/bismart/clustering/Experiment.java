package ro.bismart.clustering;

import org.knowm.xchart.VectorGraphicsEncoder;
import ro.bismart.clustering.charts.ChartPlotter;
import ro.bismart.clustering.model.Cluster;
import ro.bismart.clustering.model.TimeSeries;
import ro.bismart.clustering.startegies.AdaptiveDBSCAN;
import ro.bismart.clustering.math.DistanceMetrics;
import ro.bismart.clustering.util.IODevice;
import ro.bismart.clustering.util.UsageParser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by adrian on 13.01.2017.
 */
public class Experiment {
//    private static final String[] buildings = {"WPH","SOS","VKC","LAW","BRI","BHE","WAH","ASC","THH","HAR","MRF","MHP",
//    "BMH","ALM","CSS","CTV","SAL","BIT", "MUS", "STU","HSH","JHH","DRB","SLH","HOH","OHE", "GFS","LPB","LRC","KAP",
//            "BKS","EEB","LVL","RGL","JKP","SWC","TCC","SCB","SCC","SCX","SCE"};
//
//    private static final String[] codes = {"3","3.1", "4", "7", "8", "23", "29", "32", "35", "38", "41", "45", "144",
//            "46", "15", "16", "78", "84", "85", "89", "21", "91", "111", "135", "142", "145", "169", "220", "250",
//            "253", "254", "256", "257", "262", "263", "283", "310", "312", "313", "314", "315"};

    private static final Logger LOG = Logger.getLogger("Experiment");
    private static final String[] buildings = {"WPH","SOS","VKC","LAW","BRI","BHE","WAH","ASC","THH","HAR","MRF","MHP",
            "BMH","ALM","SAL","BIT", "MUS", "STU","JHH","DRB","SLH","HOH","OHE", "GFS","LRC","KAP","BKS","EEB","LVL",
            "JKP","SWC","TCC","SCB","SCX","SCE"};

    private static final String[] codes = {"3","3.1", "4", "7", "8", "23", "29", "32", "35", "38", "41", "45", "144",
            "46", "78", "84", "85", "89", "91", "111", "135", "142", "145", "169", "250",
            "253", "254", "256", "257", "263", "283", "310", "312", "314", "315"};

    public static final String distanceMeasure = DistanceMetrics.STS;
    private static final String YEAR = "2008";

    private static String constructFileName(Integer i) {
        return "./trace/" + buildings[i] + "_" + codes[i] + "_" + YEAR + ".csv";
    }

    public static void run(String[] args) {



        //Check if we have a distance matrix precomputed, to avoid computation
        double[][] distanceMatrix = IODevice.readPrecomputedDistanceMatrix(distanceMeasure, YEAR, buildings.length);


        if(distanceMatrix == null) {
            LOG.info("Reading content from files.");
            List<TimeSeries> timeSeries = readAllTimeSeries();
            LOG.info("Done.");

            LOG.info("Computing distance matrix");
            distanceMatrix = computeDistanceMatrix(timeSeries);
            LOG.info("Done.");

            IODevice.writeDistanceMatrix(distanceMatrix, distanceMeasure, YEAR);
        }

        AdaptiveDBSCAN dbscan = new AdaptiveDBSCAN();
        LOG.info("Creating clusters...");
        List<Cluster> clusters = dbscan.createClusters(distanceMatrix);
        LOG.info("Done.");

        int clusterCounter = 0;
        for(Cluster c : clusters) {
            StringBuilder sb = new StringBuilder();
            List<TimeSeries> series = new ArrayList<>();
            for (Integer i : c.getPoints()) {
                sb.append(buildings[i]);
                sb.append(", ");
                String fileName = constructFileName(i);
                TimeSeries t = UsageParser.parseFile(fileName, buildings[i]);
                if (t != null)
                    series.add(t);
            }
            LOG.info(String.format("Plotting Cluster [%d]\tSize [%d]\nBuilding: %s", clusterCounter,
                    c.getPoints().size(), sb.toString()));

            IODevice.writeCluster(c, buildings);

            String chartName = "C_" + clusterCounter + c.getName();
            //Change amount to maximum 1000 if plotting PDF.
            int amount = 5000;
            ChartPlotter.plotTimeSeriesGroup(chartName, series, amount,
                    VectorGraphicsEncoder.VectorGraphicsFormat.EPS);

            clusterCounter++;
        }

    }

    private static List<TimeSeries> readAllTimeSeries(){

        List<TimeSeries> result = new ArrayList<>();
        for (int ii = 0; ii < buildings.length; ii++) {
            String fileName = constructFileName(ii);
            TimeSeries first = UsageParser.parseFile(fileName, buildings[ii]);
            result.add(first);
        }

        return result;
    }

    private static double[][] computeDistanceMatrix(List<TimeSeries> timeSeriesList) {
        double[][] distanceMatrix;
        distanceMatrix = new double[timeSeriesList.size()][];
        DistanceMetrics computer = new DistanceMetrics();

        for (int ii = 0; ii < timeSeriesList.size(); ii++) {

            double[] row = new double[timeSeriesList.size()];
            distanceMatrix[ii] = row;

            TimeSeries first = timeSeriesList.get(ii);

            for (int i = 0; i < buildings.length; i++) {
                TimeSeries second = timeSeriesList.get(i);

                double e = computer.computeDistance(distanceMeasure, first, second);
                row[i] = e;
            }
        }
        return distanceMatrix;
    }


}
