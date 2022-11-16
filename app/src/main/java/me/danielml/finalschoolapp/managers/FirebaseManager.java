package me.danielml.finalschoolapp.managers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import me.danielml.finalschoolapp.objects.Subject;
import me.danielml.finalschoolapp.objects.Test;
import me.danielml.finalschoolapp.objects.TestType;

public class FirebaseManager {

    private final FirebaseDatabase database;
    private final FirebaseFirestore firestore;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");


    public FirebaseManager() {
        this.database = FirebaseDatabase.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void getLastUpdatedTime(Consumer<Long> callback) {
        long time = System.currentTimeMillis();
        database.getReference().child("last_update").get().addOnCompleteListener((dataSnapshotTask) -> {
            if(dataSnapshotTask.isSuccessful())
            {
                long lastUpdatedDB = dataSnapshotTask.getResult().getValue(Long.class);
                Log.d("FirebaseManager", "Got last updated successfully");

                Log.d("FirebaseManager", "Took " + (System.currentTimeMillis() - time) + "ms");
                callback.accept( lastUpdatedDB);
            } else {
                callback.accept(0L);
                Log.e("FirebaseManager", "Couldn't get last updated value!");
            }

        });
    }

    public void getCurrentTests(Consumer<List<Test>> testCallback) {
        DatabaseReference ref = database.getReference()
                .child("years")
                .child("2022-2023")
                .child("tests")
                .getRef();

        ref.get().addOnCompleteListener((dataSnapshotTask) -> {
            List<Test> tests = new ArrayList<>();
            if(dataSnapshotTask.isSuccessful()) {
                dataSnapshotTask.getResult().getChildren().forEach((gradeSnapshot) -> {

                    gradeSnapshot.getChildren().forEach(testSnapshot -> {

                        ArrayList<Integer> classNums = testSnapshot.child("classNums").getValue(new GenericTypeIndicator<ArrayList<Integer>>() {});
                        Subject subject = Subject.from(testSnapshot.child("subject").getValue(String.class));
                        TestType type = TestType.from(testSnapshot.child("type").getValue(String.class));
                        int gradeNum = testSnapshot.child("gradeNum").getValue(Integer.class);
                        long dueDate = testSnapshot.child("dueDate").getValue(Long.class);
                        boolean manuallyCreated = testSnapshot.child("manuallyCreated").getValue(Boolean.class) != null && testSnapshot.child("manuallyCreated").getValue(Boolean.class);
                        String creationText = testSnapshot.child("creationText").getValue(String.class);

                        Test test = new Test(subject,dueDate,type,gradeNum,classNums);
                        test.setManuallyCreated(manuallyCreated);
                        test.setCreationText(creationText);

                        tests.add(test);
                    });

                });
                testCallback.accept(tests);
            }
        });
    }

    public void addReport(Test test, String issueType, String issueDetails, Consumer<DocumentState> stateManager) {

        HashMap<String, Object> reportData = new HashMap<>();
        reportData.put("testID", getDatabaseIdFromTest(test));
        reportData.put("issueType", issueType);
        reportData.put("issueDetails", issueDetails);
        reportData.put("timestamp", new Date().getTime());
        reportData.put("grade", test.getGradeNum());

        firestore
                .collection("reports")
                .document()
                .set(reportData)
                .addOnSuccessListener(nothing -> {
                    Log.d("FirebaseManager", "Successfully added the report to the database!");
                    stateManager.accept(DocumentState.FINISHED);
                })
                .addOnFailureListener(exception -> {
                    Log.e("FirebaseManager", "Failed to add report to database, caused by: " + exception.getCause());
                    exception.printStackTrace();
                    stateManager.accept(DocumentState.FAILED);
                });
        stateManager.accept(DocumentState.STARTED);
    }

    private String getDatabaseIdFromTest(Test test) {
        return test.getSubject().name().toLowerCase() + "_" + test.getType().name().toLowerCase() + "_" + dateFormat.format(test.getDueDate());
    }
}
