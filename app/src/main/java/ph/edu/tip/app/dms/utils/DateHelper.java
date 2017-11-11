package ph.edu.tip.app.dms.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateHelper {

    private static String currentLocalTime;

    public static String TO_AM_PM(String dateToConvert) {

        DateFormat f1 = new SimpleDateFormat("HH:mm:ss", Locale.US);
        String convertedDate = "";
        try {
            Date date = f1.parse(dateToConvert);
            SimpleDateFormat am_pm = new SimpleDateFormat("h:mm a", Locale.US);
            convertedDate = am_pm.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return convertedDate;
    }

    public static String TO_AM_PM_DATE(Date dateToConvert) {
        String date;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        date = sdf.format(dateToConvert);


        return date;
    }



    public static Date String_To_Time(String dateToConvert) {

        DateFormat f1 = new SimpleDateFormat("HH:mm:ss", Locale.US);
        Date convertedDate = null;
        try {
            convertedDate = f1.parse(dateToConvert);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return convertedDate;
    }


    public static String TO_DAY_OF_WEEK(String dateToConvert) {
        if (dateToConvert == null || dateToConvert.isEmpty()) {
            Log.d("Error date", "dateToConvert is empty");
        }
        String convertedDate;
        String[] items1 = dateToConvert.split("-");
        int input_year = Integer.parseInt(items1[0]);
        int input_month = Integer.parseInt(items1[1]);
        int input_day = Integer.parseInt(items1[2]);

        String dateString = String.format(Locale.US, "%d-%d-%d", input_year, input_month, input_day);
        Date dates = null;
        try {
            dates = new SimpleDateFormat("yyyy-M-d", Locale.US).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat month_date = new SimpleDateFormat("MMM", Locale.US);
        String nameOfMonth = month_date.format(dates);
        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(dates);
        String dayOfMonthsuf;

        if (input_day >= 11 && input_day <= 13) {
            dayOfMonthsuf = input_day + "th";
        } else if (input_day == 1 || input_day == 21) {
            dayOfMonthsuf = input_day + "st";
        } else if (input_day == 2 || input_day == 22) {
            dayOfMonthsuf = input_day + "nd";
        } else if (input_day == 3 || input_day == 23) {
            dayOfMonthsuf = input_day + "rd";
        } else {
            dayOfMonthsuf = input_day + "th";
        }

        convertedDate = nameOfMonth + " , " + dayOfWeek + " " + dayOfMonthsuf;
        return convertedDate;

    }


    public static String getCurrentDate() {

        DateFormat df = new SimpleDateFormat("yyyy-M-d", Locale.US);
        String date = df.format(Calendar.getInstance().getTime());
        String convertedDate;

        String[] items1 = date.split("-");

        int input_year = Integer.parseInt(items1[0]);
        int input_month = Integer.parseInt(items1[1]);
        int input_day = Integer.parseInt(items1[2]);

        String dateString = String.format(Locale.US, "%d-%d-%d", input_year, input_month, input_day);
        Date dates = null;
        try {
            dates = new SimpleDateFormat("yyyy-M-d", Locale.US).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat month_date = new SimpleDateFormat("MMM", Locale.US);
        String nameOfMonth = month_date.format(dates);
        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(dates);
        String dayOfMonthsuf;

        if (input_day >= 11 && input_day <= 13) {
            dayOfMonthsuf = input_day + "th";
        } else if (input_day == 1 || input_day == 21) {
            dayOfMonthsuf = input_day + "st";
        } else if (input_day == 2 || input_day == 22) {
            dayOfMonthsuf = input_day + "nd";
        } else if (input_day == 3 || input_day == 23) {
            dayOfMonthsuf = input_day + "rd";
        } else {
            dayOfMonthsuf = input_day + "th";
        }

        convertedDate = dayOfWeek + ", " + nameOfMonth + " " + dayOfMonthsuf;
        return convertedDate;
    }


    public static String getCurrentDate_YYYY_MM_DD() {
        DateFormat df = new SimpleDateFormat("yyyy-M-d", Locale.US);

        return df.format(Calendar.getInstance().getTime());
    }

    public static String getCurrentTime() {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return currentLocalTime = sdf.format(cal.getTime());
    }
}