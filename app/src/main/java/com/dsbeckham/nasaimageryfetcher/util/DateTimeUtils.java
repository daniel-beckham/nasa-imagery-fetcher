package com.dsbeckham.nasaimageryfetcher.util;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    public static Calendar getAssociatedPreferenceCalendar(Context context, Preference preference) {
        final Calendar calendar = Calendar.getInstance();

        String time = PreferenceManager.getDefaultSharedPreferences(context).getString(preference.getKey(), "");

        if (!time.isEmpty()) {
            SimpleDateFormat simpleDateFormat;

            // If it doesn't contain a space, then it must be a 24-hour clock.
            // Otherwise, it should be a 12-hour clock.
            if (!time.contains(" ")) {
                simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            } else {
                simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            }
            try {
                Date date = simpleDateFormat.parse(time);
                calendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return calendar;
    }

    public static String formatDate(Context context, String input, String format) {
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(context);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());

        Date date = null;
        String output = "";

        try {
            date = simpleDateFormat.parse(input);
            output = dateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return output;
    }

    public static String formatTime(Context context, int hour, int minute) {
        String time = String.format(Locale.US, "%02d:%02d", hour, minute);

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = simpleDateFormat.parse(time);

            if (!android.text.format.DateFormat.is24HourFormat(context)) {
                simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            }

            time = simpleDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }
}
