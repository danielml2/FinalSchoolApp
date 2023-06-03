package me.danielml.finalschoolapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.activities.login.UserLoginActivity;
import me.danielml.finalschoolapp.managers.FileManager;
import me.danielml.finalschoolapp.managers.FirebaseManager;
import me.danielml.finalschoolapp.objects.FilterProfile;
import me.danielml.finalschoolapp.objects.Test;
import me.danielml.finalschoolapp.service.SyncService;

public class MainActivity extends AppCompatActivity {

    private FileManager fileManager;
    private FirebaseManager firebaseManager;
    private List<Test> tests;
    private long lastUpdatedTime;

    private LinearLayout testsView;
    private TextView lastUpdatedText;

    private Button signOutTemp;
	private Button calendarMenu;
    private Button settingsBtn;

    private FilterProfile lastProfile = null;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileManager = new FileManager(getApplicationContext().getFilesDir());
        firebaseManager = new FirebaseManager();

        lastUpdatedText = findViewById(R.id.lastUpdatedText);
        testsView = findViewById(R.id.testsView);
        signOutTemp = findViewById(R.id.signOutTemp);
        signOutTemp.setOnClickListener((v) -> {
            firebaseManager.signOut();
            startActivity(new Intent(this, UserLoginActivity.class));
        });
		calendarMenu = findViewById(R.id.calendarButton);
        settingsBtn = findViewById(R.id.settingsBtn);


        try {
            tests = fileManager.getLocalTests();
            lastUpdatedTime = fileManager.getLocalLastUpdated();

            updateLastUpdatedText();
            updateTestsList(true);

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        calendarMenu.setOnClickListener((v) -> startActivity(new Intent(this, CalendarIntegration.class)));
        settingsBtn.setOnClickListener((v) -> startActivity(new Intent(this, SettingsActivity.class)));

        if(!SyncService.SERVICE_RUNNING && fileManager.isSyncServiceEnabled()) {
            Intent intent = new Intent(this, SyncService.class);
            startForegroundService(intent);
        } else {
            Log.d("SchoolTests", "Service is already running or is not enabled");
        }

        tts = new TextToSpeech(getApplicationContext(), (code) -> {
            if(code == TextToSpeech.SUCCESS)
                tts.setLanguage(Locale.forLanguageTag("he-IL"));
        });
        updateProfilePictureActionBar();
    }

    /**
     * Creates a view from test_layout.xml for the given test, to be inserted to the ListView
     * @param test the test the view should show
     * @return the created view
     */
    public View buildView(Test test) {
        Log.d("SchoolTests", "Building view for: " + test);
        LinearLayout parentLayout = new LinearLayout(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.test_layout, parentLayout);


        TextView titleView = v.findViewById(R.id.testTitleTV);
        TextView detailsView = v.findViewById(R.id.testDetailsTV);
        TextView dateView = v.findViewById(R.id.dateTV);
        TextView creationText = v.findViewById(R.id.creationTextTV);
        Button reportBtn = v.findViewById(R.id.reportBtn);

        String classNumsText = test.getClassNums().toString()
                .replace("[", "")
                 .replace("]","")
                .replace("-1","שכבתי");
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.forLanguageTag("he-IL"));
        String dateFormatted = dateFormat.format(test.asDate());
        dateFormatted = dateFormatted.substring(0, dateFormatted.length()-4);

        String title = "שכבה " + getGradeName(test.getGradeNum()) + " " + test.getType().getName() + " " + test.getSubject().getDefaultName();
        titleView.setText(title);
        detailsView.setText("לכיתות: " + classNumsText);
        dateView.setText(dateFormatted);
        creationText.setText(test.getCreationText());

        final String ttsFormatted = dateFormatted;
        v.setOnClickListener((view) -> {
            tts.speak(title  + " ב "  + ttsFormatted, TextToSpeech.QUEUE_FLUSH, null);
        });
        reportBtn.setOnClickListener((view) -> {
            Intent intent = new Intent(this, ReportActivity.class);
            intent.putExtra("reportedTest", test);

            startActivity(intent);
        });

        return parentLayout;
    }

    /**
     * Gets the hebrew grade name for the given grade number (7-12)
     * @param gradeNum Grade number
     * @return The grade's name in hebrew characters as a string.
     */
    public String getGradeName(int gradeNum) {
        switch(gradeNum) {
            case 7:
                return "ז'";
            case 8:
                return "ח'";
            case 9:
                return "ט'";
            case 10:
                return "י'";
            case 11:
                return "י\"א";
            case 12:
                return "י\"ב";
        }
        return "";
    }

    /**
     * Updates the ListView of tests given the latest user profile and data, if there's changes (unless it's force loaded)
     * @param forceLoad Load/Reload the test list despite if there's changes to the profile or data.
     */
    public void updateTestsList(boolean forceLoad) {
        firebaseManager.getUserFilterProfile((filterProfile) -> {
            SyncService.setFilterProfile(filterProfile);

            tests.sort((test1, test2) -> {
                if(test1.getDueDate() == test2.getDueDate())
                    return 0;
                else
                    return test1.getDueDate() < test2.getDueDate() ? -1 : 1;
            });

            if(filterProfile != null && (!filterProfile.equals(lastProfile) || forceLoad))
            {
                testsView.removeAllViews();
                Log.d("SchoolTests", "Refreshing tests list with new filter profile");
                lastProfile = filterProfile;
                tests.stream()
                        .filter(test -> System.currentTimeMillis() < test.getDueDate())
                        .filter(filterProfile::doesPassFilter)
                        .forEach(test -> testsView.addView(buildView(test)));
            } else if(filterProfile == null && forceLoad) {
                Log.d("SchoolTests","Profile doesn't exist, showing all tests");
                tests.stream()
                        .filter(test -> System.currentTimeMillis() < test.getDueDate())
                        .forEach(test -> testsView.addView(buildView(test)));
            }
            testsView.refreshDrawableState();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateTestsList(false);
        updateProfilePictureActionBar();
        firebaseManager.getLastUpdatedTime((newLastUpdate) -> {
            Log.d("SchoolTests", "Got latest time?");
            if(lastUpdatedTime < newLastUpdate) {
                lastUpdatedTime = newLastUpdate;
                updateLastUpdatedText();
                firebaseManager.getCurrentTests((newTests) -> {
                    tests = newTests;
                    Log.d("SchoolTests", "New tests");
                    updateTestsList(true);
                    try {
                        fileManager.saveDBDataLocally(newLastUpdate, newTests);
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    /**
     * Updates the last updated TextView on the activity to the latest lastUpdatedTime value.
     */
    public void updateLastUpdatedText() {
        Log.d("SchoolTests", "Updating text!");
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.forLanguageTag("he-IL"));
        SimpleDateFormat hourAndMinute = new SimpleDateFormat("HH:mm");
        Date lastUpdated = new Date(lastUpdatedTime);
        lastUpdatedText.setText("עודכן לאחרונה ב" + dateFormat.format(lastUpdated) + " בשעה " + hourAndMinute.format(lastUpdated));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        MenuHandler.handleItemSelected(this, item);
        return true;
    }

    /**
     * Updates/loads the actionbar for the activity, fills it with the custom layout and the user's profile picture if he one set.
     */
    public void updateProfilePictureActionBar() {
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {

            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.actionbar_layout);

            View actionBarLayout = actionBar.getCustomView();

            ImageView pfpView = actionBarLayout.findViewById(R.id.pfpActionbar);
            TextView titleView = actionBarLayout.findViewById(R.id.actionBarTitle);

            String title = "Welcome, " + firebaseManager.getCurrentUser().getDisplayName();
            titleView.setTextDirection(View.TEXT_DIRECTION_LTR);
            titleView.setText(title);

            firebaseManager.getProfilePictureForCurrentUser((uri) -> {
                if(uri != null)
                    Picasso.get().load(uri).resize(125, 125).noFade().into(pfpView);
            });
        }

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}