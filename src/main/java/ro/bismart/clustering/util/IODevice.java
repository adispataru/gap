package ro.bismart.clustering.util;

import ro.bismart.clustering.Experiment;
import ro.bismart.clustering.charts.Pair;
import ro.bismart.clustering.model.Cluster;
import ro.bismart.clustering.model.TimeSeries;

import java.io.*;
import java.util.*;

/**
 * Created by adrian on 15.01.2017.
 */
public class IODevice {

    private static final double limit = 5;

    public static void writeDistanceMatrix(double[][] matrix, String name, String year){
        DataOutputStream dos = null;
        String similarityFilepath = name + year + "_dm.dat";
        try {
            dos = new DataOutputStream(new FileOutputStream(similarityFilepath));
            for (double[] aSimilarityMatrix : matrix) {
                for (int j = 0; j < matrix.length; j++) {
                    dos.writeDouble(aSimilarityMatrix[j]);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (dos != null){
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static double[][] readPrecomputedDistanceMatrix(String name, String year,int size) {
        double[][] result = new double[size][];
        DataInputStream dis = null;
        String f = name + year + "_dm.dat";
        try {
            dis = new DataInputStream(new FileInputStream(f));
            for(int i = 0; i < size; i++){
                result[i] = new double[size];
                for(int j = 0; j < size; j++){
                    result[i][j] = dis.readDouble();
                }
            }
        } catch (IOException e) {
            return null;
        }finally {
            if(dis != null)
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return result;
    }

    public static void writeCluster(Cluster c, String[] buildings) {
        File dir = new File("./graphs/" + Experiment.distancePath);
        if(!dir.exists())
            dir.mkdirs();
        File f = new File("./graphs/" + Experiment.distancePath + "/layout.dat");
        int time = 0;
        boolean exists = f.exists();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(f, true);
            if(!exists){
                fileWriter.write("%time,from,to,clusterSize\n");
            }else{
                fileWriter.write("\n\n");
            }
            int size = c.getPoints().size();
            for(int i = 0; i < c.getPoints().size() - 1; i++){
                for(int j = i +1; j<c.getPoints().size(); j++) {
                    int ii = c.getPoints().get(i);
                    int jj = c.getPoints().get(j);
                    fileWriter.write(String.format("%d,%s,%s,%d,%d\n", time, buildings[ii], buildings[jj], size, size));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileWriter != null){
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void addBlankLinetoArimaResults(String s){
        File dir = new File(String.format("./arima/%s", Experiment.distancePath));
        if(!dir.exists())
            dir.mkdirs();
        File f = new File(String.format("./arima/%s/%s-results.csv", Experiment.distancePath, s));
        int time = 0;
        boolean exists = f.exists();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(f, true);
            if(!exists){
                fileWriter.write("%name,step, season, ratio, MAPE, SMAPE, RMSE, model, coefs\n");
            }

            fileWriter.write("\n");


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileWriter != null){
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void writeARIMAResults(String fileName, String name, int season, int step, double ratio, int[] model, double mape, double smape, double rmse, List<double[]> bestARMACoefs) {
        File dir = new File(String.format("./arima/%s", Experiment.distancePath));
        if(!dir.exists())
            dir.mkdirs();
        File f = new File(String.format("./arima/%s/%s-results.csv", Experiment.distancePath, fileName));
        int time = 0;
        boolean exists = f.exists();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(f, true);
            if(!exists){
                fileWriter.write("%name,step, season, ratio, MAPE, SMAPE, RMSE, model, coefs\n");
            }

            StringBuilder sb = new StringBuilder();
            for(double[] m : bestARMACoefs){
                sb.append("<");
                for(int i = 0; i < m.length; i++){
                    sb.append(String.format("%.4f",m[i]));
                    if(i != m.length - 1){
                        sb.append(";");
                    }
                }
                sb.append(">");
            }
            fileWriter.write(String.format("%s,%d,%d,%.4f,%.4f,%.4f,%.4f,%d:%d,%s\n",name, step, season, ratio, mape,
                    smape, rmse, model[0], model[1], sb.toString()));


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileWriter != null){
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeClusterPerformance(Cluster c, int clusterCounter, String bestName, Map<String, Double[]> performance, Map<String, Double[]> avgperformance, int step, int season) {
        File dir = new File("./graphs/" + Experiment.distancePath);
        if(!dir.exists())
            dir.mkdirs();
        File f = new File("./graphs/" + Experiment.distancePath + "/" + clusterCounter + c.getName() + ".csv");
        boolean exists = f.exists();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(f, true);
            if(!exists){
                fileWriter.write("%building,step,season,-BEST-,MAPE,SMAPE,RMSE,-AVG-,MAPE,SMAPE,RMSE \n");
            }
            for(String b : performance.keySet()){
                Double[] measures = performance.get(b);
                Double[] measures2 = avgperformance.get(b);
                fileWriter.write(String.format("%s,%d,%d,%s,%.4f,%.4f,%.4f, ,%.4f,%.4f,%.4f\n", b, step, season, bestName, measures[0], measures[1],
                        measures[2], measures2[0], measures2[1], measures2[2]));
            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileWriter != null){
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeARIMACondensedResults(String name, int step, CResult c) {
        File dir = new File("./arima/");
        if(!dir.exists())
            dir.mkdirs();
        File f = new File(String.format("./arima/%s/condensed-results%d.csv", Experiment.distancePath, step));
        int time = 0;
        boolean exists = f.exists();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(f, true);
            if(!exists){
                fileWriter.write("%name, bestSeason, minMAPE, MAPEstddev,\n");
            }

            fileWriter.write(String.format("%s,%d,%.4f,%.4f\n", name, c.bestSeason, c.bestMape, c.MAPEDistance));


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileWriter != null){
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeClusterInsideDistance(Cluster c, String clusterCounter, double[][] distanceMatrix) {
        //return c.getPoints().size() > MAX_CLUSTER_SIZE;
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        double avg = 0;
        int total = 0;
        List<Integer> ps = c.getPoints();
        Set<Double> distances = new TreeSet<>();
        for(int ii = 0; ii < ps.size() - 1; ii++){
            int i = ps.get(ii);
            for(int jj = 0; jj < ps.size(); jj++) {
                int j = ps.get(jj);
                if(i == j)
                    continue;
                if (max < distanceMatrix[i][j])
                    max = distanceMatrix[i][j];
                if (min > distanceMatrix[i][j])
                    min = distanceMatrix[i][j];
                avg += distanceMatrix[i][j];
                distances.add(distanceMatrix[i][j]);
                total++;
            }
        }

        avg /= total;
        if(distances.size() < 1){
            return ;
        }
        List<Double> d = new ArrayList<>(distances);
        double median = d.get(d.size() / 2);
        double medDistance = 0;
        double avgDistance = 0;
        for(int i = 0; i < d.size(); i++){
            medDistance += Math.abs(d.get(i) - median);
            avgDistance += Math.abs(d.get(i) - avg);
        }


        double medfitness = Math.abs((max - medDistance + 1) / (medDistance - min + 1));
        double avgfitness = Math.abs((max - avgDistance + 1) / (avgDistance - min + 1));
        double pCADM = 100 * medDistance / max;
        double pCADA = 100 * avgDistance / max;
//        LOG.info(String.format("Avg: %.4f\tMedian: %.4f\tMax: %.4f\tMin: %.4f\tMed-dist: %.4f\tAVg-dist: %.4f\n", avg , median, max, min, medDistance, avgDistance));
        File dir = new File("./graphs/" + Experiment.distancePath);
        if(!dir.exists())
            dir.mkdirs();
        File f = new File("./graphs/" + Experiment.distancePath + "/cluster-dist.csv");
        boolean exists = f.exists();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(f, true);
            if(!exists){
                fileWriter.write("%cluster, size,avg distance,median distance, distance from avg, distance from median, avg fitness, median fitness\n");
            }

            fileWriter.write(String.format("%s,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f\n", clusterCounter + c.getName(), ps.size(),
                    avg, median, avgDistance, medDistance, pCADA, pCADM));



        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileWriter != null){
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public static List<ResultInfo> readMAPEresults(String fileName){
        Scanner in = null;

        try {
            in = new Scanner(new FileInputStream(fileName));
            String line;
            String [] tokens;
            List<ResultInfo> results = new LinkedList<>();
            in.nextLine();
            while (in.hasNextLine()){
                line = in.nextLine();
                tokens = line.split(",");
                if(tokens.length < 7){
                    continue;
                }
                ResultInfo info = new ResultInfo();
                info.name = tokens[0];
                info.step = Integer.parseInt(tokens[1]);
                info.season = Integer.parseInt(tokens[2]);
                info.ratio = Double.parseDouble(tokens[3]);
                info.mape = Double.parseDouble(tokens[4]);
                if(!(limit > info.mape)){
                    info.mape = limit;
                }
                info.smape = Double.parseDouble(tokens[5]);
                info.rmse = Double.parseDouble(tokens[6]);
                results.add(info);
            }
            return results;


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if(in != null){
                in.close();
            }
        }

        return null;
    }

    public static List<List<ResultInfo>> readClusterMAPEresults(String[] fileNames){

        List<List<ResultInfo>> result = new ArrayList<>();
        List<ResultInfo> results = new LinkedList<>();
        List<ResultInfo> avgResults = new LinkedList<>();
        String line;
        String[] tokens;
        for(String fileName : fileNames) {
            if (fileName == null)
                continue;
            Scanner in = null;
            try {
                in = new Scanner(new File(fileName));
                in.nextLine();
                int counter = 0;
                while (in.hasNextLine()) {
                    counter++;
                    line = in.nextLine();
                    tokens = line.split(",");
                    if (tokens.length < 11) {
                        continue;
                    }
                    ResultInfo info = new ResultInfo();
                    ResultInfo info2 = new ResultInfo();
                    info.name = tokens[0] + "-" + tokens[3];
                    info2.name = tokens[0] + "-AVG";
                    info.step = Integer.parseInt(tokens[1]);
                    info2.step = Integer.parseInt(tokens[1]);
                    info.season = Integer.parseInt(tokens[2]);
                    info2.season = Integer.parseInt(tokens[2]);
                    info.mape = Double.parseDouble(tokens[4]);
                    if(!(limit > info.mape)){
                        info.mape = limit;
                    }
                    info.smape = Double.parseDouble(tokens[5]);
                    info.rmse = Double.parseDouble(tokens[6]);
                    info2.mape = Double.parseDouble(tokens[8]);
                    if(!(limit > info2.mape)){
                        info2.mape = limit;
                    }
                    info2.smape = Double.parseDouble(tokens[9]);
                    info2.rmse = Double.parseDouble(tokens[10]);
                    results.add(info);
                    avgResults.add(info2);
                }
                System.out.printf("Read %d lines from %s\n", counter, fileName);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
        result.add(results);
        result.add(avgResults);

        return result;
    }

    public static boolean isClusterPerformanceWritten(Cluster c, int clusterCounter) {
        File f = new File("./graphs/" + Experiment.distancePath + "/" + clusterCounter + c.getName() + ".csv");
        return f.exists();
    }

    public static void writeToWResults(String type, String tName, int step, Double mape, Double smape, Double rmse) {
        File dir = new File(String.format("./tow/%s", Experiment.distancePath));
        if(!dir.exists())
            dir.mkdirs();
        File f = new File(String.format("./tow/%s/%s-results.csv", Experiment.distancePath, type));
        int time = 0;
        boolean exists = f.exists();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(f, true);
            if(!exists){
                fileWriter.write("%name,step, MAPE, SMAPE, RMSE\n");
            }


            fileWriter.write(String.format("%s,%d,%.4f,%.4f,%.4f,\n", tName, step, mape, smape, rmse));


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileWriter != null){
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeSeries(TimeSeries t) {
        File dir = new File(String.format("./prediction/%s", Experiment.distancePath));
        if(!dir.exists())
            dir.mkdirs();
        File f = new File(String.format("./prediction/%s/%s.csv", Experiment.distancePath, t.getName()));
        int time = 0;
        boolean exists = f.exists();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(f, false);
            if(!exists){
                fileWriter.write("%Date, Value\n");
            }


            Date start = t.getStart();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            double[] data = t.getData();
            for(int i = 0; i < t.getData().length; i++) {
                fileWriter.write(String.format("%s,%.4f,\n", calendar.toString(), data[i]));
                calendar.add(Calendar.MILLISECOND, Math.toIntExact(t.getStep()));
            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileWriter != null){
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeAverageMape(Map<String, Map<Integer, Pair<Double>>> csvMap) {
        File dir = new File("./graphs");
        if(!dir.exists())
            dir.mkdirs();
        File f = new File("./graphs/averageMape.csv");
        int time = 0;
        boolean exists = f.exists();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(f, true);
            if(!exists){
                fileWriter.write("%Method,Step,Average Mape (or improvement), Average deterioration\n");
            }


            fileWriter.write(String.format("%s\n", Experiment.distancePath));
            for(String s : csvMap.keySet()){
                for(Integer step : csvMap.get(s).keySet()) {
                    Pair<Double> pair = csvMap.get(s).get(step);
                    fileWriter.write(String.format("%s,%d,%.4f,%.4f,\n", s, step, pair.getFirst(), pair.getSecond()));
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileWriter != null){
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int identifyClusterCounter(String base) {
        File dir = new File(base);
        File[] files = dir.listFiles();
        if(files == null)
            return 0;
        int total = 0;
        for(File f : files){
            if(f.getName().startsWith("C_"))
                total++;
        }
        return total;
    }

    public static class ResultInfo {
        public String name;
        public int step;
        public int season;
        public double ratio;
        public double mape;
        public double smape;
        public double rmse;
    }

    public static class CResult{
        public int step;
        public int bestSeason;
        public double bestMape;
        public double MAPEDistance;
        public String name;
    }
}
