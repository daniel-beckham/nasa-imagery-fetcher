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
    public static String convertDateToCustomDateFormat(String input, String inputFormat, String outputFormat) {
        Date date = null;

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(inputFormat, Locale.US);
            date = simpleDateFormat.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String output = "";

        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(outputFormat, Locale.getDefault());
            output = simpleDateFormat.format(date);
        }

        if (output.isEmpty()) {
            output = input;
        }

        return output;
    }

    public static String convertDateToLongDateFormat(Context context, String input, String inputFormat) {
        Date date = null;

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(inputFormat, Locale.US);
            date = simpleDateFormat.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String output = "";

        if (date != null) {
            java.text.DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(context);
            output = dateFormat.format(date);
        }

        if (output.isEmpty()) {
            output = input;
        }

        return output;
    }

    public static Calendar getAssociatedPreferenceCalendar(Context context, Preference preference) {
        Calendar calendar = Calendar.getInstance();
        String time = PreferenceManager.getDefaultSharedPreferences(context).getString(preference.getKey(), "");

        if (!time.isEmpty()) {
            Date date = null;
            SimpleDateFormat simpleDateFormat = null;

            // If it doesn't contain a space, then it's most likely a
            // 24-hour clock. Otherwise, it's probably be a 12-hour clock.
            if (!time.contains(" ")) {
                simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            } else {
                simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            }

            try {
                date = simpleDateFormat.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (date != null) {
                calendar.setTime(date);
            }
        }

        return calendar;
    }

    public static String formatTime(Context context, int hour, int minute) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        try {
            simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            date = simpleDateFormat.parse(String.format(Locale.US, "%02d:%02d", hour, minute));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String output = "";

        if (date != null) {
            if (!android.text.format.DateFormat.is24HourFormat(context)) {
                simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            }

            output = simpleDateFormat.format(date);
        }

        if (output.isEmpty()) {
            output = String.format(Locale.US, "%02d:%02d", hour, minute);
        }

        return output;
    }
}
