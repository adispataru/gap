package ro.bismart.clustering.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by adispataru on 6/3/2016.
 */
public class Cluster {
    private static AtomicLong counter = new AtomicLong();
    private Long id;
    private List<Integer> points;
    private String name;

    public Cluster(){
        this.id = counter.incrementAndGet();
        points = new ArrayList<>();
        this.name = "";
    }

    public Long getId() {
        return id;
    }

    public List<Integer> getPoints() {
        return points;
    }

    public void setPoints(List<Integer> points) {
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
