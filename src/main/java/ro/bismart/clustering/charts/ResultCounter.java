package ro.bismart.clustering.charts;

import ro.bismart.clustering.util.IODevice;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by adrian on 06.03.2017.
 */
public class ResultCounter {

    public static Map<Integer, Map<Integer, AtomicInteger>> countMapePerStep(Map<String, List<IODevice.CResult>> results){
        Map<Integer, Map<Integer, AtomicInteger>> bestSeasonCounter = new TreeMap<>();
        for(String key : results.keySet()){
            List<IODevice.CResult> cResults = results.get(key);
            for(IODevice.CResult c : cResults){
                bestSeasonCounter.putIfAbsent(c.step, new TreeMap<>());
                bestSeasonCounter.get(c.step).putIfAbsent(c.bestSeason, new AtomicInteger(0));
                bestSeasonCounter.get(c.step).get(c.bestSeason).incrementAndGet();
            }
        }
        return bestSeasonCounter;
    }

    public static Map<Integer, Map<Integer, List<String>>> countMapeImproved(Map<String, List<IODevice.CResult>> results){
        final int noQuatiles = 100;
        Map<Integer, Map<String, AtomicInteger>> improvementMap = new TreeMap<>();
        Map<Integer, Map<Integer, List<String>>> buildingimprovementMap = new TreeMap<>();
        for(String key : results.keySet()){
            List<IODevice.CResult> cResults = results.get(key);
            for(IODevice.CResult c : cResults){
                improvementMap.putIfAbsent(c.step, new TreeMap<>());
                buildingimprovementMap.putIfAbsent(c.step, new TreeMap<>());
                if(c.bestMape >= 0){
                    double step  = 0;
                    while(c.bestMape >= step && step <= 1.0)
                        step += 10.0 / noQuatiles;
                    int percentile = Math.toIntExact(Math.round(step * noQuatiles));

                    improvementMap.get(c.step).putIfAbsent("improved", new AtomicInteger(0));
                    buildingimprovementMap.get(c.step).putIfAbsent(percentile, new LinkedList<>());
                    improvementMap.get(c.step).get("improved").incrementAndGet();
                    buildingimprovementMap.get(c.step).get(percentile).add(c.name);
                }else{
                    double step  = -1 * 10.0/noQuatiles;
                    while(c.bestMape <= step && step >= -1.0)
                        step -= 10.0 / noQuatiles;
                    int percentile = -1 * Math.toIntExact(Math.round(Math.abs(step) * noQuatiles));
                    improvementMap.get(c.step).putIfAbsent("deteriorated", new AtomicInteger(0));
                    buildingimprovementMap.get(c.step).putIfAbsent(percentile, new LinkedList<>());
                    improvementMap.get(c.step).get("deteriorated").incrementAndGet();
                    buildingimprovementMap.get(c.step).get(percentile).add(c.name);
                }

            }
        }
        return buildingimprovementMap;
    }

    public static Map<Integer, Map<Integer, List<String>>> countMapeBestSteps(Map<String, List<IODevice.CResult>> results){

        Map<Integer, Map<String, AtomicInteger>> improvementMap = new TreeMap<>();
        Map<Integer, Map<Integer, List<String>>> buildingimprovementMap = new TreeMap<>();
        for(String key : results.keySet()){
            List<IODevice.CResult> cResults = results.get(key);
            for(IODevice.CResult c : cResults){
                improvementMap.putIfAbsent(c.step, new TreeMap<>());
                buildingimprovementMap.putIfAbsent(c.step, new TreeMap<>());

                buildingimprovementMap.get(c.step).putIfAbsent(c.bestSeason, new LinkedList<>());

                buildingimprovementMap.get(c.step).get(c.bestSeason).add(c.name);

            }
        }
        return buildingimprovementMap;
    }

    public static Map<Integer, Pair<Double>> computeAverageMape(Map<String, List<IODevice.CResult>> results){

        Map<Integer, Pair<Double>> result = new TreeMap<>();
        Map<Integer, Double> sumImp = new TreeMap<>();
        Map<Integer, Double> sumDet = new TreeMap<>();
        for(String key : results.keySet()){
            for(IODevice.CResult res : results.get(key)){
                if(res.bestMape >= 0) {
                    sumImp.putIfAbsent(res.step, 0.0);
                    sumImp.put(res.step, sumImp.get(res.step) + res.bestMape);
                }else{
                    sumDet.putIfAbsent(res.step, 0.0);
                    sumDet.put(res.step, sumDet.get(res.step) + Math.abs(res.bestMape));
                }
            }
        }
        Set<Integer> allKeys = new TreeSet<>(sumImp.keySet());
        allKeys.addAll(sumDet.keySet());
        for(Integer key : allKeys){
            double avgImp = sumImp.get(key) != null ? sumImp.get(key) / results.size() : 0;
            double avgDet = sumDet.get(key) != null ? sumDet.get(key) / results.size() : 0;
            result.put(key, new Pair<>(avgImp, avgDet));
        }
        return result;
    }

}
