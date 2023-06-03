package me.danielml.finalschoolapp.managers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import me.danielml.finalschoolapp.objects.Subject;
import me.danielml.finalschoolapp.objects.Test;
import me.danielml.finalschoolapp.objects.TestType;

public class FileManager {

    private final File internalSaveLocation;
    public FileManager(File internalSaveLocation) {
        this.internalSaveLocation = internalSaveLocation;
    }

    /**
     * Gets the locally saved last updated time for the local data (From data.json)
     * @return the locally saved last updated time for the data in Unix Time (milliseconds since 1970 on UTC time), If it doesn't exist or fails returns -1.
     * @throws IOException If the file doesn't exist / fails to be read
     * @throws JSONException If the file isn't able to be parsed correctly.
     */
    public long getLocalLastUpdated() throws IOException, JSONException {
        JSONObject dataJSON = getJSONObject("data");
        if(dataJSON.has("last_updated"))
            return dataJSON.getLong("last_updated");
        else
            return -1;
    }

    /**
     * Saves the last time the background sync service has checked the database for updates (Saves to preferences.json)
     * @param lastCheck The last check time in Unix Time (milliseconds since 1970 on UTC time)
     * @throws JSONException If parsing the existing file fails
     * @throws IOException If the write operation fails
     */
    public void saveLastCheck(long lastCheck) throws JSONException, IOException {
        JSONObject object = getJSONObject("preferences");
        object.put("service_last_checked", lastCheck);

       writeJSON("preferences", object);
    }

    /**
     * Gets the last time the background sync service has checked the database for updates. (from preferences.json)
     * @return The last time checked, in Unix Time (milliseconds since 1970 on UTC time), If it isn't saved or fails, returns the current system time.
     * @throws FileNotFoundException If it tries to read preferences.json when it doesn't exist
     * @throws JSONException If the JSON parser fails to parse preferences.json
     */
    public long getLastCheck() throws FileNotFoundException, JSONException {
        JSONObject object = getJSONObject("preferences");
        return object.has("service_last_checked") ? object.getLong("service_last_checked") : System.currentTimeMillis();
    }

    /**
     * Saves the test database data locally (Last updated time, list of tests) (Saved to data.json)
     * @param lastUpdated Last time database was updated in Unix Time (milliseconds since 1970 on UTC time)
     * @param tests The list of tests currently at the database
     * @throws JSONException If any parsing of the JSON on the read/write fails
     * @throws IOException If getting the current data.json file fails.
     */
    public void saveDBDataLocally(long lastUpdated, List<Test> tests) throws JSONException, IOException {
        JSONObject object = getJSONObject("data");

        object.put("last_updated", lastUpdated);

        JSONArray testsArray = new JSONArray();
        tests.forEach(test -> testsArray.put(test.toJSON()));
        object.put("tests", testsArray);


        Log.d("FileManager", "SAVE:" + object.toString());
        writeJSON("data", object);
    }

    /**
     * Gets the list of tests saved locally. (from data.json)
     * @return Gets the list of tests, if the list doesn't exist, returns an empty list.
     * @throws FileNotFoundException If reading the file fails
     * @throws JSONException If parsing the JSON from the file fails.
     */
    public List<Test> getLocalTests() throws FileNotFoundException, JSONException {
        JSONObject object = getJSONObject("data");

        if(!object.has("tests"))
            return new ArrayList<>();

        JSONArray jsonTests = object.getJSONArray("tests");
        List<Test> tests = new ArrayList<>();
        for(int i = 0; i < jsonTests.length(); i++) {
            JSONObject testObject = (JSONObject) jsonTests.get(i);
            tests.add(fromJSON(testObject));
        }
        return tests;
    }

    /**
     * Writes the JSONObject to a given JSON file in the internal save location
     * @param fileName File to be saved (without file extension)
     * @param jsonObject The json object to be saved
     * @throws IOException If writing to the file fails.
     */
    public void writeJSON(String fileName, JSONObject jsonObject) throws IOException {
        File file = new File(internalSaveLocation, fileName + ".json");
        if(!file.exists()) file.createNewFile();

        PrintWriter writer = new PrintWriter(file);
        writer.write(jsonObject.toString());
        writer.flush();
    }

    /**
     * Gets the JSONObject from a given JSON file in the interal save location
     * @param fileName File to load (without file extension)
     * @return The JSONObject from the file, if the file exists otherwise returns an empty JSONObject.
     * @throws FileNotFoundException If the file doesn't exist
     * @throws JSONException If parsing the JSON from the file fails.
     */
    public JSONObject getJSONObject(String fileName) throws FileNotFoundException, JSONException {
        File file = new File(internalSaveLocation, fileName+ ".json");
        if(!file.exists()) {
            Log.d("SchoolTests", fileName + "JSON File doesn't exist!");
            return new JSONObject();
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String jsonString = reader.lines().collect(Collectors.joining());

        return new JSONObject(jsonString);
    }

    /**
     * Creates a test from a given JSONObject json.
     * @param json The JSONObject that represents the test
     * @return The test from the JSON data.
     * @throws JSONException If parsing any of the JSONObject to types fails.
     */
    public Test fromJSON(JSONObject json) throws JSONException {

        ArrayList<Integer> classNums = new ArrayList<>();
        JSONArray classNumJSON = json.getJSONArray("classNums");
        for(int i = 0; i < classNumJSON.length(); i++) {
            classNums.add((Integer) classNumJSON.get(i));
        }

        return new Test(Subject.from(json.getString("subject")),
                            json.getLong("dueDate"),
                            TestType.from(json.getString("testType")),
                            json.getInt("gradeNum"),
                            classNums,
                            json.getString("creationText"));
    }

    /**
     * Saves the test ID to calendar event IDs map to calendar_events.json
     * @param testIDsToEventIDs The test id to calendar event id map to save.
     * @throws IOException If writing to the file fails.
     */
    public void saveEventIDs(HashMap<String, Long> testIDsToEventIDs) throws IOException {

        JSONObject jsonObject = new JSONObject();

        testIDsToEventIDs.forEach((key, value) -> {
            try {
                jsonObject.put(key, value);
            } catch (JSONException e) {
                Log.e("SchoolTests", "Failed putting " + key + " into the JSON! (Event ID: " + value + ")");
                e.printStackTrace();
            }
        });
        writeJSON("calendar_events", jsonObject);
    }

    /**
     * Gets the map of test ID to calendar event IDs from calendar_events.json
     * @return The test id to calendar event id map, unless it fails or doesn't exist which then returns an empty map.
     * @throws FileNotFoundException If the file doesn't exist
     * @throws JSONException If parsing the JSON file fails.
     */
    public HashMap<String, Long> getEventIDs() throws FileNotFoundException, JSONException {
        JSONObject object = getJSONObject("calendar_events");

        if(object.length() < 1) {
            Log.e("SchoolTests", "Failed loading back event IDs! File deleted or corrupted?");
            return new HashMap<>();
        }

        HashMap<String, Long> testIDsToEventIDs = new HashMap<>();
        object.keys().forEachRemaining(testKey -> {
            try {
                testIDsToEventIDs.put(testKey, object.getLong(testKey));
            } catch (JSONException e) {
                Log.e("SchoolTests", "Failed loading event ID from JSON (Test key: " + testKey);
                e.printStackTrace();
            }
        });

        return testIDsToEventIDs;
    }

    /**
     * Save the selected calendar ID to preferences.json
     * @param calID The Calendar ID
     * @throws IOException If writing to the file fails
     * @throws JSONException If parsing preferences.json fails.
     */
    public void saveCalendarID(long calID) throws IOException, JSONException {
        JSONObject object = new JSONObject().put("calID", calID);
        writeJSON("preferences", object);
    }

    /**
     * Gets the calendar ID from preferences.json
     * @return The calendar ID if it succeeds, -1 if it fails.
     * @throws FileNotFoundException If the file doesn't exist
     * @throws JSONException If parsing the file fails.
     */
    public long getCalendarID() throws FileNotFoundException, JSONException {
        JSONObject obj = getJSONObject("preferences");
        return obj.has("calID") ? obj.getLong("calID") : -1;
    }

    /**
     * Gets the setting for auto syncing the calendar in the background (from preferences.json)
     * @return The setting saved, if it fails loading it defaults to false.
     */
    public boolean isAutoSyncingCalendar() {
        try {
            JSONObject obj = getJSONObject("preferences");
            if(!obj.has("calendarAutoSync"))
                return false;
            return obj.getBoolean("calendarAutoSync");
        } catch (IOException | JSONException exception) {
            return false;
        }
    }

    /**
     * Saves the calendar auto sync in the background setting to preferences.json
     * @param calendarAutoSync The background auto sync setting to save.
     */
    public void saveCalendarAutoSync(boolean calendarAutoSync)  {
        try {
            JSONObject obj = getJSONObject("preferences");
            obj.put("calendarAutoSync", calendarAutoSync);
            writeJSON("preferences", obj);
        } catch (IOException | JSONException exception) {
            Log.e("SchoolTests", "Saving calendar auto sync setting failed!");
            exception.printStackTrace();
        }
    }

    /**
     * Gets the setting for should the background sync service in the background or not.
     * @return The setting value, defaults to false if fails.
     */
    public boolean isSyncServiceEnabled() {
        try {
            JSONObject obj = getJSONObject("preferences");
            if(!obj.has("syncService"))
                return false;
            return obj.getBoolean("syncService");
        } catch (IOException | JSONException exception) {
            return false;
        }
    }

    /**
     * Saves the setting for should the background sync service run in the background or not.
     * @param syncServiceEnabled The setting value.
     */
    public void saveSyncService(boolean syncServiceEnabled) {
        try {
            JSONObject obj = getJSONObject("preferences");
            obj.put("syncService", syncServiceEnabled);
            writeJSON("preferences", obj);
        } catch (IOException | JSONException exception) {
            Log.e("SchoolTests", "Saving sync service setting failed!");
            exception.printStackTrace();
        }
    }

}
