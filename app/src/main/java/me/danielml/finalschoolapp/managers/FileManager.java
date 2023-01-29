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

    public long getLocalLastUpdated() throws IOException, JSONException {
        JSONObject dataJSON = getJSONObject("data");
        if(dataJSON.has("last_updated"))
            return dataJSON.getLong("last_updated");
        else
            return -1;
    }

    public void saveLastCheck(long lastCheck) throws JSONException, IOException {
        JSONObject object = new JSONObject();
        object.put("last_checked", lastCheck);

       writeJSON("syncSettings", object);
    }

    public long getLastCheck() throws FileNotFoundException, JSONException {
        JSONObject object = getJSONObject("syncSettings");
        return object.has("last_checked") ? object.getLong("last_checked") : System.currentTimeMillis();
    }

    public void saveDBDataLocally(long lastUpdated, List<Test> tests) throws JSONException, IOException {
        JSONObject object = getJSONObject("data");

        object.put("last_updated", lastUpdated);

        JSONArray testsArray = new JSONArray();
        tests.forEach(test -> testsArray.put(test.toJSON()));
        object.put("tests", testsArray);


        Log.d("FileManager", "SAVE:" + object.toString());
        writeJSON("data", object);
    }

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

    public void writeJSON(String fileName, JSONObject jsonObject) throws IOException {
        File file = new File(internalSaveLocation, fileName + ".json");
        if(!file.exists()) file.createNewFile();

        PrintWriter writer = new PrintWriter(file);
        writer.write(jsonObject.toString());
        writer.flush();
    }


    public JSONObject getJSONObject(String fileName) throws FileNotFoundException, JSONException {
        File file = new File(internalSaveLocation, fileName+ ".json");
        if(!file.exists()) {
            return new JSONObject();
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String jsonString = reader.lines().collect(Collectors.joining());

        return new JSONObject(jsonString);
    }

    public Test fromJSON(JSONObject json) throws JSONException {

        ArrayList<Integer> classNums = new ArrayList<>();
        JSONArray classNumJSON = json.getJSONArray("classNums");
        for(int i = 0; i < classNumJSON.length(); i++) {
            classNums.add((Integer) classNumJSON.get(i));
        }

        Test test = new Test(Subject.from(json.getString("subject")),
                            json.getLong("dueDate"),
                            TestType.from(json.getString("testType")),
                            json.getInt("gradeNum"),
                            classNums);

        test.setManuallyCreated(json.getBoolean("manuallyCreated"));
        test.setCreationText(json.getString("creationText"));

        return test;
    }

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

    public void saveCalendarID(long calID) throws IOException, JSONException {
        JSONObject object = new JSONObject().put("calID", calID);
        writeJSON("preferences", object);
    }

    public long getCalendarID() throws FileNotFoundException, JSONException {
        JSONObject obj = getJSONObject("preferences");
        return obj.has("calID") ? obj.getLong("calID") : -1;
    }

    public boolean isAutoSyncingCalendar() {
        try {
            JSONObject obj = getJSONObject("preferences");
            return obj.has("calendarAutoSync") && obj.getBoolean("calendarAutoSync");
        } catch (IOException | JSONException exception) {
            return false;
        }
    }

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

    public boolean isSyncServiceEnabled() {
        try {
            JSONObject obj = getJSONObject("preferences");
            return obj.has("syncService") && obj.getBoolean("syncService");
        } catch (IOException | JSONException exception) {
            return false;
        }
    }

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
