package me.danielml.finalschoolapp.managers;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
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

import me.danielml.finalschoolapp.objects.FilterProfile;
import me.danielml.finalschoolapp.objects.Subject;
import me.danielml.finalschoolapp.objects.Test;
import me.danielml.finalschoolapp.objects.TestType;

public class FirebaseManager {

    private final FirebaseDatabase database;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth authentication;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");


    public FirebaseManager() {
        this.database = FirebaseDatabase.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.authentication = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public void getUserFilterProfile(Consumer<FilterProfile> profileCallback) {
        if(!isSignedIn())
            profileCallback.accept(FilterProfile.NULL_FALLBACK);
        String userID = authentication.getCurrentUser().getUid();

        database.getReference()
                .child("profiles")
                .child(userID)
                .get()
                .addOnCompleteListener((task) -> {
                    if(task.getResult().getValue() != null) {
                        DataSnapshot profileSnapshot = task.getResult();


                        int classNum = profileSnapshot.child("classNum").getValue(Integer.class);
                        int gradeNum = profileSnapshot.child("gradeNum").getValue(Integer.class);

                        Subject majorASubject = null, majorBSubject = null;
                        if(gradeNum >= 10) {

                            majorASubject = Subject.from(profileSnapshot.child("majorA").getValue(String.class));
                            majorBSubject = Subject.from(profileSnapshot.child("majorB").getValue(String.class));
                        }

                        FilterProfile filterProfile = new FilterProfile(classNum, gradeNum, majorASubject, majorBSubject);
                        profileCallback.accept(filterProfile);
                    } else {
                        profileCallback.accept(null);
                    }
                });
    }

    public void setUserFilterProfile(FilterProfile filterProfile) {
        String userID = authentication.getCurrentUser().getUid();

        database.getReference()
                .child("profiles")
                .child(userID)
                .setValue(filterProfile)
                .addOnCompleteListener((task) -> {
                    System.out.println("User profile task: " + task.isSuccessful());
                });
    }

    public void getLastUpdatedTime(Consumer<Long> callback) {
        long time = System.currentTimeMillis();
        database.getReference().child("last_update").get().addOnCompleteListener((dataSnapshotTask) -> {
            if(dataSnapshotTask.isSuccessful())
            {
                long lastUpdatedDB = dataSnapshotTask.getResult().getValue(Long.class);
                Log.d("FirebaseManager", "Got last updated successfully");

                Log.d("FirebaseManager", "Took " + (System.currentTimeMillis() - time) + "ms");
                callback.accept(lastUpdatedDB);
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

    public boolean isSignedIn() {
        return authentication.getCurrentUser() != null;
    }

    public void signUp(String email, String password, Consumer<FirebaseUser> onSignIn, Consumer<Exception> onFailedSignIn) {
        authentication.createUserWithEmailAndPassword(email, password).addOnCompleteListener((task -> {
            if(task.isSuccessful()) {
                onSignIn.accept(authentication.getCurrentUser());
            } else {
                onFailedSignIn.accept(task.getException());
            }
        }));
    }

    public void signIn(String email, String password, Consumer<FirebaseUser> onSignIn, Consumer<Exception> onFailedSignIn) {
        authentication.signInWithEmailAndPassword(email, password).addOnCompleteListener((task -> {
            if(task.isSuccessful()) {
                onSignIn.accept(authentication.getCurrentUser());
            } else {
                onFailedSignIn.accept(task.getException());
            }
        }));
    }

    public void signOut() {
        authentication.signOut();
    }

    private String getDatabaseIdFromTest(Test test) {
        return test.getSubject().name().toLowerCase() + "_" + test.getType().name().toLowerCase() + "_" + dateFormat.format(test.getDueDate());
    }
}
