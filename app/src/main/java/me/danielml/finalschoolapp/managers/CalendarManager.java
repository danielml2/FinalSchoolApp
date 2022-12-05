package me.danielml.finalschoolapp.managers;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import me.danielml.finalschoolapp.objects.Test;

public class CalendarManager {

    private HashMap<String, Long> availableCalendarIDs;

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
                availableCalendarIDs.put(cur.getString(1), cur.getLong(0));
            }
        }
        cur.close();

    }

    public HashMap<String, Long> syncCalendarExport(List<Test> tests, Context context, String calName, HashMap<String, Long> savedEventIDs) {

        long calID = availableCalendarIDs.get(calName);

        List<String> testKeys = tests.stream().map(this::getEventIDForTest).collect(Collectors.toList());

        List<Test> additions = tests
                .stream()
                .filter(test -> !savedEventIDs.containsKey(getEventIDForTest(test)))
                .collect(Collectors.toList());
        List<String> removals = savedEventIDs.keySet()
                .stream()
                .filter(testKey -> !testKeys.contains(testKey))
                .collect(Collectors.toList());


        HashMap<String, Long> testIDtoEventID = new HashMap<>();

        // Remove the event from the calendar and also remove it from the saved list
        removals.forEach(key -> {
            Log.d("SchoolTests", "Removing test with key: " + key);
            removeEvent(savedEventIDs.get(key), calID, context);
            savedEventIDs.remove(key);
        });

        additions.forEach(test -> {
            long eventID = addEvent(test, calID, context);
            Log.d("SchoolTests", "Added new test to the calendar: " + getEventIDForTest(test) + " wtih new event ID: " + eventID);
            testIDtoEventID.put(getEventIDForTest(test), eventID);
        });
        Log.d("SchoolTests", "Applied " + (removals.size() + additions.size()) + " changes to the calendar.");
        testIDtoEventID.putAll(savedEventIDs);


        return testIDtoEventID;
    }

    public long addEvent(Test test, long calID, Context context) {
        ContentResolver resolver = context.getContentResolver();

        System.out.println(test.getType().getName() + " " + test.getSubject().getDefaultName());
        Uri uri = resolver.insert(CalendarContract.Events.CONTENT_URI, toEventValues(test, calID));

        long eventID = Long.parseLong(uri.getLastPathSegment());

        Log.d("SchoolTests Calendar", "Added new event with ID: " + eventID);
        return eventID;
    }

    public void removeEvent(long eventID, long calID, Context context) {
        ContentResolver resolver = context.getContentResolver();

        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
        int deletedRowsCount = resolver.delete(deleteUri, null, null);

        Log.e("SchoolTests", "Deleted event with ID: " + eventID + " in calID" + calID + ", with " + deletedRowsCount + " deleted rows.");
    }

    public ContentValues toEventValues(Test test, long calID) {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.DTSTART, test.getDueDate());
        values.put(CalendarContract.Events.DTEND, test.getDueDate() + 86400000);
        values.put(CalendarContract.Events.DESCRIPTION, "כיתות: "+ Arrays.toString(test.getClassNums().toArray()).replace("[","").replace("]","").replace("-1","שכבתי"));
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        values.put(CalendarContract.Events.TITLE, test.getType().getName() + " " + test.getSubject().getDefaultName());
        return values;
    }

    private String getEventIDForTest(Test test) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        return test.getSubject().name().toLowerCase() + "_" + test.getType().name().toLowerCase() + "_" + dateFormat.format(test.getDueDate()) + "_" + test.getGradeNum();
    }

    public String getNameFromID(long calID) {
        Optional<Map.Entry<String, Long>> optional =
                availableCalendarIDs.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue() == calID)
                        .findFirst();

        return optional.map(Map.Entry::getKey).orElse(null);
    }

    public long getIDFromName(String calName) {
       return availableCalendarIDs.getOrDefault(calName, -1L);
    }

}
