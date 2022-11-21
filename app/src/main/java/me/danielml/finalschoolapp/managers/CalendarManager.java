package me.danielml.finalschoolapp.managers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;

public class CalendarManager {

    private final String[] calendarProjectionArray = {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
    };

    public void printCalendars(Context context) {


        Cursor cur = null;
        ContentResolver resolver = context.getContentResolver();

        cur = resolver.query(CalendarContract.Calendars.CONTENT_URI, calendarProjectionArray, null ,null, null);

        if(cur.getCount() > 0) {
            while(cur.moveToNext()) {
                System.out.println("ID: " + cur.getInt(0));
                System.out.println("Display Name: " + cur.getString(1));
            }
        }
        cur.close();

    }
}
