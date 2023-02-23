package org.bahmni.module.hip.web.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static Date parseDate(String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.parse(date);
    }

    public static Date parseDateTime(String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.parse(date);
    }

    public static boolean isDateBetweenDateRange(String date, String fromDate, String toDate) throws ParseException {
        return parseDate(date).compareTo(parseDate(fromDate)) >= 0 && parseDate(date).compareTo(parseDate(toDate)) < 0;
    }
}
