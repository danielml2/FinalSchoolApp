package me.danielml.finalschoolapp.objects;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Test {

    private final Subject subject;
    private final int gradeNum;
    private final List<Integer> classNums;
    private final long dueDate;
    private final TestType type;

    private String creationText;

    private boolean manuallyCreated;

    public Test(Subject subject, long dueDate, TestType type, int gradeNum, ArrayList<Integer> classNums) {
        this.subject = subject;
        this.dueDate = dueDate;
        this.type = type;
        this.gradeNum = gradeNum;
        this.creationText = "NONE";
        this.manuallyCreated = false;
        this.classNums = classNums;
    }

    public void addClassNum(int classNum) {
        this.classNums.add(classNum);
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

    public void setCreationText(String creationText) {
        this.creationText = creationText;
    }

    public String getCreationText() {
        return creationText;
    }

    public boolean isManuallyCreated() {
        return manuallyCreated;
    }

    public void setManuallyCreated(boolean manuallyCreated) {
        this.manuallyCreated = manuallyCreated;
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

    public JSONObject toJSON()  {
        try {
            return new JSONObject()
                     .put("dueDate", dueDate)
                     .put("gradeNum", gradeNum)
                     .put("subject", subject.name())
                     .put("testType", type.name())
                     .put("classNums", classNumsJSON())
                     .put("manuallyCreated", manuallyCreated)
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


