package ro.bismart.clustering.startegies;


import ro.bismart.clustering.model.Cluster;

import java.util.List;

/**
 * Created by adispataru on 25-Jun-16.
 */
public interface ClusteringStrategy {
    /**
     * This method creates the clusters using DBSCAN algorithm
     * @param similarityMatrix - the similarity matrix used for clustering
     * @return a {@link List<  Cluster  >} with the identified clusters.
     */
    List<Cluster> createClusters(double[][] similarityMatrix);
}
