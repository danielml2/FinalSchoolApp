package me.danielml.finalschoolapp.managers;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firestore.v1.RunAggregationQueryRequest;

import java.io.ByteArrayOutputStream;
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
    private final FirebaseStorage storage;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");


    public FirebaseManager() {
        this.database = FirebaseDatabase.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.authentication = FirebaseAuth.getInstance();
    }

    /**
     * Gets the current user logged in (If there is one)
     * @return The FirebaseUser object representing the user.
     */
    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Gets the current user's filter profile from the database.
     * @param profileCallback Callback for when the database fetching runs, returns the user's profile if found,
     *                        if it's null or the user isn't logged in, returns null or the fallback profile.
     */
    public void getUserFilterProfile(Consumer<FilterProfile> profileCallback) {
        if(!isSignedIn())
            profileCallback.accept(FilterProfile.NULL_FALLBACK);
        String userID = authentication.getCurrentUser().getUid();

        database.getReference()
                .child("profiles")
                .child(userID)
                .child("filters")
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

    /**
     * Sets the user filter profile in the database for the current user
     * @param filterProfile Filter profile to set.
     */
    public void setUserFilterProfile(FilterProfile filterProfile) {
        String userID = authentication.getCurrentUser().getUid();

        database.getReference()
                .child("profiles")
                .child(userID)
                .child("filters")
                .setValue(filterProfile)
                .addOnCompleteListener((task) -> {
                    System.out.println("User profile task: " + task.isSuccessful());
                });
    }

    /**
     * Gets the last updated time from the database.
     * @param callback Callback to send the data to when fetching finishes.
     *                 Returns 0 if it doesn't find it, otherwise the value in the database is returned.
     */
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

    /**
     * Gets the current list of tests in the database.
     * @param testCallback The callback to call when fetching ends.
     *                     If it fails, returns an empty list.
     */
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
                        String creationText = testSnapshot.child("creationText").getValue(String.class);

                        Test test = new Test(subject,dueDate,type,gradeNum,classNums, creationText);
                        tests.add(test);
                    });

                });
                testCallback.accept(tests);
            }
        });
    }

    /**
     * Adds a report for a given test to the database. with a specific issue type and details.
     * @param test The test to be reported
     * @param issueType The issue with the test
     * @param issueDetails More details about the issue
     * @param successCallback Callback if the report was added successfully
     * @param failedCallback Callback if the report wasn't added successfully
     */
    public void addReport(Test test, String issueType, String issueDetails, Runnable successCallback, Runnable failedCallback) {

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
                    successCallback.run();
                })
                .addOnFailureListener(exception -> {
                    Log.e("FirebaseManager", "Failed to add report to database, caused by: " + exception.getCause());
                    exception.printStackTrace();
                    failedCallback.run();
                });
    }

    /**
     * Gets the current user's profile picture's URL in the storage bucket
     * @param profileURICallback Callback for when fetching the profile picture ends.
     */
    public void getProfilePictureForCurrentUser(Consumer<Uri> profileURICallback) {
        String userID = getCurrentUser().getUid();

        database.getReference()
                .child("profiles")
                .child(userID)
                .child("pfpURL")
                .get()
                .addOnCompleteListener((task -> {
                    if(task.isSuccessful()) {
                        if(task.getResult().exists()) {
                            String url = task.getResult().getValue(String.class);
                            profileURICallback.accept(Uri.parse(url));
                        }
                        else
                            profileURICallback.accept(null);
                    }
                    else
                        profileURICallback.accept(null);
                }));
    }

    /**
     * Uploads a given profile picture to the firebase storage bucket.
     * @param photoBitmap The bitmap of the photo
     * @param successfulUploadCallback Callback if the profile picture succeeds in uploading
     * @param failedUpload Callback if the upload fails
     */
    public void uploadImageForCurrentUser(Bitmap photoBitmap, Consumer<Uri> successfulUploadCallback, Consumer<String> failedUpload) {
        if(photoBitmap == null)
            return;

        String userID = getCurrentUser().getUid();

        ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, compressedStream);
        byte[] imageData = compressedStream.toByteArray();

        StorageReference reference = storage.getReference();

        reference.child("userPfps").child(userID)
                .putBytes(imageData)
                .addOnCompleteListener((task) -> {
                    if(task.isSuccessful()) {
                        task.getResult().getStorage().getDownloadUrl().addOnSuccessListener((url) -> {
                            database.getReference()
                                    .child("profiles")
                                    .child(userID)
                                    .child("pfpURL")
                                    .setValue(url.toString())
                                    .addOnSuccessListener((success) -> {
                                        successfulUploadCallback.accept(url);
                                    });
                        })
                        .addOnFailureListener((exception) -> failedUpload.accept("Failed updating the profile, try again"));
                    } else {
                        failedUpload.accept("Failed uploading image, try again.");
                    }
                });

    }

    /**
     * Checks if there's a user signed in or not.
     * @return If there's a user signed in or not.
     */
    public boolean isSignedIn() {
        return authentication.getCurrentUser() != null;
    }

    /**
     * Registers a new user
     * @param email The email address of the user.
     * @param password The user's password
     * @param onSignUp Callback for when the user is registered successfully
     * @param onFailedSignUp Callback for when registering a new user fails.
     */
    public void signUp(String email, String password, Consumer<FirebaseUser> onSignUp, Consumer<Exception> onFailedSignUp) {
        authentication.createUserWithEmailAndPassword(email, password).addOnCompleteListener((task -> {
            if(task.isSuccessful()) {
                onSignUp.accept(authentication.getCurrentUser());
            } else {
                onFailedSignUp.accept(task.getException());
            }
        }));
    }

    /**
     * Authenticates a user and signs in given an email and password credentials
     * @param email The user's email
     * @param password The user's password
     * @param onSignIn Callback if the sign in succeeds and the credentials were correct.
     * @param onFailedSignIn Callback if the sign in failed or the credentials were incorrect.
     */
    public void signIn(String email, String password, Consumer<FirebaseUser> onSignIn, Consumer<Exception> onFailedSignIn) {
        authentication.signInWithEmailAndPassword(email, password).addOnCompleteListener((task -> {
            if(task.isSuccessful()) {
                onSignIn.accept(authentication.getCurrentUser());
            } else {
                onFailedSignIn.accept(task.getException());
            }
        }));
    }

    /**
     * Signs out the user from the app.
     */
    public void signOut() {
        authentication.signOut();
    }

    /**
     * Gets the database ID for a given test
     * @param test A given test's ID
     * @return The string representing the ID of the test in the database.
     */
    public String getDatabaseIdFromTest(Test test) {
        return test.getSubject().name().toLowerCase() + "_" + test.getType().name().toLowerCase() + "_" + dateFormat.format(test.getDueDate());
    }
}
