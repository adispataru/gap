package ro.bismart.clustering.charts;

import org.knowm.xchart.*;
import org.knowm.xchart.style.MatlabTheme;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.markers.SeriesMarkers;
import ro.bismart.clustering.Experiment;
import ro.bismart.clustering.model.TimeSeries;
import ro.bismart.clustering.util.IODevice;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Created by adrian on 15.01.2017.
 */
public class ChartPlotter {

    private static final Logger LOG = Logger.getLogger(ChartPlotter.class.toString());
//    private static final Color[] colors = {Color.BLACK, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.ORANGE};
    private static final Marker[] markers = {SeriesMarkers.CIRCLE, SeriesMarkers.DIAMOND, SeriesMarkers.SQUARE, SeriesMarkers.TRIANGLE_DOWN, SeriesMarkers.TRIANGLE_UP};
    public static final String outputDir = "./graphs/" + Experiment.distancePath + "/";
    public static final String mapeOutputDir = "./graphs/" + Experiment.distancePath + "/MAPE/";

    public static void plotTimeSeriesGroup(String outFile, List<TimeSeries> timeSeries, int position, int amount, VectorGraphicsEncoder.VectorGraphicsFormat format){
        Random random = new Random(System.currentTimeMillis());

        XYChart chart = new XYChartBuilder().width(1920).height(600).build();
        chart.getStyler().setTheme(new CustomMatlab(Styler.LegendPosition.OutsideE));

//        chart.setTitle("Sample Chart");
        chart.setYAxisTitle("Total (kWh)");
        chart.setXAxisTitle("Time (h)");

        AtomicLong min  = new AtomicLong(Long.MAX_VALUE);
        for(TimeSeries t : timeSeries) {
            if(t == null){
//                LOG.info(String.format("%s has no start time", t.getName()));
                continue;
            }
            if(t.getStart().getTime() < min.get())
                min.set(t.getStart().getTime());
        }

        int k = 0;
        float hue = 0.0f;
        for(TimeSeries aTimeSeries : timeSeries){
            if(aTimeSeries == null){
//                LOG.info(String.format("%s has no start time", aTimeSeries.getName()));
                continue;
            }
            int start = position;
            int offset = Math.toIntExact((aTimeSeries.getStart().getTime() - min.get()) / aTimeSeries.getStep());
            int diff = 0;
            if(offset > start) {
                diff = offset - start;
                start = offset;
            }
            List<Double> time = new ArrayList<>();
            List<Double> chartData = new ArrayList<>();
            double[] data = aTimeSeries.getData();
            for(int i = start; i - offset < data.length && i - start < amount; i++){
                time.add((i+(diff))/4.0);
                if(!Double.isNaN(data[i-offset]))
                    chartData.add(data[i-offset]);
                else
                    chartData.add(2000.0);
            }

            if(chartData.size() < 1){
                LOG.info(String.format("Start: %d\tPos: %d\toff: %d\tsize: %d\t%s", start, position, offset, data.length, aTimeSeries.getStart().toString()));
            }
            XYSeries series = chart.addSeries(aTimeSeries.getName(), time, chartData);
            series.setMarker(SeriesMarkers.NONE);
            series.setLineColor(Color.getHSBColor(hue, 1, 0.8f));
            hue += 1.0f / timeSeries.size();
            k++;

        }

        chart.getStyler().setYAxisMax(10.0);
        chart.getStyler().setYAxisMin(0.0);

        String dir = outFile.substring(0, outFile.lastIndexOf("/"));

        File f = new File(outputDir + dir);
        if(!f.exists())
            f.mkdirs();
        try {
            VectorGraphicsEncoder.saveVectorGraphic(chart, outputDir+outFile, format);
        } catch (IOException e) {
            LOG.severe("Cannot save pdf.");
        }
    }

    public static void plotWholeMAPE(Map<String, List<IODevice.CResult>> results){
        XYChart chart = new XYChartBuilder().width(800).height(400).build();
        chart.getStyler().setTheme(new CustomMatlab(Styler.LegendPosition.OutsideE));

//        chart.setTitle("Sample Chart");
        chart.setYAxisTitle("MAPE (%)");
        chart.setXAxisTitle("Time (h)");

        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);

        int k = 0, kk =0;
        double limit = 200.0;
        float hue = 0.0f;
        float hueStep = 1.0f / results.size();
        for(String key : results.keySet()) {

            List<Double> time = new ArrayList<>();
            List<Double> chartData = new ArrayList<>();
            List<Double> stedevData = new ArrayList<>();
            for (int i = 0; i < results.get(key).size(); i++) {
                time.add((results.get(key).get(i).step + 1 )/4.0);
                chartData.add(results.get(key).get(i).bestMape * 100);
                stedevData.add(results.get(key).get(i).MAPEDistance * 100);

                if(! (limit > stedevData.get(i))){
                    System.out.printf("Value too large: %.4f, setting to %g\n", stedevData.get(i), limit);
                    stedevData.set(i, limit);
                }
            }



            XYSeries series = chart.addSeries(key, time, chartData, stedevData);
            series.setMarker(markers[kk % markers.length ]);
//            series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
            Color color = Color.getHSBColor(hue, 1f, 0.8f);
            series.setMarkerColor(color);
            series.setLineColor(color);
            series.setFillColor(color);
            hue += hueStep;
//            System.out.println(chart.getStyler().getTheme().isErrorBarsColorSeriesColor());
//            XYSeries devseries = chart.addSeries(key, time, stedevData);
//            series.setMarker(SeriesMarkers.NONE);
//            series.setLineColor(colors[k % colors.length]);


        }

//        chart.getStyler().setYAxisMax(100.0);
        chart.getStyler().setYAxisMin(0.0);
        File f = new File(mapeOutputDir);
        if(!f.exists())
            f.mkdir();
        try {
            VectorGraphicsEncoder.saveVectorGraphic(chart, mapeOutputDir+"MAPE", VectorGraphicsEncoder.VectorGraphicsFormat.PDF);
        } catch (IOException e) {
            LOG.severe("Cannot save pdf.");
        }
    }



    public static void plotMAPEperStep(Map<String, List<IODevice.CResult>> results, String name){
        float hue = 0.0f;

        Map<Integer, List<IODevice.CResult>> dataMap = new TreeMap<>();
        results.keySet().forEach(k -> {
            for(IODevice.CResult c : results.get(k)){
                dataMap.putIfAbsent(c.step, new LinkedList<>());
                dataMap.get(c.step).add(c);
            }
        });



        float hueStep = 1.0f / results.size();
        for(Integer step : dataMap.keySet()) {
            CategoryChart chart = new CategoryChartBuilder().width(800).height(600).build();
            chart.getStyler().setTheme(new CustomMatlab(Styler.LegendPosition.OutsideE));

            int k = 0, kk = 0;
            double limit = 200.0;
            chart.setYAxisTitle("MAPE (%)");
            chart.setXAxisTitle("Best Season (h)");

            chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);

            Map<String, List<Double>> time = new HashMap<>();
            Set<Double> encounteredTimes = new TreeSet<>();
            Map<String, List<Double>> chartData = new HashMap<>();
            Map<String, List<Double>> stedevData = new HashMap<>();

            for (IODevice.CResult c : dataMap.get(step)) {

                time.putIfAbsent(c.name, new ArrayList<>());
                chartData.putIfAbsent(c.name, new ArrayList<>());
                stedevData.putIfAbsent(c.name, new ArrayList<>());
                time.get(c.name).add(c.bestSeason / 4.0);
                encounteredTimes.add(c.bestSeason / 4.0);
                chartData.get(c.name).add(c.bestMape * 100);
                stedevData.get(c.name).add(c.MAPEDistance * 100);
                int i = stedevData.get(c.name).size() - 1;
                if (!(limit > stedevData.get(c.name).get(i))) {
                    System.out.printf("Value too large: %.4f, setting to %g\n", stedevData.get(c.name).get(i), limit);
                    stedevData.get(c.name).set(i, limit);
                }
            }

            List<Double> cTime = new ArrayList<>(encounteredTimes);
            for (IODevice.CResult c : dataMap.get(step)) {
                List<Double> cData = new ArrayList<>(encounteredTimes);
                List<Double> sData = new ArrayList<>(encounteredTimes);
                for(int i = 0; i < cTime.size(); i++){
                    if(cTime.get(i).equals(time.get(c.name).get(0))){
                        cData.set(i, chartData.get(c.name).get(0));
                        sData.set(i, stedevData.get(c.name).get(0));
                    }else{
                        cData.set(i, .0);
                        sData.set(i, .0);
                    }
                }
                CategorySeries series = chart.addSeries(c.name, cTime, cData, sData);

//                series.setMarker(markers[kk % markers.length]);
//                series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
//                series.setMarkerColor(colors[k % colors.length]);
                series.setLineColor(Color.getHSBColor(hue, 1f, 0.8f));
                series.setFillColor(Color.getHSBColor(hue, 1f, 0.8f));
                series.setMarkerColor(Color.getHSBColor(hue, 1f, 0.8f));
//                series.
                hue += hueStep;
//                if (chartData.get(c.name).get(0) < 40) {
//                    series.setShowInLegend(false);
//                }
//            System.out.println(chart.getStyler().getTheme().isErrorBarsColorSeriesColor());
//            XYSeries devseries = chart.addSeries(key, time, stedevData);
//            series.setMarker(SeriesMarkers.NONE);
//            series.setLineColor(colors[k % colors.length]);

            }

//            chart.getStyler().setYAxisMax(100.0);
//            chart.getStyler().setYAxisMin(0.0);

            System.out.printf("Plotting for step %d\n", step);
            String nameStep = String.format("%.2fh", (step + 1) / 4.);
            File f = new File(mapeOutputDir);
            if(!f.exists())
                f.mkdir();
            try {
                VectorGraphicsEncoder.saveVectorGraphic(chart, mapeOutputDir + name + "MAPE"+nameStep, VectorGraphicsEncoder.VectorGraphicsFormat.PDF);
            } catch (IOException e) {
                LOG.severe("Cannot save pdf.");
            }
        }
    }

    public static void plotAllMAPEperStep(List<IODevice.ResultInfo> results, String name){

        Map<Integer, List<IODevice.ResultInfo>> dataMap = new TreeMap<>();
        results.forEach(k -> {
                dataMap.putIfAbsent(k.step, new LinkedList<>());
                dataMap.get(k.step).add(k);
        });


        for(Integer step : dataMap.keySet()) {

            CategoryChart chart = new CategoryChartBuilder().width(800).height(400).build();
            chart.getStyler().setTheme(new CustomMatlab(Styler.LegendPosition.OutsideE));
//            chart.getStyler().setHasAnnotations(true);
            chart.getStyler().setLegendVisible(false);
            int k = 0, kk = 0;
            double limit = 200.0;
            chart.setYAxisTitle("MAPE (%)");
            chart.setXAxisTitle("Season (h)");


            chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);

            Map<String, List<Double>> time = new HashMap<>();
            Map<String, List<Double>> chartData = new HashMap<>();
            Map<String, List<Double>> stedevData = new HashMap<>();

            for (IODevice.ResultInfo c : dataMap.get(step)) {

                time.putIfAbsent(c.name, new ArrayList<>());
                chartData.putIfAbsent(c.name, new ArrayList<>());
                time.get(c.name).add(c.season / 4.0);
                chartData.get(c.name).add(c.mape * 100);

            }
            float hue = 0.0f;
            float hueStep = 1.0f / time.size();
            for (String sName : time.keySet()) {

                CategorySeries series = chart.addSeries(sName, time.get(sName), chartData.get(sName));

//                series.setMarker(markers[kk % markers.length]);
//                series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
//                series.setMarkerColor(colors[k % colors.length]);
//                series.setLineColor(Color.getHSBColor(hue, 1f, 0.8f));
                series.setFillColor(Color.getHSBColor(hue, 1f, 0.8f));
                hue += hueStep;
//                if (chartData.get(c.name).get(0) < 40) {
//                    series.setShowInLegend(false);
//                }
//            System.out.println(chart.getStyler().getTheme().isErrorBarsColorSeriesColor());
//            XYSeries devseries = chart.addSeries(key, time, stedevData);
//            series.setMarker(SeriesMarkers.NONE);
//            series.setLineColor(colors[k % colors.length]);

            }

//            chart.getStyler().setYAxisMax(100.0);
//            chart.getStyler().setYAxisMin(0.0);

            System.out.printf("Plotting for step %d\n", step);
            String nameStep = String.format("%.2fh", (step + 1) / 4.);
            File f = new File(mapeOutputDir);
            if(!f.exists())
                f.mkdir();
            try {
                VectorGraphicsEncoder.saveVectorGraphic(chart, mapeOutputDir + name + "MAPE-all-"+nameStep, VectorGraphicsEncoder.VectorGraphicsFormat.PDF);
            } catch (IOException e) {
                LOG.severe("Cannot save pdf.");
            }
        }
    }

    public static void ploteMAPEDifference(Map<String, List<IODevice.CResult>> results, String name){

//        Map<Integer, List<IODevice.CResult>> dataMap = new TreeMap<>();
//        results.keySet().forEach(k -> {
//            for(IODevice.CResult c : results.get(k)){
//                dataMap.putIfAbsent(c.step, new LinkedList<>());
//                dataMap.get(c.step).add(c);
//            }
//        });


        CategoryChart chart = new CategoryChartBuilder().width(1200).height(600).build();
        chart.getStyler().setTheme(new CustomMatlab(Styler.LegendPosition.OutsideE));
        chart.setYAxisTitle("MAPE improvement (%)");
        chart.setXAxisTitle("Prediction step (h)");
//        chart.getStyler().setHasAnnotations(true);
//        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);

        int k = 0, kk = 0;
        double limit = 200.0;
        float hue = 0.0f;
        float hueStep = 1.0f / results.size();
        for(String key : results.keySet()){
            List<IODevice.CResult> res = results.get(key);
            List<Double> time = new ArrayList<>();
            List<Double> chartData = new ArrayList<>();
            for(IODevice.CResult r : res){
                time.add((r.step + 1)/4.);
                chartData.add(r.bestMape * 100);
            }
            CategorySeries series = chart.addSeries(key, time, chartData);
//            series.setMarker(markers[kk % markers.length]);
//                series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
//            series.setMarkerColor(colors[k % colors.length]);
            series.setLineColor(Color.getHSBColor(hue, 1f, 0.8f));
            series.setFillColor(Color.getHSBColor(hue, 1f, 0.8f));
            hue += hueStep;
            for(int i = 0; i < chartData.size(); i++) {
                if (chartData.get(i) < 0) {
                    series.setShowInLegend(false);
                }else{
                    series.setShowInLegend(true);
                    break;
                }
            }



        }

        System.out.printf("Plotting difference\n");
        File f = new File(mapeOutputDir);
        if(!f.exists())
            f.mkdir();
        try {
            VectorGraphicsEncoder.saveVectorGraphic(chart, mapeOutputDir + name + "MAPE", VectorGraphicsEncoder.VectorGraphicsFormat.PDF);
        } catch (IOException e) {
            LOG.severe("Cannot save pdf.");

        }
    }

    public static void plotAllMAPEDifferencePerStep(List<IODevice.ResultInfo> comp1 , List<IODevice.ResultInfo> comp2, String name){

        Map<Integer, List<IODevice.ResultInfo>> dataMap = new TreeMap<>();
        Map<Integer, List<IODevice.ResultInfo>> dataMap2 = new TreeMap<>();
        comp1.forEach(k -> {
            dataMap.putIfAbsent(k.step, new LinkedList<>());
            dataMap.get(k.step).add(k);
        });

        comp2.forEach(k -> {
            dataMap2.putIfAbsent(k.step, new LinkedList<>());
            dataMap2.get(k.step).add(k);
        });

        for(Integer step : dataMap.keySet()) {

            CategoryChart chart = new CategoryChartBuilder().width(1200).height(600).build();
            chart.getStyler().setTheme(new CustomMatlab(Styler.LegendPosition.OutsideE));
//            chart.getStyler().setHasAnnotations(true);
//            chart.getStyler().setLegendVisible(false);
            int k = 0, kk = 0;
            double limit = 200.0;
            chart.setYAxisTitle("MAPE improvement (%)");
            chart.setXAxisTitle("Season (h)");


            chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);

            Map<String, List<Double>> time = new HashMap<>();
            Map<String, List<Double>> chartData = new HashMap<>();
            Map<String, List<Double>> stedevData = new HashMap<>();


            List<IODevice.ResultInfo> resultInfos2 = dataMap2.get(step);
            List<IODevice.ResultInfo> resultInfos1 = dataMap.get(step);
            for(int i = 0; i < resultInfos2.size(); i++){
                IODevice.ResultInfo r2 = resultInfos2.get(i);
                IODevice.ResultInfo r = resultInfos1.get(i);
                int j = i+1;
                while (r2.season != r.season){
                    r = resultInfos1.get(j++);
                }

                time.putIfAbsent(r2.name, new ArrayList<>());
                chartData.putIfAbsent(r2.name, new ArrayList<>());
                time.get(r2.name).add(r2.season / 4.0);
                double dMape = r.mape - r2.mape;
                chartData.get(r2.name).add(dMape * 100);

//                time.add((r.season + 1)/4.);
//                chartData.add((r.mape - r2.mape) * 100);
            }


            float hue = 0.0f;
            float hueStep = 1.0f / time.size();
            for (String sName : time.keySet()) {

                CategorySeries series = chart.addSeries(sName, time.get(sName), chartData.get(sName));

                for(int i = 0; i < chartData.get(sName).size(); i++) {
                    if (chartData.get(sName).get(i) > 0) {
                        series.setShowInLegend(true);
                        break;
                    }else{
                        series.setShowInLegend(false);
                    }
                }
//                series.setMarker(markers[kk % markers.length]);
//                series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
//                series.setMarkerColor(colors[k % colors.length]);
//                series.setLineColor(Color.getHSBColor(hue, 1f, 0.8f));
                series.setFillColor(Color.getHSBColor(hue, 1f, 0.8f));
                hue += hueStep;
//                if (chartData.get(c.name).get(0) < 40) {
//                    series.setShowInLegend(false);
//                }
//            System.out.println(chart.getStyler().getTheme().isErrorBarsColorSeriesColor());
//            XYSeries devseries = chart.addSeries(key, time, stedevData);
//            series.setMarker(SeriesMarkers.NONE);
//            series.setLineColor(colors[k % colors.length]);

            }

//            chart.getStyler().setYAxisMax(100.0);
//            chart.getStyler().setYAxisMin(0.0);

            System.out.printf("Plotting for step %d\n", step);
            String nameStep = String.format("%.2fh", (step + 1) / 4.);
            File f = new File(mapeOutputDir);
            if(!f.exists())
                f.mkdir();
            try {
                VectorGraphicsEncoder.saveVectorGraphic(chart, mapeOutputDir + name + "MAPE-dif-all-"+nameStep, VectorGraphicsEncoder.VectorGraphicsFormat.PDF);
            } catch (IOException e) {
                LOG.severe("Cannot save pdf.");
            }
        }
    }

    public static void plotMAPEHistogram(Map<String, Map<Integer, Map<Integer, List<String>>>> plotMap) {

        Map<Integer, CategoryChart> chartMap = new TreeMap<>();
        for(String seriesName : plotMap.keySet()) {

            for (Integer step : plotMap.get(seriesName).keySet()) {

                if(chartMap.get(step) == null) {
                    chartMap.put(step, new CategoryChartBuilder().width(800).height(400).build());
                    chartMap.get(step).setYAxisTitle("Amount of data set");
                    chartMap.get(step).setXAxisTitle("MAPE");
                    chartMap.get(step).getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
                }

                CategoryChart chart = chartMap.get(step);
                chart.getStyler().setTheme(new CustomMatlab(Styler.LegendPosition.OutsideE));

                Map<String, List<Integer>> time = new HashMap<>();
                Map<String, List<Double>> chartData = new HashMap<>();

                double sampleSize = 0;

                for (Integer mape : plotMap.get(seriesName).get(step).keySet()) {

                    time.putIfAbsent(seriesName, new ArrayList<>());
                    chartData.putIfAbsent(seriesName, new ArrayList<>());

                    sampleSize += plotMap.get(seriesName).get(step).get(mape).size();

                }

                for (Integer mape : plotMap.get(seriesName).get(step).keySet()) {

                    time.get(seriesName).add(mape);
                    chartData.get(seriesName).add(plotMap.get(seriesName).get(step).get(mape).size() / sampleSize);

                }
                float hue = 0.0f;
                float hueStep = 1.0f / time.size();
                for (String sName : time.keySet()) {

                    CategorySeries series = chart.addSeries(sName, time.get(sName), chartData.get(sName));

//                series.setMarker(markers[kk % markers.length]);
//                series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
//                series.setMarkerColor(colors[k % colors.length]);
//                series.setLineColor(Color.getHSBColor(hue, 1f, 0.8f));
//                    series.setFillColor(Color.getHSBColor(hue, 1f, 0.8f));
                    hue += hueStep;
//                if (chartData.get(c.name).get(0) < 40) {
//                    series.setShowInLegend(false);
//                }
//            System.out.println(chart.getStyler().getTheme().isErrorBarsColorSeriesColor());
//            XYSeries devseries = chart.addSeries(key, time, stedevData);
//            series.setMarker(SeriesMarkers.NONE);
//            series.setLineColor(colors[k % colors.length]);

                }

//            chart.getStyler().setYAxisMax(100.0);
//            chart.getStyler().setYAxisMin(0.0);

            }
        }

        for(Integer step : chartMap.keySet()){
            CategoryChart chart = chartMap.get(step);
            System.out.printf("Plotting for step %d\n", step);
            String nameStep = String.format("%.2fh", (step + 1) / 4.);
            File f = new File(mapeOutputDir);
            if (!f.exists())
                f.mkdir();
            try {
                VectorGraphicsEncoder.saveVectorGraphic(chart, mapeOutputDir + "full-histogram" + nameStep, VectorGraphicsEncoder.VectorGraphicsFormat.PDF);
            } catch (IOException e) {
                LOG.severe("Cannot save pdf.");
            }
        }
    }

    public static void plotMAPEHistogramPerSeries(Map<String, Map<Integer, Map<Integer, List<String>>>> plotMap, String dir) {

        for(String seriesName : plotMap.keySet()) {

            CategoryChart chart = new CategoryChartBuilder().width(700).height(400).build();
            chart.setYAxisTitle("Amount of data set");
            chart.setXAxisTitle("MAPE");
            chart.setTitle(seriesName);
            chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
            chart.getStyler().setTheme(new CustomMatlab(Styler.LegendPosition.InsideNW));
//            chart.getStyler().setYAxisMax(1.0);
//            chart.getStyler().setAxisTickLabelsFont(new Font("Verdana", Font.PLAIN, 18));
            Map<Integer, List<Integer>> time = new HashMap<>();
            Map<Integer, List<Double>> chartData = new HashMap<>();

            for (Integer step : plotMap.get(seriesName).keySet()) {


                double sampleSize = 0;


                time.putIfAbsent(step, new ArrayList<>());
                chartData.putIfAbsent(step, new ArrayList<>());
                for (Integer mape : plotMap.get(seriesName).get(step).keySet()) {

                    sampleSize += plotMap.get(seriesName).get(step).get(mape).size();

                }

                for (Integer mape : plotMap.get(seriesName).get(step).keySet()) {

                    time.get(step).add(mape);
                    chartData.get(step).add(plotMap.get(seriesName).get(step).get(mape).size() / sampleSize);

                }

//            chart.getStyler().setYAxisMin(0.0);

            }
            float hue = 0.0f;
            float hueStep = 1.0f / time.size();
            final Set<Integer> allTimes = new TreeSet<>();
            for(Integer step : time.keySet()){
                allTimes.addAll(time.get(step));
            }
            List<Integer> allTimesList = new ArrayList<>();
            for(int i = -110 ; i <= 110; i+= 10 ){
                allTimesList.add(i);
            }
            for (Integer step : time.keySet()) {
                List<Double> data = new ArrayList<>();
                for(Integer t : allTimesList){
                    int positive = 1;
                    if(t < 0)
                        positive = -1;
                    if(time.get(step).contains(t)){
                        data.add(positive * chartData.get(step).get(time.get(step).indexOf(t)));
                    }else{
                        data.add(0.0);
                    }
                }

                CategorySeries series = chart.addSeries((step + 1)/4. + "h", allTimesList, data);

                    series.setFillColor(Color.getHSBColor(hue, 1f, 0.8f));
                hue += hueStep;

            }
            File f = new File(dir);
            if (!f.exists())
                f.mkdirs();
            try {
                VectorGraphicsEncoder.saveVectorGraphic(chart, dir + seriesName + "-histogram", VectorGraphicsEncoder.VectorGraphicsFormat.PDF);
            } catch (IOException e) {
                LOG.severe("Cannot save pdf.");
            }
        }
    }

    public static void plotMAPEBestStepsPerSeries(Map<String, Map<Integer, Map<Integer, List<String>>>> plotMap) {

        for(String seriesName : plotMap.keySet()) {

            CategoryChart chart = new CategoryChartBuilder().width(600).height(330).build();
            chart.setYAxisTitle("Amount of data set");
            chart.setXAxisTitle("Best Season");
            chart.setTitle(seriesName);
            chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
            chart.getStyler().setTheme(new CustomMatlab(Styler.LegendPosition.InsideNE));
            chart.getStyler().setYAxisMax(1.0);
//            chart.getStyler().setAxisTickLabelsFont(new Font("Verdana", Font.PLAIN, 18));
            Map<Integer, List<Integer>> time = new HashMap<>();
            Map<Integer, List<Double>> chartData = new HashMap<>();

            for (Integer step : plotMap.get(seriesName).keySet()) {


                double sampleSize = 0;


                time.putIfAbsent(step, new ArrayList<>());
                chartData.putIfAbsent(step, new ArrayList<>());
                for (Integer mape : plotMap.get(seriesName).get(step).keySet()) {
                    sampleSize += plotMap.get(seriesName).get(step).get(mape).size();
                }
                for (Integer mape : plotMap.get(seriesName).get(step).keySet()) {
                    time.get(step).add(mape);
                    chartData.get(step).add(plotMap.get(seriesName).get(step).get(mape).size() / sampleSize);
                }
            }
            float hue = 0.0f;
            float hueStep = 1.0f / time.size();
            final Set<Integer> allTimes = new TreeSet<>();
            for(Integer step : time.keySet()){
                allTimes.addAll(time.get(step));
            }
            List<Integer> allTimesList = new ArrayList<>(allTimes);
            for (Integer step : time.keySet()) {
                List<Double> data = new ArrayList<>();
                for(Integer t : allTimes){
                    if(time.get(step).contains(t)){
                        data.add(chartData.get(step).get(time.get(step).indexOf(t)));
                    }else{
                        data.add(0.0);
                    }
                }

                CategorySeries series = chart.addSeries((step + 1)/4. + "h", allTimesList, data);

                series.setFillColor(Color.getHSBColor(hue, 1f, 0.8f));
                hue += hueStep;
            }
            File f = new File(mapeOutputDir);
            if (!f.exists())
                f.mkdirs();
            try {
                VectorGraphicsEncoder.saveVectorGraphic(chart, mapeOutputDir + seriesName + "-bestSeason", VectorGraphicsEncoder.VectorGraphicsFormat.PDF);
            } catch (IOException e) {
                LOG.severe("Cannot save pdf.");
            }
        }
    }

    private static class CustomMatlab extends MatlabTheme{

        private Styler.LegendPosition legendPosition;

        public CustomMatlab(Styler.LegendPosition lp){
            legendPosition = lp;
        }

        @Override
        public Styler.LegendPosition getLegendPosition() {
            return legendPosition;
        }

        @Override
        public boolean isPlotGridLinesVisible() {
            return true;
        }
        @Override
        public boolean isPlotGridVerticalLinesVisible() {
            return false;
        }
        @Override
        public boolean isPlotGridHorizontalLinesVisible() {
            return true;
        }

        @Override
        public boolean isErrorBarsColorSeriesColor() {
            return true;
        }


    }
}
