package ro.bismart.clustering;

import org.knowm.xchart.VectorGraphicsEncoder;
import ro.bismart.clustering.charts.ChartPlotter;
import ro.bismart.clustering.forecast.ARIMA;
import ro.bismart.clustering.forecast.ToW;
import ro.bismart.clustering.model.Cluster;
import ro.bismart.clustering.model.TimeSeries;
import ro.bismart.clustering.startegies.AdaptiveDBSCAN;
import ro.bismart.clustering.math.DistanceMetrics;
import ro.bismart.clustering.util.IODevice;
import ro.bismart.clustering.util.UsageParser;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private static final String[][] plotStrings = {{"ALM", "SWC"}, {"MHP", "SLH"}, {"BIT", "BMH", "MRF"}};

    public static final String distanceMeasure = DistanceMetrics.STS;
    public static final String fitDistanceMeasure = DistanceMetrics.STS;
    private static final String FORECAST_METHOD = "ARIMA";
    public static String YEAR = "2008";
    private static final int [] seasons = {1, 4, 96, 672};
    private static final int[] steps = {0, 3, 15, 95};
    public static final String distancePath = distanceMeasure + "/" + fitDistanceMeasure;

    private static String constructFileName(Integer i) {
        return "./trace/" + buildings[i] + "_" + codes[i] + "_" + YEAR + ".csv";
    }


    public static void runARIMA(String[] args){
        int ii = 0;
//        String fileName = constructFileName(ii);
        List<TimeSeries> timeSeries = readAllTimeSeries();
//        YEAR = "2009";
//        List<TimeSeries> secondtimeSeries = readAllTimeSeries();
//        for(int i = 0; i < timeSeries.size(); i++){
//            TimeSeries t1 = timeSeries.get(i);
//            TimeSeries t2 = secondtimeSeries.get(i);
//            if(!t1.getName().equals(t2.getName())){
//                LOG.info("Problem in reading timeseries ");
//                continue;
//            }
//            //TODO Check for other fields and create method with this.
//            t1.addData(t2.getData());
//
//        }
        double ratio = 3.0/2;
        for (TimeSeries first : timeSeries) {
            IODevice.addBlankLinetoArimaResults("individual");
            for(int step : steps ) {
                TimeSeries best = null;
                double score = Double.MAX_VALUE;
                IODevice.addBlankLinetoArimaResults("individual");
                for (int season : seasons) {
                    //            TimeSeries first = UsageParser.parseFile(fileName, buildings[ii]);
                    //            double[] originalData = first.getData().clone();
                    TimeSeries second = arimaExperiment(first, season, step, ratio, "individual");

                    double MAPE = DistanceMetrics.MAPE(first, second);
                    if(MAPE < score){
                        score = MAPE;
                        best = second;
                    }

                }
                List<TimeSeries> toPlot = new LinkedList<>();
                toPlot.add(first);
                toPlot.add(best);
                int position = Math.toIntExact(Math.round(first.getData().length / ratio));

//                LOG.info("Plotting..");
//                ChartPlotter.plotTimeSeriesGroup( String.format("P-%s-%dsteps",first.getName(), step), toPlot, position, 2000,
//                        VectorGraphicsEncoder.VectorGraphicsFormat.EPS);
//                LOG.info("Done.");
            }
        }



    }

    private static TimeSeries arimaExperiment(TimeSeries first, int season, int forwardStep, double ratio, String type) {

        LOG.info(String.format("[%s] size: %d", first.getName(), first.getData().length));
        if(first.getData().length < 1){
            LOG.info("Passing due to length..");
            return null;
        }
        ARIMA arima = new ARIMA(first, season);
        int position = Math.toIntExact(Math.round(first.getData().length / ratio));


//        for(int i = 0; i < position; i++){
//            prediction.add(first.getData()[i]);
//        }


        int[] model = arima.getARIMAmodel(position);

        List<Double> prediction = new LinkedList<>();
        double[] originalData = first.getData().clone();
        LOG.info(String.format("Best model is [p,q]= [%d,%d]", model[0], model[1]));
        for (int i = 0; i < first.getData().length - position; i++) {

            double newValue = arima.aftDeal(arima.predictValue(model[0], model[1], position + i), position + i);
            arima.originalData.getData()[position + i] = newValue;
            arima.originalData.getData()[position + i - forwardStep] = originalData[position + i - forwardStep];
            prediction.add(newValue);
//                System.out.printf("%.4f\t|\t%.4f\n", newValue, first.getData()[position + i]);

        }

        //copy prediction content
        double[] t = new double[prediction.size()];
        for (int i = 0; i < prediction.size(); i++) {
            t[i] = prediction.get(i);
        }
        TimeSeries predTS = new TimeSeries();
        predTS.setName(String.format("P(%s,%d,%d)", first.getName(), forwardStep, season));
        predTS.setData(t);
        predTS.setStep(first.getStep());
        predTS.setTimeUnit(first.getTimeUnit());
        predTS.setStart(new Date(first.getStart().getTime() + position * predTS.getStep()));

        Double[] measures = new Double[3];
        measures[0] = DistanceMetrics.MAPE(first, predTS);
        measures[1] = DistanceMetrics.SMAPE(first, predTS);
        measures[2] = DistanceMetrics.RMSD(first, predTS);
        IODevice.writeARIMAResults(type, first.getName(), season, forwardStep, ratio, model, measures[0], measures[1], measures[2], arima.getBestARMACoefs());

        return predTS;
    }

    public static TimeSeries createPredictionFromSample(TimeSeries model, TimeSeries prediction, TimeSeries t1, int season){
        int position = Math.toIntExact((prediction.getStart().getTime() - model.getStart().getTime()) / model.getStep());
        TimeSeries result = new TimeSeries();
        result.setStart(prediction.getStart());
        result.setName("D("+t1.getName()+"," + prediction.getName() + ")");
        result.setTimeUnit(prediction.getTimeUnit());
        result.setStep(prediction.getStep());
        result.setData(prediction.getData().clone());
        double[] prData = prediction.getData();
        int offset = Math.toIntExact((t1.getStart().getTime() - model.getStart().getTime()) / model.getStep());
        boolean sts = distanceMeasure.equals(DistanceMetrics.STS);

        for(int i = 0; i < prData.length && position + i < t1.getData().length; i++){
            if(sts) {
            double slope = (model.getData()[position - season - i - 1] - model.getData()[position - season - i - 2]) -
                    (t1.getData()[position - offset - season - i - 1] - t1.getData()[position - offset - season - i - 2]);
            result.getData()[i] = prData[i] + slope;
            }else{
                result.getData()[i] = prData[i] + (model.getData()[position - i - season - 1] - t1.getData()[position - i - season - 1]);
            }
        }
        return result;
    }

    public static int run(String[] args) {

        final double ratio = 1.5;

        //Check if we have a distance matrix precomputed, to avoid computation
        double[][] distanceMatrix = IODevice.readPrecomputedDistanceMatrix(distanceMeasure, YEAR, buildings.length);
        double[][] fitMatrix = IODevice.readPrecomputedDistanceMatrix(fitDistanceMeasure, YEAR, buildings.length);

        List<TimeSeries> timeSeries = readAllTimeSeries();
        if(distanceMatrix == null || fitMatrix == null) {
            LOG.info("Reading content from files.");
//            List<TimeSeries> timeSeries = readAllTimeSeries();
            LOG.info("Done.");

            LOG.info("Computing distance matrix");
            distanceMatrix = computeDistanceMatrix(timeSeries, distanceMeasure);
            LOG.info("Done.");

            LOG.info("Computing fit matrix");
            fitMatrix = computeDistanceMatrix(timeSeries, fitDistanceMeasure);
            LOG.info("Done.");

//            IODevice.writeDistanceMatrix(fitMatrix, fitDistanceMeasure, YEAR);
//
//            IODevice.writeDistanceMatrix(distanceMatrix, distanceMeasure, YEAR);
        }


        AdaptiveDBSCAN dbscan = new AdaptiveDBSCAN();
        dbscan.setFitMatrix(fitMatrix);
        LOG.info("Creating clusters...");

//        List<Cluster> clusters = dbscan.createClusters(distanceMatrix);
        List<Cluster> clusters = manuallyCreateClusters();
        LOG.info("Done.");

        int clusterCounter = 0;

        for(Cluster c : clusters) {
            boolean predict = true;
//            IODevice.writeClusterInsideDistance(c, clusterCounter + "-final", distanceMatrix);


//            if(IODevice.isClusterPerformanceWritten(c,  clusterCounter)){
//                LOG.info(String.format("[%d] Cluster performance already present. Skipping...", clusterCounter));
//                clusterCounter++;
//                continue;
//            }


            StringBuilder sb = new StringBuilder();
            List<TimeSeries> series = new ArrayList<>();
            int bestIndex = -1;
            double minAvgDistance = Double.POSITIVE_INFINITY;
            int position = 0;
            for (Integer i : c.getPoints()) {
                sb.append(timeSeries.get(i).getName());
                sb.append(", ");

                TimeSeries t = timeSeries.get(i);
                if (t != null) {
                    series.add(t);
                    int p = Math.toIntExact(Math.round(t.getData().length / ratio));
                    if(p > position)
                        position = p;
                }



                double avgDistance = 0;
                for(Integer j : c.getPoints()){
                    avgDistance += fitMatrix[i][j];
                }
                avgDistance /= c.getPoints().size();
                double stddev = 0;
                for(Integer j : c.getPoints()){
                    stddev += Math.pow((avgDistance - fitMatrix[i][j]), 2);
                }

                stddev /= c.getPoints().size();
                stddev = Math.sqrt(stddev);

                if(stddev < minAvgDistance && t.getData().length > 0){
                    minAvgDistance = stddev;
                    bestIndex = series.size() - 1;
                }
            }

            if(bestIndex < 0 ) {
                LOG.info("Passing by, no data..");
                continue;
            }

            if(Experiment.FORECAST_METHOD.equals("ARIMA")) {
                Map<Integer, List<Map<String, TimeSeries>>> predictions = predictForClusterMembers(ratio, clusterCounter, c, series, bestIndex);
                for(Integer step : predictions.keySet()){
                    Map<String, List<TimeSeries>> toPlot = new HashMap<>();
                    for(String seriesName : predictions.get(step).get(0).keySet()){
                        String baseName = seriesName.substring(0, 7);
                        toPlot.putIfAbsent(baseName, new ArrayList<TimeSeries>());

                        toPlot.get(baseName).add(predictions.get(step).get(0).get(seriesName));
                        if(predictions.get(step).get(1).get(seriesName) != null) {
                            toPlot.get(baseName).add(predictions.get(step).get(1).get(seriesName));
                        }
                    }
                    for(String name : toPlot.keySet()){

                        for(TimeSeries t : toPlot.get(name)){
                            IODevice.writeSeries(t);
                        }
                        String chartName = "/comparison/" + clusterCounter+ name + step;
                        ChartPlotter.plotTimeSeriesGroup(chartName, toPlot.get(name), position, 672,
                                VectorGraphicsEncoder.VectorGraphicsFormat.PDF);
                    }

                }
            }else if (Experiment.FORECAST_METHOD.equals("ToW")){
                predictForClusterMembersToW(ratio, clusterCounter, c, series, bestIndex);
            }

            LOG.info(String.format("Plotting Cluster [%d]\tSize [%d]\nBuilding: %s", clusterCounter,
                    c.getPoints().size(), sb.toString()));


            String chartName = "C_" + clusterCounter + c.getName();
            //Change amount to maximum 1000 if plotting PDF.
            int amount = 5000;
//            ChartPlotter.plotTimeSeriesGroup(chartName, series, position, amount,
//                    VectorGraphicsEncoder.VectorGraphicsFormat.EPS);

            clusterCounter++;
        }
        return clusterCounter;

    }

    private static List<Cluster> manuallyCreateClusters() {
        List<Cluster> result = new ArrayList<>();
        for (String[] strings : plotStrings) {
            Cluster c = new Cluster();
            List<Integer> points = new ArrayList<>();
            List<String> buildingsList = Arrays.asList(buildings);
            for (String s : strings) {
                points.add(buildingsList.indexOf(s));
            }
            c.setPoints(points);
            result.add(c);
        }

        return result;
    }

    private static Map<Integer, List<Map<String, TimeSeries>>> predictForClusterMembers(double ratio, int clusterCounter, Cluster c, List<TimeSeries> series, int bestIndex) {
        TimeSeries best = series.get(bestIndex);
        TimeSeries average = computeAverage(series);
        Map<Integer, List<Map<String, TimeSeries>>> result = new HashMap<>();

        for(int step : steps) {
            result.putIfAbsent(step, new ArrayList<>());

            Map<String, Double> minBestPredScore = new HashMap<>();
            Map<String, Double> minBestAvgPredScore = new HashMap<>();

            Map<String, TimeSeries> bestPrediction = new HashMap<>();
            Map<String, TimeSeries> bestAvgPrediction = new HashMap<>();
            for(TimeSeries t : series){
                minBestPredScore.put(t.getName(), Double.MAX_VALUE);
                minBestAvgPredScore.put(t.getName(), Double.MAX_VALUE);
            }
            for(int season : seasons) {
                final TimeSeries basePrediction = arimaExperiment(best, season, step, ratio, "best");
                final TimeSeries avgPrediction = arimaExperiment(average, season, step, ratio, "average");
//
//                LOG.info(String.format("Best index: %d:%s out of %d", bestIndex, best.getName(), series.size()));
//                LOG.info(String.format("Average %s of length %d", avgPrediction.getName(), avgPrediction.getData().length));

                Map<String, Double[]> performance = new TreeMap<>();
                Map<String, Double[]> avgperformance = new TreeMap<>();
                for (TimeSeries t : series) {
                    TimeSeries pred;
                    TimeSeries avgpred = createPredictionFromSample(average, avgPrediction, t, season);
                    if (!t.getName().equals(best.getName())) {
                        pred = createPredictionFromSample(best, basePrediction, t, season);
                    } else {
                        pred = basePrediction;
                    }

                    Double[] measures = new Double[3];
                    measures[0] = DistanceMetrics.MAPE(t, pred);
                    if(measures[0] < minBestPredScore.get(t.getName())){
                        minBestPredScore.put(t.getName(),measures[0]);
                        bestPrediction.put(t.getName(), pred);
                    }
                    measures[1] = DistanceMetrics.SMAPE(t, pred);
                    measures[2] = DistanceMetrics.RMSD(t, pred);
                    Double[] avgMeasures = new Double[3];
                    avgMeasures[0] = DistanceMetrics.MAPE(t, avgpred);
                    if(avgMeasures[0] < minBestAvgPredScore.get(t.getName())){
                        minBestAvgPredScore.put(t.getName(), avgMeasures[0]);
                        bestAvgPrediction.put(t.getName(), avgpred);
                    }
                    avgMeasures[1] = DistanceMetrics.SMAPE(t, avgpred);
                    avgMeasures[2] = DistanceMetrics.RMSD(t, avgpred);
                    avgperformance.put(t.getName(), avgMeasures);
                    performance.put(t.getName(), measures);

                    if(season == 1){
                        TimeSeries pred2 = pred.clone();
                        pred2.setName(pred.getName() + "-standard");
                        bestPrediction.put(t.getName() + "standard", pred2);

                        TimeSeries avgpred2 = avgpred.clone();
                        avgpred2.setName(avgpred.getName() + "-standard");
                        bestAvgPrediction.put(t.getName() + "standard", avgpred2);

                        TimeSeries t2 = t. clone();
                        t2.setName(t.getName() + "-actual");
                        bestPrediction.put(t.getName() + "-actual", t2);
                    }
                }
//                IODevice.writeClusterPerformance(c,clusterCounter, best.getName(), performance, avgperformance, step, season);

            }

            result.get(step).add(bestPrediction);
            result.get(step).add(bestAvgPrediction);


        }

        return result;

//        series.add(average);
//        series.add(bestPrediction);
    }

    private static void predictForClusterMembersToW(double ratio, int clusterCounter, Cluster c, List<TimeSeries> series, int bestIndex) {
        TimeSeries best = series.get(bestIndex);
        TimeSeries average = computeAverage(series);

        TimeSeries bestPrediction = null;
        TimeSeries bestAvgPrediction = null;
        double minBestPredScore = Double.MAX_VALUE;
        double minBestAvgPredScore = Double.MAX_VALUE;

        for(int step : steps) {

                final TimeSeries basePrediction = ToW.createPrediction(best, step, ratio, "best");
                final TimeSeries avgPrediction = ToW.createPrediction(average, step, ratio, "average");

                LOG.info(String.format("Best index: %d:%s out of %d", bestIndex, best.getName(), series.size()));
                LOG.info(String.format("Average %s of length %d", avgPrediction.getName(), avgPrediction.getData().length));

                Map<String, Double[]> performance = new TreeMap<>();
                Map<String, Double[]> avgperformance = new TreeMap<>();
                for (TimeSeries t : series) {
                    TimeSeries pred;
                    TimeSeries avgpred = createPredictionFromSample(average, avgPrediction, t, 0);
                    if (!t.getName().equals(best.getName())) {
                        pred = createPredictionFromSample(best, basePrediction, t, 0);
                    } else {
                        pred = basePrediction;
                    }

                    Double[] measures = new Double[3];
                    measures[0] = DistanceMetrics.MAPE(t, pred);
                    if(measures[0] < minBestPredScore){
                        minBestPredScore = measures[0];
                        bestPrediction = pred;
                    }
                    measures[1] = DistanceMetrics.SMAPE(t, pred);
                    measures[2] = DistanceMetrics.RMSD(t, pred);
                    Double[] avgMeasures = new Double[3];
                    avgMeasures[0] = DistanceMetrics.MAPE(t, avgpred);
                    if(avgMeasures[0] < minBestAvgPredScore){
                        minBestAvgPredScore = avgMeasures[0];
                        bestAvgPrediction = avgpred;
                    }
                    avgMeasures[1] = DistanceMetrics.SMAPE(t, avgpred);
                    avgMeasures[2] = DistanceMetrics.RMSD(t, avgpred);
                    avgperformance.put(t.getName(), avgMeasures);
                    performance.put(t.getName(), measures);
                }
                IODevice.writeClusterPerformance(c,clusterCounter, best.getName(), performance, avgperformance, step, 0);

        }

        series.add(average);
        series.add(bestPrediction);
    }

    public static void runDistanceToBlank(String args[]){
        DistanceMetrics computer = new DistanceMetrics();
        List<TimeSeries> timeSeries = readAllTimeSeries();
        double[] row = new double[timeSeries.size()];

        for (int ii = 0; ii < timeSeries.size(); ii++) {
            TimeSeries first = timeSeries.get(ii);
            TimeSeries blank = TimeSeries.createBlankTimeSeries(first.getStart(), first.getStep(),
                    first.getTimeUnit(), first.getName()+"blank", first.getData().length);

            double e = computer.computeDistance(distanceMeasure, first, blank);
            row[ii] = e;
            System.out.printf("%s: %.4f", first.getName(), e);

        }

    }

    /**
     * TODO: Create method to compute weighted average based on distance in cluster. sum of distances and divide each distance by that number. subtract that from 1 and that is the weight
     *
     * @param series
     * @return
     */
    private static TimeSeries computeAverage(List<TimeSeries> series) {
        long minStart = Long.MAX_VALUE;
        long maxEnd = Long.MIN_VALUE;
        long step = 0;
        TimeUnit tu = null;
        StringBuilder sb = new StringBuilder("avg-");
        for(TimeSeries t : series){
            if(t.getStart().getTime() < minStart){
                minStart = t.getStart().getTime();
            }
            long end = t.getStart().getTime() + t.getData().length * t.getStep();
            if(end > maxEnd){
                maxEnd = end;
            }
            if(step != t.getStep()){
                step = t.getStep();
            }
            if(!t.getTimeUnit().equals(tu)){
                tu = t.getTimeUnit();
            }
            sb.append(t.getName());
            sb.append("-");
        }
        int range = Math.toIntExact((maxEnd - minStart) / step );
        double[] data = new double[range];
        int[] denominator = new int[range];
        for(TimeSeries t : series){
            int offset = Math.toIntExact((t.getStart().getTime() - minStart) / step );
            for(int i = 0; i < t.getData().length; i++){
                data[i+offset] += t.getData()[i];
                denominator[i+offset] += 1;
            }
        }
        for(int i = 0; i < range; i++){
            data[i] /= denominator[i];
        }
        TimeSeries result = new TimeSeries();
        result.setStart(new Date(minStart));
        result.setStep(step);
        result.setTimeUnit(tu);
        result.setName(sb.toString());
        result.setData(data);
        return result;
    }

    private static List<TimeSeries> readAllTimeSeries(){

        List<TimeSeries> result = new ArrayList<>();
        for (int ii = 0; ii < buildings.length; ii++) {
            String fileName = constructFileName(ii);
            TimeSeries first = UsageParser.parseFile(fileName, buildings[ii]);
            if(first != null && first.getStart() != null) {
                result.add(first);
            }else {
                LOG.info(String.format("Timeseries %s has no data ", buildings[ii]));
            }
        }

        return result;
    }

    private static double[][] computeDistanceMatrix(List<TimeSeries> timeSeriesList, String dMeasure) {
        double[][] distanceMatrix;
        distanceMatrix = new double[timeSeriesList.size()][];
        DistanceMetrics computer = new DistanceMetrics();

        for (int ii = 0; ii < timeSeriesList.size(); ii++) {

            double[] row = new double[timeSeriesList.size()];
            distanceMatrix[ii] = row;

            TimeSeries first = timeSeriesList.get(ii);

            for (int i = 0; i < timeSeriesList.size(); i++) {
                TimeSeries second = timeSeriesList.get(i);

                double e = computer.computeDistance(dMeasure, first, second);
                row[i] = e;
            }
        }
        return distanceMatrix;
    }


}
