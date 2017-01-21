package ro.bismart.clustering.charts;

import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.MatlabTheme;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import ro.bismart.clustering.Experiment;
import ro.bismart.clustering.model.TimeSeries;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Created by adrian on 15.01.2017.
 */
public class ChartPlotter {

    private static final Logger LOG = Logger.getLogger(ChartPlotter.class.toString());
    private static final Color[] colors = {Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GREEN, Color.MAGENTA, Color.RED, Color.PINK};
    public static final String outputDir = "./graphs/" + Experiment.distanceMeasure + "/";

    public static void plotTimeSeriesGroup(String outFile, List<TimeSeries> timeSeries, int amount, VectorGraphicsEncoder.VectorGraphicsFormat format){
        Random random = new Random(System.currentTimeMillis());

        XYChart chart = new XYChartBuilder().width(1200).height(400).build();
        chart.getStyler().setTheme(new OwnTheme());

//        chart.setTitle("Sample Chart");
        chart.setYAxisTitle("Total (kWh)");
        chart.setXAxisTitle("Time (h)");

        AtomicLong min  = new AtomicLong(Long.MAX_VALUE);
        timeSeries.forEach(t -> {
            if(t.getStart().getTime() < min.get())
                min.set(t.getStart().getTime());
        });

        int k = 0;
        for(TimeSeries aTimeSeries : timeSeries){
            int offset = Math.toIntExact((aTimeSeries.getStart().getTime() - min.get()) / aTimeSeries.getStep());

            List<Double> time = new ArrayList<>();
            List<Double> chartData = new ArrayList<>();
            Double[] data = aTimeSeries.getData();
            for(int i = offset; i < data.length && i - offset < amount; i++){
                time.add(i/4.0);
                chartData.add(data[i]);
            }

            XYSeries series = chart.addSeries(aTimeSeries.getName(), time, chartData);
            series.setMarker(SeriesMarkers.NONE);
            series.setLineColor(colors[k % colors.length]);
            k++;

        }

        chart.getStyler().setYAxisMax(180.0);

        try {
            VectorGraphicsEncoder.saveVectorGraphic(chart, outputDir+outFile, format);
        } catch (IOException e) {
            LOG.severe("Cannot save pdf.");
        }
    }



    private static class OwnTheme extends MatlabTheme{

        @Override
        public Styler.LegendPosition getLegendPosition() {
            return Styler.LegendPosition.InsideNE;
        }

    }
}
