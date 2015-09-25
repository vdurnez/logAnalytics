package analytics.log;

import analytics.AnalyticsConfiguration;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;

@Component
public class LogUtils {

    private Logger logger = Logger.getLogger(LogUtils.class);
    private SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());

    /*
    populated by loadLogFile() :
    key is timeStamp (minute-level)
    value is first line in file that corresponds to this timestamp
     */
    private TreeMap<Long, Integer> logIndex = new TreeMap<>();


    @Autowired
    AnalyticsConfiguration analyticsConfiguration;

    public BufferedReader getLogFileReader() throws IOException {
        try {
            BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(analyticsConfiguration.getLogFile()));
            return bufferedReader;
        } catch (IOException e) {
            logger.error("error when opening file " + analyticsConfiguration.getLogFile() + " exception=" + e);
            throw e;
        }
    }

    /**
     * read log file (once at startup) in order to build time mapping index
     * (key=timeStamp at minute-level)
     * (value=index of first line that matches this timestamp)
     *
     * @throws IOException
     */
    @PostConstruct
    private void loadLogFile() throws IOException {
        BufferedReader bufferedReader = getLogFileReader();
        String line;
        int idx = 0;
        long currentDate = 0;
        long lineDateMinute = 0;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                idx++;
                final String[] lineItems = line.split("\\t");
                try {
                    Date lineDate = logDateFormat.parse(lineItems[0]);
                    lineDateMinute = lineDate.getTime() / 1000 / 60;
                    if (lineDateMinute > currentDate) {
                        currentDate = lineDateMinute;
                        logIndex.put(lineDateMinute, idx);
                    }
                } catch (ParseException e1) {
                    logger.info("failure when parsing " + lineItems[0] + " => ignore line - exception=" + e1);
                }
            }
        } catch (IOException e) {
            logger.error("error when reading line " + idx + " of file " + analyticsConfiguration.getLogFile() + " exception=" + e);
            throw e;
        }

        logger.info("Analyzed log file " + analyticsConfiguration.getLogFile() + " => " + idx + " lines with " + logIndex.size() + " logIndex entries");
    }

    public int findStartLineFor(String dateString) {
        Date startDate = getDate(dateString, "yyyy");
        if (startDate == null)
            startDate = getDate(dateString, "yyyy-MM");
        if (startDate == null)
            startDate = getDate(dateString, "yyyy-MM-dd");
        if (startDate == null)
            startDate = getDate(dateString, "yyyy-MM-dd HH");
        if (startDate == null)
            startDate = getDate(dateString, "yyyy-MM-dd HH:mm");
        if (startDate == null)
            startDate = getDate(dateString, "yyyy-MM-dd HH:mm:ss");

        if (startDate == null) {
            return -1;
        }
        long startDateMinutePrecision = startDate.getTime() / 1000 / 60;
        return logIndex.ceilingEntry(startDateMinutePrecision).getValue();
    }

    public Date getDate(String dateString, String datePattern) {
        try {
            return DateUtils.parseDate(dateString, Locale.getDefault(), datePattern);
        } catch (ParseException e) {
            return null;
        }

    }
}
