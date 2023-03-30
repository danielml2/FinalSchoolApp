package me.danielml.finalschoolapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    private String[] majorNames;
    private FilterProfile lastProfile = null;
    private boolean firstLoad = true;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileManager = new FileManager(getApplicationContext().getFilesDir());
        firebaseManager = new FirebaseManager();
        majorNames = getResources().getStringArray(R.array.majorsNames);

        lastUpdatedText = findViewById(R.id.lastUpdatedText);
        testsView = findViewById(R.id.testsView);
        signOutTemp = findViewById(R.id.signOutTemp);
        signOutTemp.setOnClickListener((v) -> {
            firebaseManager.signOut();
            finish();
        });
		calendarMenu = findViewById(R.id.calendarButton);
        settingsBtn = findViewById(R.id.settingsBtn);


        try {
            tests = fileManager.getLocalTests();
            lastUpdatedTime = fileManager.getLocalLastUpdated();

            updateTestsList();

            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.forLanguageTag("he-IL"));
            SimpleDateFormat hourAndMinute = new SimpleDateFormat("HH:mm");
            Date lastUpdated = new Date(lastUpdatedTime);
            lastUpdatedText.setText("עודכן לאחרונה ב" + dateFormat.format(lastUpdated) + " בשעה " + hourAndMinute.format(lastUpdated));

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
    }

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

    public void updateTestsList() {
        firebaseManager.getUserFilterProfile((filterProfile) -> {
            SyncService.setFilterProfile(filterProfile);

            tests.sort((test1, test2) -> {
                if(test1.getDueDate() == test2.getDueDate())
                    return 0;
                else
                    return test1.getDueDate() < test2.getDueDate() ? -1 : 1;
            });

            if(filterProfile != null && !filterProfile.equals(lastProfile))
            {
                testsView.removeAllViews();
                Log.d("SchoolTests", "Refreshing tests list with new filter profile");
                lastProfile = filterProfile;
                tests.stream()
                        .filter(test -> System.currentTimeMillis() < test.getDueDate())
                        .filter(filterProfile::doesPassFilter)
                        .forEach(test -> testsView.addView(buildView(test)));
            } else if(filterProfile == null && firstLoad) {
                Log.d("SchoolTests","Profile doesn't exist, showing all tests");
                tests.stream()
                        .filter(test -> System.currentTimeMillis() < test.getDueDate())
                        .forEach(test -> testsView.addView(buildView(test)));
                firstLoad = false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateTestsList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.signOutMenu) {
            firebaseManager.signOut();
            startActivity(new Intent(this, UserLoginActivity.class));
        } else if(item.getItemId() == R.id.settingsMenu) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if(item.getItemId() == R.id.changePfpMenu) {
            startActivity(new Intent(this, ProfilePictureActivity.class));
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}