package me.danielml.finalschoolapp.managers;

import android.util.Log;
import android.widget.ArrayAdapter;

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
}
