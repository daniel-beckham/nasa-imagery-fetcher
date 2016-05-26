package com.dsbeckham.nasaimageryfetcher.util;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static Calendar convertDateToCalendar(String input, String inputFormat) {
        Date date = null;

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(inputFormat, Locale.US);
            date = simpleDateFormat.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance();

        if (date != null) {
            calendar.setTime(date);
        }

        return calendar;
    }

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
}
