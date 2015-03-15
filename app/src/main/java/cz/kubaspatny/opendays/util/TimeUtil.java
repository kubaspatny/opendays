package cz.kubaspatny.opendays.util;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.TimeZone;

/**
 * Created by Kuba on 15/3/2015.
 */
public class TimeUtil {

    private static DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    public static DateTime parseTimestamp(String timestamp){
        DateTime dateTime = dateTimeFormatter.parseDateTime(timestamp);
        return dateTime.toDateTime(DateTimeZone.forTimeZone(TimeZone.getDefault()));
    }

}
