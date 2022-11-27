package me.danielml.finalschoolapp.managers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;

import java.util.HashMap;
import java.util.Set;

public class CalendarManager {

    private HashMap<String, Integer> availableCalendarIDs;

    private final String[] calendarProjectionArray = {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
    };

    public CalendarManager() {
        this.availableCalendarIDs = new HashMap<>();
    }

    public Set<String> availableCalendarNames() {
        return availableCalendarIDs.keySet();
    }

    public void loadAvaliableCalendarIDs(Context context) {


        Cursor cur = null;
        ContentResolver resolver = context.getContentResolver();

        cur = resolver.query(CalendarContract.Calendars.CONTENT_URI, calendarProjectionArray, null ,null, null);

        if(cur.getCount() > 0) {
            while(cur.moveToNext()) {
                availableCalendarIDs.put(cur.getString(1), cur.getInt(0));
            }
        }
        cur.close();

    }

}
