package ro.bismart.clustering.charts;

import ro.bismart.clustering.util.IODevice;

import java.util.*;

/**
 * Created by adrian on 23.02.2017.
 */
public class ResultProcessor {

    public static Map<String, List<IODevice.CResult>> aggregateMAPEResults(List<IODevice.ResultInfo> results){
        Map<String, List<IODevice.ResultInfo>> map = new HashMap<>();
        results.forEach(r -> {
            map.putIfAbsent(r.name, new ArrayList<>());
            map.get(r.name).add(r);
        });

        Map<String, List<IODevice.CResult>> processed = new HashMap<>();
        for(String key : map.keySet()){
            processed.put(key, new LinkedList<>());
            List<IODevice.ResultInfo> res = map.get(key);
//            System.out.println("Processing " + key);
            for(int i = 0; i < res.size(); i++){
                int step = res.get(i).step;
//                System.out.printf("Step = %d; i = %d\n", step, i);
                double minMape = Double.MAX_VALUE;
                int bestSeason = -1;

                double sumDistance = 0;
                int j;
                for(j = i; j < res.size() && res.get(j).step == step; j++){
                    if(minMape > res.get(j).mape){
                        minMape = res.get(j).mape;
                        bestSeason = res.get(j).season;
                    }
                    sumDistance += Math.pow(res.get(j).mape - minMape, 2);
                }
                int range = j - i;
                sumDistance = Math.sqrt(sumDistance);
                sumDistance /= range;
                i+= range;
                IODevice.CResult cres = new IODevice.CResult();
                cres.step = step;
                cres.name = key;
                cres.bestMape = minMape;
                cres.bestSeason = bestSeason;
                cres.MAPEDistance = sumDistance;
                processed.get(key).add(cres);

            }
        }

//        for(String key : processed.keySet()){
//            for(CResult c : processed.get(key)){
//                IODevice.writeARIMACondensedResults(key, c.step, c);
//            }
//        }
        return processed;
    }

    public static Map<String, List<IODevice.CResult>> aggregateStandardMAPEResults(List<IODevice.ResultInfo> results){
        Map<String, List<IODevice.ResultInfo>> map = new HashMap<>();
        results.forEach(r -> {
            map.putIfAbsent(r.name, new ArrayList<>());
            map.get(r.name).add(r);
        });

        Map<String, List<IODevice.CResult>> processed = new HashMap<>();
        for(String key : map.keySet()){
            processed.put(key, new LinkedList<>());
            List<IODevice.ResultInfo> res = map.get(key);
//            System.out.println("Processing " + key);
            for(int i = 0; i < res.size(); i++){
                int season = res.get(i).season;
                if(season != 1)
                    continue;
//                System.out.printf("Season = %d; i = %d\n", season, i);
                double minMape = !Double.isNaN(res.get(i).mape) ? res.get(i).mape : Double.MAX_VALUE;
//                double minMape = res.get(i).mape;

                IODevice.CResult cres = new IODevice.CResult();
                cres.step = res.get(i).step;
                cres.name = key;
                cres.bestMape = minMape;
                cres.bestSeason = season;
                processed.get(key).add(cres);

            }
        }

//        for(String key : processed.keySet()){
//            for(CResult c : processed.get(key)){
//                IODevice.writeARIMACondensedResults(key, c.step, c);
//            }
//        }
        return processed;
    }

    public static Map<String, List<IODevice.CResult>> computeDifference(Map<String, List<IODevice.CResult>> base, Map<String, List<IODevice.CResult>> diff) {
        Map<String, List<IODevice.CResult>> result = new HashMap<>();
        Map<String, List<IODevice.CResult>> baseMap = new HashMap<>(base);
        boolean excludeSelf = true;

        for(String s : base.keySet()){
            List<IODevice.CResult> remove = baseMap.remove(s);
            String baseName = s.substring(0, 7);
            if(excludeSelf && s.length() > 8 && baseName.equals(s.substring(8, s.length())))
                continue;
            baseMap.put(baseName, remove);
        }
        for(String name : diff.keySet()){
            String baseName = name.substring(0, 7);


            List<IODevice.CResult> baseRes = baseMap.get(baseName);
            List<IODevice.CResult> diffRes = diff.get(name);


            if(diffRes == null || baseRes == null)
                continue;
            if(baseRes.size() != diffRes.size()){
                System.err.printf("Different sizes for %s\n", baseName);
                continue;
            }
            List<IODevice.CResult> res = new ArrayList<>(baseRes.size());

            for(int i = 0; i < baseRes.size(); i++){
                IODevice.CResult r1 = baseRes.get(i);
                IODevice.CResult r2 = diffRes.get(i);
                if(r1.step != r2.step){
                    System.err.printf("Different steps for %s", baseName);
                    continue;
                }
                IODevice.CResult r = new IODevice.CResult();
                r.name = r1.name + "-minus-" + r2.name;
                r.bestMape = r1.bestMape - r2.bestMape;
                r.bestSeason = r1.bestSeason - r2.bestSeason;
                r.MAPEDistance = r1.MAPEDistance - r2.MAPEDistance;
                r.step = r1.step;
                res.add(r);
            }
            result.put(baseName, res);
        }
        return result;
    }
}
