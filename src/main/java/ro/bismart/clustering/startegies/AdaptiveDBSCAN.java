package ro.bismart.clustering.startegies;

import ro.bismart.clustering.model.Cluster;
import ro.bismart.clustering.model.MatrixProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by adrian on 20.01.2017.
 */
public class AdaptiveDBSCAN implements ClusteringStrategy{
    private static final Logger LOG = Logger.getLogger("AdaptiveDBSCAN");
    private static final int MAX_CLUSTER_SIZE = 5;

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

            if (clusters.get(i).getName().equals("noise")) {
                noiseCluster = clusters.remove(i);
                break;
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
                }else{
                    stop = false;
                }
            }

            if (c.getPoints().size() > MAX_CLUSTER_SIZE && !stop) {
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
    private static List<Cluster> breakCluster(Cluster c, double[][] distanceMatrix) {
        double avg = 0;
        int total = 0;
        double min = Double.MAX_VALUE;
        for(Integer i : c.getPoints()){
            for(Integer j : c.getPoints()) {
                if(i.equals(j))
                    continue;

                if(min > distanceMatrix[i][j])
                    min = distanceMatrix[i][j];
                avg += distanceMatrix[i][j];
                total++;
            }
        }
        avg /= total;
        double eps = (avg + 2*min) / 3;
        DBSCANClusteringStrategy dbScan = new DBSCANClusteringStrategy(eps, 2);
        return dbScan.createClusters(distanceMatrix, c.getPoints());
    }
}
