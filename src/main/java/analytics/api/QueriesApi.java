package analytics.api;

import analytics.AnalyticsConfiguration;
import analytics.log.LogLine;
import analytics.model.StreamDistinctElements;
import analytics.model.StreamTopElements;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/1/queries")
public class QueriesApi {

    private Logger logger = Logger.getLogger(QueriesApi.class);

    @Autowired
    AnalyticsConfiguration analyticsConfiguration;

    @RequestMapping(value = "/count/{datePattern}", method = RequestMethod.GET)
    public CountDTO countInTimeRange(@PathVariable("datePattern") String datePattern) throws IOException {
        logger.info("start 1/queries/count/" + datePattern);
        LocalDateTime start = LocalDateTime.now();

        DateRangeUtils.DateRange dateRange = DateRangeUtils.getRange(datePattern);
        if (dateRange == null) {
            throw new IllegalArgumentException("datePattern '" + datePattern + "' is not recognized");
        }
        logger.info("start 1/queries/count/" + datePattern + " - from " + dateRange.startDate + " to " + dateRange.endDate);

        StreamDistinctElements streamDistinctElements = new StreamDistinctElements();

        BufferedReader bufferedReader = readLogFile();
        String line;
        long idx = 0;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                idx++;
                LogLine logLine = LogLine.readLine(line);
                if (logLine.timestamp.getTime() >= dateRange.startDate.getTime() &&
                        logLine.timestamp.getTime() < dateRange.endDate.getTime()) {
                    streamDistinctElements.addElement(logLine.content);
                }
            }
        } catch (IOException e) {
            logger.error("error when reading line " + idx + " of file " + analyticsConfiguration.getLogFile() + " exception=" + e);
            throw e;
        }

        int cardinality = streamDistinctElements.cardinality();

        LocalDateTime end = LocalDateTime.now();
        long durationMilliseconds = Duration.between(start, end).toMillis();
        logger.info("end 1/queries/count/" + datePattern + " - " + cardinality + " duration=" + durationMilliseconds + " ms");

        return new CountDTO(cardinality);
    }

    public class CountDTO {
        public long count;

        public CountDTO(long count) {
            this.count = count;
        }
    }


    @RequestMapping(value = "/popular/{datePattern}", method = RequestMethod.GET)
    public QueriesDTO getPopular(@PathVariable("datePattern") String datePattern, @RequestParam(value = "size", defaultValue = "10") int size) throws IOException {
        logger.info("start 1/queries/popular/" + datePattern + " size=" + size);
        LocalDateTime start = LocalDateTime.now();

        DateRangeUtils.DateRange dateRange = DateRangeUtils.getRange(datePattern);
        if (dateRange == null) {
            throw new IllegalArgumentException("datePattern '" + datePattern + "' is not recognized");
        }
        logger.info("start 1/queries/popular/" + datePattern + " size=" + size + " - from " + dateRange.startDate + " to " + dateRange.endDate);

        // note : one could adjust capacity to logs volumetry and variability
        StreamTopElements<String> streamTopElements = new StreamTopElements<>(5000);

        BufferedReader bufferedReader = readLogFile();
        String line;
        long idx = 0;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                idx++;
                LogLine logLine = LogLine.readLine(line);
                if (logLine.timestamp.getTime() >= dateRange.startDate.getTime() &&
                        logLine.timestamp.getTime() < dateRange.endDate.getTime()) {
                    streamTopElements.addElement(logLine.content);
                }
            }
        } catch (IOException e) {
            logger.error("error when reading line " + idx + " of file " + analyticsConfiguration.getLogFile() + " exception=" + e);
            throw e;
        }

        QueriesDTO result = new QueriesDTO();

        List<StreamTopElements<String>.Count> topCountElements = streamTopElements.topElementsWithCount(size);

        for (StreamTopElements.Count topElement : topCountElements) {
            result.addQueryCount((String) topElement.element, topElement.count);
        }

        LocalDateTime end = LocalDateTime.now();
        long durationMilliseconds = Duration.between(start, end).toMillis();
        logger.info("end 1/queries/popular/" + datePattern + " size=" + size + " duration=" + durationMilliseconds + " ms - " + result);

        return result;
    }

    public static class QueriesDTO {
        public List<QueryCount> queries = new ArrayList<>();

        public void addQueryCount(String query, long count) {
            queries.add(new QueryCount(query, count));
        }


        @Override
        public String toString() {
            return "queries=" + queries;
        }
    }

    public static class QueryCount {
        public String query;
        public long count;

        public QueryCount(String query, long count) {
            this.query = query;
            this.count = count;
        }

        @Override
        public String toString() {
            return count + ":" + query;
        }
    }

    public BufferedReader readLogFile() throws IOException {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = Files.newBufferedReader(Paths.get(analyticsConfiguration.getLogFile()));
            return bufferedReader;

        } catch (IOException e) {
            logger.error("error when opening file " + analyticsConfiguration.getLogFile() + " exception=" + e);
            throw e;
        }
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({IOException.class, JsonParseException.class, JsonMappingException.class})
    public
    @ResponseBody
    String handleInvalidException(Exception e) {
        logger.error(HttpStatus.INTERNAL_SERVER_ERROR + " - " + e);
        return "Exception " + e.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, Exception.class})
    public
    @ResponseBody
    String handleException(Exception e) {
        logger.error(HttpStatus.BAD_REQUEST + " - " + e);
        return e.getMessage();
    }
}
