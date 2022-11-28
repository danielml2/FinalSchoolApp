package me.danielml.finalschoolapp.managers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;

import me.danielml.finalschoolapp.objects.Test;

public class CalendarManager {

    private HashMap<String, Integer> availableCalendarIDs;

    private final String[] calendarProjectionArray = {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
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

    public void addEvent(Context context, Test test, String calName) {
        ContentResolver resolver = context.getContentResolver();

        long calID = availableCalendarIDs.get(calName);

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.DTSTART, test.getDueDate());
        values.put(CalendarContract.Events.DTEND, test.getDueDate() + 86400000);
        values.put(CalendarContract.Events.DESCRIPTION, "כיתות: "+ Arrays.toString(test.getClassNums().toArray()).replace("[","").replace("]","").replace("-1","שכבתי"));
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        values.put(CalendarContract.Events.TITLE, test.getType().getName() + " " + test.getSubject().getDefaultName());

        System.out.println(test.getType().getName() + " " + test.getSubject().getDefaultName());
        Uri uri = resolver.insert(CalendarContract.Events.CONTENT_URI, values);


        long eventID = Long.parseLong(uri.getLastPathSegment());


        Log.d("SchoolTests Calendar", "Added new event with ID: " + eventID);
    }

}
