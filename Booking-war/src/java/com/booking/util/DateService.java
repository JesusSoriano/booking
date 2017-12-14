package com.booking.util;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Jesús Soriano
 */
public class DateService implements Serializable {

    public static Date getDawnDay(Date date) {
        // getting the time of the beginning of the day
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 1);
        return c.getTime();
    }

    public static Date getMidnightDay(Date date) {
        // getting the time of the end of the day
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }

    public static Date getDaysEarlier(Date date, int days) {
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        return new Date(date.getTime() - (7 * DAY_IN_MS));
    }
}
