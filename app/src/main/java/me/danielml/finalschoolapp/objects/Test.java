package me.danielml.finalschoolapp.objects;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Test implements Serializable {

    private final Subject subject;
    private final int gradeNum;
    private final List<Integer> classNums;
    private final long dueDate;
    private final TestType type;

    private String creationText;


    public Test(Subject subject, long dueDate, TestType type, int gradeNum, ArrayList<Integer> classNums, String creationText) {
        this.subject = subject;
        this.dueDate = dueDate;
        this.type = type;
        this.gradeNum = gradeNum;
        this.creationText = creationText;
        this.classNums = classNums;
    }

    public Subject getSubject() {
        return subject;
    }

    public long getDueDate() {
        return dueDate;
    }

    public Date asDate() { return new Date(dueDate); }

    public String getDateFormatted() {
        return new SimpleDateFormat("yyyy-MM-dd").format(asDate());
    }

    public TestType getType() {
        return type;
    }

    public List<Integer> getClassNums() {
        return classNums;
    }

    public int getGradeNum() {
        return gradeNum;
    }
    
    public String getCreationText() {
        return creationText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Test test = (Test) o;
        return subject == test.subject && dueDate == test.dueDate && type == test.type;
    }

    @Override
    public String toString() {
        return "(Grade " + gradeNum + "): " + subject + " " + type + " at " + getDateFormatted() + " for " + classNums;
    }

    /**
     * Maps the test to a JSONObject.
     * @return The JSONObject with the test's values in it.
     */
    public JSONObject toJSON()  {
        try {
            return new JSONObject()
                     .put("dueDate", dueDate)
                     .put("gradeNum", gradeNum)
                     .put("subject", subject.name())
                     .put("testType", type.name())
                     .put("classNums", classNumsJSON())
                     .put("creationText", creationText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONArray classNumsJSON() {
        JSONArray array = new JSONArray();
        classNums.forEach(array::put);
        return array;
    }

}


