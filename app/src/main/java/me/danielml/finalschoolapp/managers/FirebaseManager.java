package me.danielml.finalschoolapp.managers;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import me.danielml.finalschoolapp.objects.Subject;
import me.danielml.finalschoolapp.objects.Test;
import me.danielml.finalschoolapp.objects.TestType;

public class FirebaseManager {

    private final FirebaseDatabase database;

    public FirebaseManager() {
        this.database = FirebaseDatabase.getInstance();
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
}
