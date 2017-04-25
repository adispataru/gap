package ro.bismart.clustering;

import ro.bismart.clustering.charts.ChartPlotter;
import ro.bismart.clustering.charts.Pair;
import ro.bismart.clustering.charts.ResultCounter;
import ro.bismart.clustering.charts.ResultProcessor;
import ro.bismart.clustering.math.DistanceMetrics;
import ro.bismart.clustering.util.IODevice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by adrian on 21.01.2017.
 */
public class Application {

    private static String baseMAPEOUTPUTDIR;

    public static void main(String[] args) {
//        Experiment.runARIMA(args);
        int c = Experiment.run(args);

//        plotStuff();


    }

    private static void plotStuff() {
        //Plot self prediction results
        String baseArima = "./arima/" + Experiment.distancePath + "/%s-results.csv";
        List<IODevice.ResultInfo> results = IODevice.readMAPEresults(String.format(baseArima, "individual"));
        System.out.println("Read " + results.size() + "results");

//        ChartPlotter.plotAllMAPEperStep(results, "individual");
        Map<String, List<IODevice.CResult>> individualResults = ResultProcessor.aggregateMAPEResults(results);
        Map<String, List<IODevice.CResult>> standardResults = ResultProcessor.aggregateStandardMAPEResults(results);
//        ChartPlotter.plotMAPEperStep(individualResults, "individual");

        Map<Integer, Pair<Double>> standardAvgMape = ResultCounter.computeAverageMape(standardResults);
        Map<Integer, Pair<Double>> seasonalAvgMape = ResultCounter.computeAverageMape(individualResults);
        Map<String, Map<Integer, Pair<Double>>> standardsMap = new TreeMap<>();
        standardsMap.put("Standard", standardAvgMape);
        standardsMap.put("Seasonal", seasonalAvgMape);

        //Plot clustered prediction results
        String [] dm = {DistanceMetrics.STS, DistanceMetrics.EUCLIDEAN};
        Map<String, Map<String, Map<Integer, Pair<Double>>>> finalResultsMap = new TreeMap<>();
        List<IODevice.ResultInfo> avgEE = null;
        List<IODevice.ResultInfo> centerSS = null;
        for(String m1 : dm) {
            for(String m2 : dm) {
                String base = "./graphs/" + m1 + "/" + m2 + "/";
                baseMAPEOUTPUTDIR = base;
                int clusterCounter = IODevice.identifyClusterCounter(base);
                String[] fileNames = new String[clusterCounter];
                int i = 0;
                while (i < fileNames.length - 1) {
                    fileNames[i] = base + i + ".csv";
                    i++;
                }
//                fileNames[i] = base + i + "noise.csv";

                List<List<IODevice.ResultInfo>> res = IODevice.readClusterMAPEresults(fileNames);
                if(m1.equals(DistanceMetrics.EUCLIDEAN) && m2.equals(DistanceMetrics.EUCLIDEAN))
                    avgEE = res.get(1);

                if(m1.equals(DistanceMetrics.STS) && m2.equals(DistanceMetrics.STS))
                    centerSS = res.get(0);
                Map<String, Map<Integer, Pair<Double>>> methodMap = processResults(individualResults, standardResults, res);
                finalResultsMap.put(m1 + "-" + m2, methodMap);
            }
        }
        Map<String, List<IODevice.CResult>> bestCResults = ResultProcessor.aggregateMAPEResults(centerSS);
        Map<String, List<IODevice.CResult>> avgcres = ResultProcessor.aggregateMAPEResults(avgEE);
        Map<String, List<IODevice.CResult>> diffBestAvg =  ResultProcessor.computeDifference(bestCResults, avgcres);
        Map<Integer, Map<Integer, List<String>>> improvementBestAvgHistogram = ResultCounter.countMapeImproved(diffBestAvg);

        Map<String, Map<Integer, Map<Integer, List<String>>>> plotMap2 = new HashMap<>();
        plotMap2.put("Average to Center", improvementBestAvgHistogram);
        String dir = "./graphs/";

        ChartPlotter.plotMAPEHistogramPerSeries(plotMap2, dir);



        Map<String, Map<Integer, String>> bestClusterPerStepPerMethod = new TreeMap<>();
        Map<Integer, String> bestMethodPerStep = new TreeMap<>();
        for(String key : finalResultsMap.keySet()){
            Map<String, Map<Integer, Pair<Double>>> methodMap = finalResultsMap.get(key);
            //ToDo First check which method best behaves with regard to standard and seasonal
            //Todo Then find a way to represent which clustering method is the best.
            for(String method : methodMap.keySet()){
                bestClusterPerStepPerMethod.putIfAbsent(method, new TreeMap<>());
                Map<Integer, Pair<Double>> stepMap = methodMap.get(method);
                for(Integer step : stepMap.keySet()){
                    if(bestClusterPerStepPerMethod.get(method).get(step) == null){
                        bestClusterPerStepPerMethod.get(method).put(step, key);
                    }else{
                        Pair<Double> current = stepMap.get(step);
                        String bestEncountered = bestClusterPerStepPerMethod.get(method).get(step);
                        Pair<Double> best = finalResultsMap.get(bestEncountered).get(method).get(step);

//                        if(current.getFirst() - best.getFirst() > current.getSecond() - best.getSecond()){
                        if(current.getFirst() - current.getSecond() > best.getFirst() - best.getSecond()){
                            bestClusterPerStepPerMethod.get(method).put(step, key);
                        }
                    }
                    if(bestMethodPerStep.get(step) == null){
                        bestMethodPerStep.put(step, key + ":" + method);
                    }else{
                        Pair<Double> current = stepMap.get(step);
                        String[] bestEncountered = bestMethodPerStep.get(step).split(":");
                        Pair<Double> best = finalResultsMap.get(bestEncountered[0]).get(bestEncountered[1]).get(step);

//                        if(current.getFirst() - best.getFirst() > current.getSecond() - best.getSecond()){
                        if(current.getFirst() - current.getSecond() > best.getFirst() - best.getSecond()){
                            bestMethodPerStep.put(step, key + ":" + method);
                        }
                    }
                }
            }
        }
        System.out.println(bestClusterPerStepPerMethod);
        System.out.println(bestMethodPerStep);
    }

    private static Map<String, Map<Integer, Pair<Double>>> processResults(Map<String, List<IODevice.CResult>> individualResults, Map<String, List<IODevice.CResult>> standardResults, List<List<IODevice.ResultInfo>> res) {

//        ChartPlotter.plotAllMAPEperStep(res.get(0), "clusterBest");
//        ChartPlotter.plotAllMAPEperStep(res.get(1), "clusterAvg");

//        ChartPlotter.plotAllMAPEDifferencePerStep(results, res.get(0), "diffBest");
//        ChartPlotter.plotAllMAPEDifferencePerStep(results, res.get(1), "diffAvg");
//        ChartPlotter.plotAllMAPEDifferencePerStep(res.get(0), res.get(1), "diff-BEST-AVG");

        Map<String, List<IODevice.CResult>> bestCResults = ResultProcessor.aggregateMAPEResults(res.get(0));
        Map<String, List<IODevice.CResult>> standardBestResults = ResultProcessor.aggregateStandardMAPEResults(res.get(0));
        Map<Integer, Map<Integer, AtomicInteger>> bestHistogram = ResultCounter.countMapePerStep(individualResults);


        Map<String, List<IODevice.CResult>> avgcres = ResultProcessor.aggregateMAPEResults(res.get(1));
        Map<String, List<IODevice.CResult>> standardAvgResults = ResultProcessor.aggregateStandardMAPEResults(res.get(1));
//        ChartPlotter.plotMAPEperStep(bestCResults, "clusterBest");
//        ChartPlotter.plotMAPEperStep(avgcres, "clusterAvg");

//        Plot difference between the three methods
        Map<String, List<IODevice.CResult>> diffBest =  ResultProcessor.computeDifference(individualResults, bestCResults);
        Map<String, List<IODevice.CResult>> diffSeasonal =  ResultProcessor.computeDifference(standardResults, individualResults);
        Map<String, List<IODevice.CResult>> diffStandardBest =  ResultProcessor.computeDifference(standardResults, bestCResults);
        Map<String, List<IODevice.CResult>> diffStandardAvg =  ResultProcessor.computeDifference(standardResults, avgcres);
        Map<String, List<IODevice.CResult>> diffStandardBestStandard =  ResultProcessor.computeDifference(standardResults, standardBestResults);
        Map<String, List<IODevice.CResult>> diffStandardAvgStandard =  ResultProcessor.computeDifference(standardResults, standardAvgResults);
        Map<String, List<IODevice.CResult>> diffAvg =  ResultProcessor.computeDifference(individualResults, avgcres);
        Map<String, List<IODevice.CResult>> diffBestAvg =  ResultProcessor.computeDifference(bestCResults, avgcres);
        Map<String, List<IODevice.CResult>> diffAvgBest =  ResultProcessor.computeDifference(avgcres, bestCResults);

        //TODO Implement IODevice to write data to csv
        Map<Integer, Pair<Double>> stdAMAvgMape = ResultCounter.computeAverageMape(diffStandardAvg);
        Map<Integer, Pair<Double>> stdBMAvgMape = ResultCounter.computeAverageMape(diffStandardBest);
        Map<Integer, Pair<Double>> stdAMstdAvgMape = ResultCounter.computeAverageMape(diffStandardAvgStandard);
        Map<Integer, Pair<Double>> stdBMstdAvgMape = ResultCounter.computeAverageMape(diffStandardBestStandard);
        Map<Integer, Pair<Double>> seasonalAMAvgMape = ResultCounter.computeAverageMape(diffAvg);
        Map<Integer, Pair<Double>> seasonalBMAvgMape = ResultCounter.computeAverageMape(diffBest);
        Map<Integer, Pair<Double>> BMAMAvgMape = ResultCounter.computeAverageMape(diffBestAvg);
        Map<String, Map<Integer, Pair<Double>>> csvMap = new TreeMap<>(((o1, o2) -> o2.compareTo(o1)));
//        csvMap.put("A Standard vs Standard", stdAMstdAvgMape);
//        csvMap.put("C Standard vs Standard", stdBMstdAvgMape);
//        csvMap.put("A vs Standard", stdAMAvgMape);
//        csvMap.put("C vs Standard", stdBMAvgMape);
        csvMap.put("A vs Seasonal", seasonalAMAvgMape);
        csvMap.put("C vs Seasonal", seasonalBMAvgMape);
//        csvMap.put("A vs C", BMAMAvgMape);
//        IODevice.writeAverageMape(csvMap);



        Map<Integer, Map<Integer, List<String>>> StandardHistogram = ResultCounter.countMapeImproved(standardResults);
        Map<Integer, Map<Integer, List<String>>> SeasonalHistogram = ResultCounter.countMapeImproved(individualResults);
        Map<Integer, Map<Integer, List<String>>> individualBestSteps = ResultCounter.countMapeBestSteps(individualResults);
        Map<String, Map<Integer, Map<Integer, List<String>>>> bestMap = new HashMap<>();
        bestMap.put("Seasonal", individualBestSteps);
//        ChartPlotter.plotMAPEBestStepsPerSeries(bestMap);

//        plotDifferenceMAPEHistogram(diffBest, diffSeasonal, diffStandardBest, diffStandardAvg, diffAvg, diffBestAvg, StandardHistogram, SeasonalHistogram);
        if (true)
            return csvMap;


        plotStandAloneMAPEHistogram(bestCResults, standardBestResults, avgcres, standardAvgResults);

        ChartPlotter.ploteMAPEDifference(diffBest, "actual-minus-best");
        ChartPlotter.ploteMAPEDifference(diffAvg, "actual-minus-avg");
        ChartPlotter.ploteMAPEDifference(diffBestAvg, "Best-minus-Avg");
        ChartPlotter.ploteMAPEDifference(diffAvgBest, "Avg-minus-Best");
        return csvMap;
    }

    private static void plotStandAloneMAPEHistogram(Map<String, List<IODevice.CResult>> bestCResults, Map<String, List<IODevice.CResult>> standardBestResults, Map<String, List<IODevice.CResult>> avgcres, Map<String, List<IODevice.CResult>> standardAvgResults) {
        Map<Integer, Map<Integer, List<String>>> StandardBestHistogram = ResultCounter.countMapeImproved(standardBestResults);
        Map<Integer, Map<Integer, List<String>>> StandardAvgHistogram = ResultCounter.countMapeImproved(standardAvgResults);
        Map<Integer, Map<Integer, List<String>>> SeasonalBestHistogram = ResultCounter.countMapeImproved(bestCResults);
        Map<Integer, Map<Integer, List<String>>> SeasonalAvgHistogram = ResultCounter.countMapeImproved(avgcres);
        Map<String, Map<Integer, Map<Integer, List<String>>>> plotMap = new HashMap<>();

        plotMap.put("Best Member Standard", StandardBestHistogram);
        plotMap.put("Average Members Standard", StandardAvgHistogram);
        plotMap.put("Best Member Seasonal", SeasonalBestHistogram);
        plotMap.put("Average Members Seasonal", SeasonalAvgHistogram);
        String dir = baseMAPEOUTPUTDIR + "/MAPE/";
        ChartPlotter.plotMAPEHistogramPerSeries(plotMap, dir);
    }

    private static void plotDifferenceMAPEHistogram(Map<String, List<IODevice.CResult>> diffBest, Map<String, List<IODevice.CResult>> diffSeasonal, Map<String, List<IODevice.CResult>> diffStandardBest, Map<String, List<IODevice.CResult>> diffStandardAvg, Map<String, List<IODevice.CResult>> diffAvg, Map<String, List<IODevice.CResult>> diffBestAvg, Map<Integer, Map<Integer, List<String>>> standardHistogram, Map<Integer, Map<Integer, List<String>>> seasonalHistogram) {
        Map<Integer, Map<Integer, List<String>>> improvementSeasonalHistogram = ResultCounter.countMapeImproved(diffSeasonal);
        Map<Integer, Map<Integer, List<String>>> improvementSeasonalBestHistogram = ResultCounter.countMapeImproved(diffStandardBest);
        Map<Integer, Map<Integer, List<String>>> improvementSeasonalAvgHistogram = ResultCounter.countMapeImproved(diffStandardAvg);
        Map<Integer, Map<Integer, List<String>>> improvementBestHistogram = ResultCounter.countMapeImproved(diffBest);
        Map<Integer, Map<Integer, List<String>>> improvementAvgHistogram = ResultCounter.countMapeImproved(diffAvg);
        Map<Integer, Map<Integer, List<String>>> improvementBestAvgHistogram = ResultCounter.countMapeImproved(diffBestAvg);

        Map<String, Map<Integer, Map<Integer, List<String>>>> plotMap2 = new HashMap<>();
        plotMap2.put("Standard", standardHistogram);
        plotMap2.put("Seasonal", seasonalHistogram);

        plotMap2.put("Seasonal to Standard Improvement", improvementSeasonalHistogram);
        plotMap2.put("Center to Standard Improvement", improvementSeasonalBestHistogram);
        plotMap2.put("Avg to Standard Improvement", improvementSeasonalAvgHistogram);
        plotMap2.put("Center to Seasonal Improvement", improvementBestHistogram);
        plotMap2.put("Avg to Seasonal Improvement", improvementAvgHistogram);
        plotMap2.put("Avg to Best Improvement", improvementBestAvgHistogram);
        String dir = baseMAPEOUTPUTDIR + "/MAPE/";

        ChartPlotter.plotMAPEHistogramPerSeries(plotMap2, dir);
    }
}
