package ro.bismart.clustering.util;

import ro.bismart.clustering.model.TimeSeries;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by adrian on 13.01.2017.
 */
public class UsageParser {

    private static final Logger LOG = Logger.getLogger("UsageParser");

    public static TimeSeries parseFile(String fileName, String seriesName) {
        File f = new File(fileName);
        if(!f.exists()) {
            LOG.severe(f.getPath() + " does not exist");
            return null;
        }
        Scanner scanner = null;


        try {
            scanner = new Scanner(f);
            TimeSeries result  = new TimeSeries();
            //header
            String line = scanner.nextLine();
            Date old = null;
            List<Double> values = new ArrayList<>();
            long interval = 0;
            while (scanner.hasNext()){
                line = scanner.nextLine();
                String[] tokens = line.split(",");
                DateFormat dateInstance = new SimpleDateFormat("MM/d/yy h:mm");
                Date date = dateInstance.parse(tokens[1]);
                if(old == null){
                    result.setStart(date);
                    old = date;
                }else{
                    interval = date.getTime() - old.getTime();
                }
                Double val = Double.parseDouble(tokens[2]);
                if(val.equals(Double.NaN))
                    val = .0;
                values.add(val);

            }
            result.setStep(interval);
            result.setTimeUnit(TimeUnit.MILLISECONDS);
            Double[] t = new Double[values.size()];
            result.setData(values.toArray(t));
            result.setName(seriesName);

            return result;


        } catch (FileNotFoundException e) {
            LOG.severe(e.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null){
                scanner.close();
            }
        }

        return null;
    }
}
