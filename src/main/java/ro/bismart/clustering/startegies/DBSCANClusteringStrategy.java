package ro.bismart.clustering.startegies;

import ro.bismart.clustering.model.Cluster;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by adispataru on 6/3/2016.
 * This class implements {@link ClusteringStrategy} using the DBSCAN algorithm.
 */
public class DBSCANClusteringStrategy implements ClusteringStrategy {

    private static final Logger LOG = Logger.getLogger("DBSCAN");
    private final double eps;
    private final int minPts;

    /**
     * @param eps the threshold for grouping based on the similarity matrix
     * @param minPts the minimum number neighbors in a cluster
     */
    public DBSCANClusteringStrategy(double eps, int minPts){
        this.eps = eps;
        this.minPts = minPts;
    }

    /**
     * This method creates the clusters using DBSCAN algorithm
     * @param distanceMatrix - the similarity matrix used for clustering
     * @return a {@link List<  Cluster  >} with the identified clusters.
     */
    @Override
    public List<Cluster> createClusters(double[][] distanceMatrix) {

        List<Integer> points = new ArrayList<>(distanceMatrix.length);
        for(int i = 0; i < distanceMatrix.length; i++){
            points.add(i);
        }
        List<Cluster> result = dbScanMatrix(distanceMatrix, points);
        LOG.info("Created " + result.size() + " clusters");

        //cluster
        return result;
    }

    public List<Cluster> createClusters(double[][] distanceMatrix, List<Integer> points) {

        List<Cluster> result = dbScanMatrix(distanceMatrix, points);
        LOG.info("Created " + result.size() + " clusters");

        //cluster
        return result;
    }

    private List<Cluster> dbScanMatrix(double[][] distanceMatrix, List<Integer> points){
        Cluster[] clusters = new Cluster[distanceMatrix.length];
        boolean[] visited = new boolean[distanceMatrix.length];
        boolean[] noise = new boolean[distanceMatrix.length];
        Map<Integer, Long> classifier = new HashMap<>(distanceMatrix.length);
        int clusterCounter = 0;
        Random random = new Random(System.currentTimeMillis());
        List<Integer> indices = new ArrayList<>(points);
        int r;
//        int i = indices.remove(r);
        while(!indices.isEmpty()){
            r = random.nextInt(indices.size());
            int i = indices.remove(r);
//        for(int i = distanceMatrix.length - 1; i > 0; i--){
            if(visited[i])
                continue;
//            System.out.println("Clustering " + i + " out of " + distanceMatrix.length);
            visited[i] = true;
            Queue<Integer> epsNeigh = getEpsilonNeighborhoodMatrix(distanceMatrix[i], points);
            if(epsNeigh.size() < minPts){
//                LOG.info("P is noise " + i);
                noise[i] = true;
            }else{
                Cluster c = new Cluster();
                expandClusterMatrix(i, epsNeigh, c, visited, classifier, distanceMatrix, points, indices);
                clusters[clusterCounter] = c;
                clusterCounter++;
            }
        }
        List<Cluster> result = new ArrayList<>();
        for (Cluster cluster1 : clusters) {
            if (cluster1 != null) {
                result.add(cluster1);
                for(Integer j : cluster1.getPoints()){
                    noise[j] = false;
                }
            } else {
                break;
            }
        }

        //add noisy points to their own cluster;
        Cluster cluster = new Cluster();
        cluster.setName("noise");
        for(Integer j : points){
            if(noise[j]){
                cluster.getPoints().add(j);
            }
        }
        if(cluster.getPoints().size() > 0){
            result.add(cluster);
        }
        return result;
    }


    private void expandClusterMatrix(int id, Queue<Integer> epsNeigh, Cluster c,
                                     boolean[] visited, Map<Integer, Long> classifier, double[][] distanceMatrix, List<Integer> points, List<Integer> indices) {


        c.getPoints().add(id);

        classifier.put(id, c.getId());
        while (!epsNeigh.isEmpty()){
            //            Long cp = idList.get(i);
            int p = epsNeigh.remove();

            if(!visited[p]){
                visited[p] = true;
                indices.remove(indices.indexOf(p));
                Queue<Integer> newNeigh = getEpsilonNeighborhoodMatrix(distanceMatrix[p], points);
                if(newNeigh.size() > minPts){
                    epsNeigh = mergeQueues(epsNeigh, newNeigh);
                }else{
                    epsNeigh.add(p);
                }


            }else{
                if(classifier.get(p) == null){
                    c.getPoints().add(p);
                    classifier.put(p, c.getId());
                }
            }
        }
    }

    private Queue<Integer> getEpsilonNeighborhoodMatrix(double[] distanceVector, List<Integer> points) {
        Queue<Integer> result = new LinkedList<>();
        for(Integer j : points){
            if(distanceVector[j] <= eps){
                result.add(j);
            }
        }
        return result;
    }

    private Queue<Integer> mergeQueues(Queue<Integer> q1, Queue<Integer> q2){
        Set<Integer> queueSet = new HashSet<>(q1.size() + q2.size());
        queueSet.addAll(q1);
        queueSet.addAll(q2);
        return new LinkedList<>(queueSet);
    }

}
