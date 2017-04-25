package ro.bismart.clustering.startegies;

import ro.bismart.clustering.model.Cluster;
import ro.bismart.clustering.model.MatrixProperties;
import ro.bismart.clustering.util.IODevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Created by adrian on 20.01.2017.
 */
public class AdaptiveDBSCAN implements ClusteringStrategy{
    private static final Logger LOG = Logger.getLogger("AdaptiveDBSCAN");
    private static final int MAX_CLUSTER_SIZE = 5;
    private double[][] fitMatrix = null;

    public double[][] getFitMatrix() {
        return fitMatrix;
    }

    public void setFitMatrix(double[][] fitMatrix) {
        this.fitMatrix = fitMatrix;
    }

    @Override
    public List<Cluster> createClusters(double[][] distanceMatrix) {

        List<Cluster> result = new ArrayList<>();

        MatrixProperties matrixProperties = new MatrixProperties();
        matrixProperties.compute(distanceMatrix);

        double avg = matrixProperties.getAvg();
        double min = matrixProperties.getMin();

        //        double eps = (2*avg + max) / 3;
        double eps = (avg + 2 * min) / 3;
        //        double eps = avg;
//        eps *= 0.;
        DBSCANClusteringStrategy dbScan = new DBSCANClusteringStrategy(eps, 2);
        List<Cluster> clusters = dbScan.createClusters(distanceMatrix);

        clusters.sort((e1, e2) -> -1 * Integer.compare(e1.getPoints().size(), e2.getPoints().size()));
        Cluster noiseCluster = new Cluster();

        for (int i = 0; i < clusters.size(); i++) {
            Cluster c = clusters.get(i);
            IODevice.writeClusterInsideDistance(c, Math.toIntExact(c.getId()) + "-initial", distanceMatrix);
            if (clusters.get(i).getName().equals("noise")) {
                noiseCluster = clusters.remove(i);
                --i;
            }
        }
        if (!noiseCluster.getName().equals("noise")) {
            noiseCluster.setName("noise");
        }

        clusters.add(noiseCluster);
        boolean stop = false;
        int oldSize = 0;
        while (!clusters.isEmpty()) {
            Cluster c = clusters.remove(0);
            if (c.getName().equals("noise")) {
                if (!clusters.isEmpty()) {
                    clusters.add(c);
                    continue;
                }
            }

            if (!fit(c, distanceMatrix) && !stop) {
                LOG.info(String.format("Breaking Cluster [%d-%s]\tSize [%d]:", c.getId(), c.getName(), c.getPoints().size()));
                if(oldSize != c.getPoints().size()) {
                    oldSize = c.getPoints().size();
                }else{
                        stop = true;
                }
                List<Cluster> newClusters = breakCluster(c, distanceMatrix);
                for (int i = 0; i < newClusters.size(); i++) {

                    if (newClusters.get(i).getName().equals("noise")) {
                        Cluster removed = newClusters.remove(i);

                        if (clusters.isEmpty()) {
                            clusters.add(removed);
                            noiseCluster = removed;
                        } else {
                            for (Integer p : removed.getPoints()) {
                                if (!noiseCluster.getPoints().contains(p)) {
                                    noiseCluster.getPoints().add(p);
                                }
                            }
                        }
                        break;
                    }
                }
                clusters.addAll(newClusters);
                continue;
            }
            result.add(c);

        }
        return result;

    }

    private boolean fit(Cluster c, double[][] distanceMatrix) {
        //return c.getPoints().size() > MAX_CLUSTER_SIZE;
        double[][] fMatrix = distanceMatrix;
        if(fitMatrix != null){
            fMatrix = fitMatrix;
        }
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        double avg = 0;
        int total = 0;
        List<Integer> ps = c.getPoints();
        Set<Double> distances = new TreeSet<>();
        for(int ii = 0; ii < ps.size() - 1; ii++){
            int i = ps.get(ii);
            for(int jj = ii + 1; jj < ps.size(); jj++) {
                int j = ps.get(jj);
                if (max < fMatrix[i][j])
                    max = fMatrix[i][j];
                if (min > fMatrix[i][j])
                    min = fMatrix[i][j];
                avg += fMatrix[i][j];
                distances.add(fMatrix[i][j]);
                total++;
            }
        }

        avg /= total;
        if(distances.size() < 1){
            return true;
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
        LOG.info(String.format("Avg: %.4f\tMedian: %.4f\tMax: %.4f\tMin: %.4f\tCADM: %.4f\tCADA: %.4f\n", avg , median, max, min, pCADM, pCADA));

        return pCADM < 100 &&  pCADA < 100;

    }

    private List<Cluster> breakCluster(Cluster c, double[][] distanceMatrix) {
        double norm = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        double avg = 0;
        int total = 0;
        List<Integer> ps = c.getPoints();
        Set<Double> distances = new TreeSet<>();
        double minAVg = Double.NEGATIVE_INFINITY;
        double[][] fMatrix = distanceMatrix;
        if(fitMatrix != null){
            fMatrix = fitMatrix;
        }
        for(int ii = 0; ii < ps.size() - 1; ii++){
            int i = ps.get(ii);
            double minRow = Double.POSITIVE_INFINITY;
            for(int jj = 0; jj < ps.size(); jj++) {
                int j = ps.get(jj);
                if(i == j)
                    continue;
                if (norm < fMatrix[i][j])
                    norm = fMatrix[i][j];
                if (min > fMatrix[i][j])
                    min = fMatrix[i][j];
                if(minRow > fMatrix[i][j])
                    minRow = fMatrix[i][j];
                avg += fMatrix[i][j];
                total++;
            }
            distances.add(minRow);
            if(minAVg < minRow)
                minAVg = minRow;
        }
//        minAVg /= ps.size();

        avg /= total;
        List<Double> d = new ArrayList<>(distances);
        double median = d.get(d.size() / 2);
        LOG.info(String.format("Avg: %.4f\tMedian: %.4f\tMax: %.4f\tMin: %.4f\n", avg , median, norm, min));

//        norm = Math.abs((median - min) + (norm - median)) / norm;
        double eps = (median+2*min)/3;
        DBSCANClusteringStrategy dbScan = new DBSCANClusteringStrategy(eps, 2);
        return dbScan.createClusters(fMatrix, c.getPoints());
    }
}
