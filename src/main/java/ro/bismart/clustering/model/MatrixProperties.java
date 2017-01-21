package ro.bismart.clustering.model;

/**
 * Created by adrian on 20.01.2017.
 */
public class MatrixProperties {
    private double avg;
    private double min;

    public double getAvg() {
        return avg;
    }

    public double getMin() {
        return min;
    }

    public MatrixProperties compute(double[][] distanceMatrix) {
        avg = 0;
        double max = Double.NEGATIVE_INFINITY;
        min = Double.POSITIVE_INFINITY;
        int total = 0;
        for(int i = 0; i < distanceMatrix.length; i++){
            for (int j = i; j < distanceMatrix.length; j++){
                //step over main diagonal (self-distance)
                if(i == j)
                    continue;
                if(max < distanceMatrix[i][j])
                    max = distanceMatrix[i][j];
                if(min > distanceMatrix[i][j])
                    min = distanceMatrix[i][j];
                avg += distanceMatrix[i][j];
                total++;

            }
        }

        avg /= total;
        return this;
    }
}
