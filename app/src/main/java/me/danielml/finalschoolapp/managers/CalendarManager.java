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
import java.util.concurrent.TimeUnit;
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

    /**
     * Loads all the available calendars on the device to a map of its name and its calendar id
     * @param context context to get the content resolver from.
     */
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

    /**
     * Checks if a calendar ID exists in the device
     * @param calID Calendar ID
     * @param context Context to load the calendar ids if it wasn't loaded yet
     * @return If the calendar exists.
     */
    public boolean doesCalendarExist(long calID, Context context) {
        if(availableCalendarIDs.isEmpty())
            loadAvaliableCalendarIDs(context);

        return availableCalendarIDs.containsValue(calID);
    }

    /**
     * Syncs the calendar with the current list of tests at that time
     * @param tests The complete list of tests
     * @param context Activity's context to use the content resolver.
     * @param calName The calendar's name
     * @param savedEventIDs The map of test IDs to the IDs of the calendar events representing them.
     * @return A new updated map of all the tests IDs and calendar event IDs currently in the calendar.
     */
    public HashMap<String, Long> syncCalendarExport(List<Test> tests, Context context, String calName, HashMap<String, Long> savedEventIDs) {
        long calID = availableCalendarIDs.get(calName);
        return syncCalendarExport(tests, context, calID, savedEventIDs);
    }

    /**
     * Syncs the calendar with the current list of tests at that time
     * @param tests The complete list of tests
     * @param context Activity's context to use the content resolver.
     * @param calID The calendar's ID
     * @param savedEventIDs The map of test IDs to the IDs of the calendar events representing them.
     * @return A new updated map of all the tests IDs and calendar event IDs currently in the calendar.
     */
    public HashMap<String, Long> syncCalendarExport(List<Test> tests, Context context, long calID, HashMap<String, Long> savedEventIDs) {

        List<String> testKeys = tests.stream().map(this::getEventJSONName).collect(Collectors.toList());

        List<Test> additions = tests
                .stream()
                .filter(test -> !savedEventIDs.containsKey(getEventJSONName(test)))
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
            Log.d("SchoolTests", "Added new test to the calendar: " + getEventJSONName(test) + " wtih new event ID: " + eventID);
            testIDtoEventID.put(getEventJSONName(test), eventID);
        });
        Log.d("SchoolTests", "Applied " + (additions.size() + removals.size()) + " changes to the calendar. (" + additions.size() + " Additions, " + removals.size() + " Removals)");
        testIDtoEventID.putAll(savedEventIDs);


        return testIDtoEventID;
    }

    /**
     * Adds an event representing the test to the specified calendar.
     * @param test Test to be added as an event
     * @param calID Calendar ID to add the event to.
     * @param context Context so we can access to the content resolver.
     * @return The new event ID for the event added for the test.
     */
    public long addEvent(Test test, long calID, Context context) {
        ContentResolver resolver = context.getContentResolver();

        System.out.println(test.getType().getName() + " " + test.getSubject().getDefaultName());
        Uri uri = resolver.insert(CalendarContract.Events.CONTENT_URI, toEventValues(test, calID));

        long eventID = Long.parseLong(uri.getLastPathSegment());

        Log.d("SchoolTests Calendar", "Added new event with ID: " + eventID);
        return eventID;
    }

    /**
     * Removes an event representing the test to the specified calendar.
     * @param eventID event to remove
     * @param calID The calendar the event is on.
     * @param context Context so we can access to the content resolver.
     */
    public void removeEvent(long eventID, long calID, Context context) {
        ContentResolver resolver = context.getContentResolver();

        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
        int deletedRowsCount = resolver.delete(deleteUri, null, null);

        Log.e("SchoolTests", "Deleted event with ID: " + eventID + " in calID" + calID + ", with " + deletedRowsCount + " deleted rows.");
    }

    /**
     * Maps an test to values for a calendar event.
     * @param test The test to be mapped
     * @param calID The calendar ID for the event
     * @return The ContentValues object containing the event details.
     */
    public ContentValues toEventValues(Test test, long calID) {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        // I have no idea why is it like this, but otherwise it just puts all the events one day earlier then it should be
        values.put(CalendarContract.Events.DTSTART, test.getDueDate() + TimeUnit.DAYS.toMillis(1));
        values.put(CalendarContract.Events.ALL_DAY, 1);
        values.put(CalendarContract.Events.DTEND, test.getDueDate() + TimeUnit.DAYS.toMillis(2));
        values.put(CalendarContract.Events.DESCRIPTION, "כיתות: "+ Arrays.toString(test.getClassNums().toArray()).replace("[","").replace("]","").replace("-1","שכבתי"));
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        values.put(CalendarContract.Events.TITLE, test.getType().getName() + " " + test.getSubject().getDefaultName());
        return values;
    }

    /**
     * Gets the name for a test in the test IDs to event IDs JSON.
     * @param test Test to have the name generated for
     * @return The string representing the test's ID in the JSON.
     */
    public String getEventJSONName(Test test) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        return test.getSubject().name().toLowerCase() + "_" + test.getType().name().toLowerCase() + "_" + dateFormat.format(test.getDueDate()) + "_" + test.getGradeNum();
    }

    /**
     * Gets the a calendar's name from its calendar ID
     * @param calID The calendar's ID
     * @return The string representing the calendar name if it exists, if not returns null.
     */
    public String getNameFromID(long calID) {
        Optional<Map.Entry<String, Long>> optional =
                availableCalendarIDs.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue() == calID)
                        .findFirst();

        return optional.map(Map.Entry::getKey).orElse(null);
    }

    /**
     * Gets the calendar's ID from its name.
     * @param calName Calendar's Name
     * @return The calendar ID representing it, if it doesn't exist returns -1.
     */
    public long getIDFromName(String calName) {
       return availableCalendarIDs.getOrDefault(calName, -1L);
    }

}
