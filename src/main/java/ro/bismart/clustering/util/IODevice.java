package ro.bismart.clustering.util;

import ro.bismart.clustering.Experiment;
import ro.bismart.clustering.model.Cluster;

import java.io.*;

/**
 * Created by adrian on 15.01.2017.
 */
public class IODevice {

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
        File dir = new File("./graphs/" + Experiment.distanceMeasure);
        if(!dir.exists())
            dir.mkdir();
        File f = new File("./graphs/" + Experiment.distanceMeasure + "/layout.dat");
        int time = 0;
        boolean exists = f.exists();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(f, true);
            if(!exists){
                fileWriter.write("%time,from,to,clusterSize\n");
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
}
