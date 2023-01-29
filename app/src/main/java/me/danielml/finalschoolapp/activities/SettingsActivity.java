package me.danielml.finalschoolapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.stream.IntStream;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.managers.FileManager;
import me.danielml.finalschoolapp.managers.FirebaseManager;
import me.danielml.finalschoolapp.objects.FilterProfile;
import me.danielml.finalschoolapp.objects.Subject;
import me.danielml.finalschoolapp.service.ServiceKiller;
import me.danielml.finalschoolapp.service.SyncService;

public class SettingsActivity extends AppCompatActivity {

    private Spinner gradeSpinner;
    private Spinner classNumSpinner;
    private Button updateProfileBtn;
    private Button updateAppSettings;
    private Button backBtn;

    private Button profileSettingsBtn;
    private LinearLayout profileSettingsLayout;
    private Button appSettingsBtn;
    private LinearLayout appSettingsLayout;

    private Switch calendarSyncSwitch;
    private Switch syncServiceSwitch;

    private ArrayAdapter<String> gradeAdapter;
    private ArrayAdapter<Integer> classNumAdapter;
    private ArrayAdapter<String> majorsAdapterA;
    private ArrayAdapter<String> majorsAdapterB;

    private LinearLayout majorsSelectUI;
    private Spinner majorsASpinner;
    private Spinner majorsBSpinner;

    private FirebaseManager firebaseManager;
    private FileManager fileManager;

    private String[] gradeNames;
    private Integer[] classNums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        firebaseManager = new FirebaseManager();
        fileManager = new FileManager(getApplicationContext().getFilesDir());


        backBtn = findViewById(R.id.backBtnSettings);
        backBtn.setOnClickListener((v) -> finish());

        setupCategoryButtonsUI();

        setupAppSettingsUI();

        setupProfileSettingsUI();
    }

    public void setupCategoryButtonsUI() {
        profileSettingsLayout = findViewById(R.id.profileSettings);
        appSettingsLayout = findViewById(R.id.appSettings);

        profileSettingsBtn = findViewById(R.id.profileSettingsBtn);
        appSettingsBtn = findViewById(R.id.appSettingsBtn);

        profileSettingsBtn.setOnClickListener((v) ->
                profileSettingsLayout.setVisibility(profileSettingsLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        appSettingsBtn.setOnClickListener((v) ->
                appSettingsLayout.setVisibility(appSettingsLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));

    }

    public void setupAppSettingsUI()  {
        calendarSyncSwitch = findViewById(R.id.calendarSyncSwitch);
        syncServiceSwitch = findViewById(R.id.backgroundSyncSwitch);

        boolean calendarSync = fileManager.isAutoSyncingCalendar();
        boolean syncServiceOn = fileManager.isSyncServiceEnabled();

        calendarSyncSwitch.setChecked(syncServiceOn && calendarSync);
        syncServiceSwitch.setChecked(syncServiceOn);

        if(calendarSync && !syncServiceOn)
            Toast.makeText(this,"You have calendar auto-sync on, but not the sync service - to use the calendar sync - enable the sync service AND calendar sync" , Toast.LENGTH_LONG).show();

        calendarSyncSwitch.setOnCheckedChangeListener((v, checked) -> {
            if(!syncServiceSwitch.isChecked())
            {
                Toast.makeText(this, "Sync service must be on for calendar auto-syncing!", Toast.LENGTH_SHORT).show();
                calendarSyncSwitch.setChecked(false);
            }
        });


        updateAppSettings = findViewById(R.id.updateAppSettings);
        updateAppSettings.setOnClickListener((v) -> {
            fileManager.saveCalendarAutoSync(calendarSyncSwitch.isChecked());
            fileManager.saveSyncService(syncServiceSwitch.isChecked());

            if(SyncService.SERVICE_RUNNING && !syncServiceSwitch.isChecked()) {
                stopService(new Intent(getApplicationContext(), SyncService.class));
            } else if(!SyncService.SERVICE_RUNNING && syncServiceSwitch.isChecked()) {
                startForegroundService(new Intent(this, SyncService.class));
            }
        });

    }

    public void setupProfileSettingsUI() {
        gradeNames = getResources().getStringArray(R.array.gradeNames);
        classNums = IntStream.of(getResources().getIntArray(R.array.classNums)).boxed().toArray(Integer[]::new);

        firebaseManager.getUserFilterProfile(this::updateUIWithProfile);
    }

    public void updateUIWithProfile(FilterProfile profile) {

        gradeSpinner = findViewById(R.id.gradeSpinner);
        gradeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, gradeNames);
        gradeSpinner.setAdapter(gradeAdapter);

        classNumSpinner = findViewById(R.id.classNumSpinner);
        classNumAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, classNums);
        classNumSpinner.setAdapter(classNumAdapter);

        updateProfileBtn = findViewById(R.id.updateBtn);

        majorsSelectUI = findViewById(R.id.majorsLayout);

        Log.d("SchoolTests","Profile Grade Num: " + profile.getGradeNum());
        Log.d("SchoolTests","Profile Class Num: " + profile.getClassNum());
        gradeSpinner.setSelection((profile.getGradeNum()-1) - gradeNames.length);
        classNumSpinner.setSelection(profile.getClassNum()-1);

        majorsASpinner = findViewById(R.id.majorASpinner);
        majorsBSpinner = findViewById(R.id.majorBSpinner);

        String[] majors = getResources().getStringArray(R.array.majorsNames);
        majorsAdapterA = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, majors);
        majorsAdapterB = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, majors);

        majorsASpinner.setAdapter(majorsAdapterA);
        majorsBSpinner.setAdapter(majorsAdapterB);

        if(profile.getGradeNum() < 10)
            majorsSelectUI.setVisibility(View.GONE);

        if(profile.getMajorA() == null)
            majorsASpinner.setSelection(0);
        else
            majorsASpinner.setSelection(majorsAdapterA.getPosition(profile.getMajorA().getDefaultName()));

        if(profile.getMajorB() == null)
            majorsBSpinner.setSelection(1);
        else
            majorsBSpinner.setSelection(majorsAdapterB.getPosition(profile.getMajorB().getDefaultName()));


        gradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int gradeNum = position + 7;

                Log.d("SchoolTests", "Selected grade: " + gradeNum);
                if(gradeNum >= 10)
                    majorsSelectUI.setVisibility(View.VISIBLE);
                else
                    majorsSelectUI.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        updateProfileBtn.setOnClickListener((v) -> {
            int classNum = classNumSpinner.getSelectedItemPosition() + 1;
            int gradeNum = gradeSpinner.getSelectedItemPosition() + 7;

            Subject selectedMajorA = null;
            Subject selectedMajorB = null;
            if(gradeNum >= 10) {
                selectedMajorA = Subject.from(majorsAdapterA.getItem(majorsASpinner.getSelectedItemPosition()));
                selectedMajorB = Subject.from(majorsAdapterB.getItem(majorsBSpinner.getSelectedItemPosition()));
            }

            firebaseManager.setUserFilterProfile(new FilterProfile(classNum, gradeNum, selectedMajorA, selectedMajorB));
        });

    }


}