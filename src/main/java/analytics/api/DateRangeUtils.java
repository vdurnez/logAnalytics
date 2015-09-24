package analytics.api;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Locale;

public class DateRangeUtils {

    public static DateRange getRange(String dateString) {

        DateRange yearRange = getRange(dateString, "yyyy", ChronoUnit.YEARS);
        if (yearRange != null)
            return yearRange;

        DateRange monthRange = getRange(dateString, "yyyy-MM", ChronoUnit.MONTHS);
        if (monthRange != null)
            return monthRange;

        DateRange dayRange = getRange(dateString, "yyyy-MM-dd", ChronoUnit.DAYS);
        if (dayRange != null)
            return dayRange;

        DateRange hourRange = getRange(dateString, "yyyy-MM-dd HH", ChronoUnit.HOURS);
        if (hourRange != null)
            return hourRange;

        DateRange minRange = getRange(dateString, "yyyy-MM-dd HH:mm", ChronoUnit.MINUTES);
        if (minRange != null)
            return minRange;

        DateRange secRange = getRange(dateString, "yyyy-MM-dd HH:mm:ss", ChronoUnit.SECONDS);
        if (secRange != null)
            return secRange;

        return null;
    }

    public static DateRange getRange(String dateString, String dateFormat, TemporalUnit unit) {
        try {
            Date startDate = DateUtils.parseDate(dateString, Locale.getDefault(), dateFormat);
            LocalDateTime endDateTime = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault()).plus(1, unit);
            Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());

            return new DateRange(startDate, endDate);

        } catch (ParseException e) {
            return null;
        }

    }

    public static class DateRange {
        public Date startDate;
        public Date endDate;

        public DateRange(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
