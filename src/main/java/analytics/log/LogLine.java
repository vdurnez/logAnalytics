package analytics.log;

import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogLine {
    public Date timestamp;
    public String content;

    private static java.text.SimpleDateFormat SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
    private static Logger logger = Logger.getLogger(LogLine.class);


    public static LogLine readLine(String line) {
        String[] lineItems = line.split("\\t");
        if (lineItems.length == 2) {
            LogLine item = new LogLine();
            item.content = lineItems[1];
            try {
                item.timestamp = SimpleDateFormat.parse(lineItems[0]);
                return item;
            } catch (ParseException e) {
                logger.info("skip wrong line " + line);
                return null;
            }
        } else {
            return null;
        }
    }

}
