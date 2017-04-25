package ro.bismart.clustering.util;

import ro.bismart.clustering.model.TimeSeries;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by adrian on 13.01.2017.
 */
public class UsageParser {

    private static final Logger LOG = Logger.getLogger("UsageParser");
    private static final String days = "ALL";

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
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int i = calendar.get(Calendar.DAY_OF_WEEK);
                if(!dayAllowed(i)){
                    continue;
                }
                Double val = Double.parseDouble(tokens[2]);
                if(val.equals(Double.NaN)) {
                    if(values.size() > 0) {
                        val = values.get(values.size() - 1);
                    }else continue;

                }else{
                    if(old == null){
                        result.setStart(date);
                        old = date;
                    }else{
                        interval = date.getTime() - old.getTime();
                        old = date;
                    }
                }
                values.add(val);

            }
            result.setStep(interval);
            result.setTimeUnit(TimeUnit.MILLISECONDS);
            double[] t = new double[values.size()];
            for(int i = 0 ; i < values.size(); i++){
                t[i] = values.get(i);
            }
            result.setData(t);
            result.setName(seriesName + "-" + days);

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

    private static boolean dayAllowed(int i) {
        if(days.equals("WEEKDAYS")){
            return i != Calendar.SATURDAY && i != Calendar.SUNDAY;
        }else if(days.equals("WEEKENDS")){
            return i == Calendar.SATURDAY || i == Calendar.SUNDAY;
        }else if(days.equals("ALL")){
            return true;
        }
        return false;
    }
}
