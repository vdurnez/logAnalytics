package analytics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 07/09/15.
 */
@Configuration
public class AnalyticsConfiguration {

    @Value("${logfile:/tmp/logs.tsv}")
    private String logFile;

    public String getLogFile() {
        return logFile;
    }
}
