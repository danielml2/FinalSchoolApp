package me.danielml.finalschoolapp.activities;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.managers.CalendarManager;
import me.danielml.finalschoolapp.managers.FileManager;
import me.danielml.finalschoolapp.managers.FirebaseManager;
import me.danielml.finalschoolapp.objects.FilterProfile;
import me.danielml.finalschoolapp.objects.Test;

public class CalendarIntegration extends AppCompatActivity {

    private Spinner calendarSelect;
    private ArrayAdapter<String> selectAdapter;

    private Button updateButton;
    private Button jumpToSettingsBtn;
    private ProgressBar progressBar;

    private FilterProfile filterProfile;
    private String savedCalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_integration);

        calendarSelect = findViewById(R.id.calendarSelect);
        updateButton = findViewById(R.id.exportToCalendarButton);
        jumpToSettingsBtn = findViewById(R.id.settingsJmpBtn);

        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (grantedPermissionMap) -> {
            boolean allowedPermissions = grantedPermissionMap.values().stream().allMatch((Boolean::booleanValue));
            if(!allowedPermissions){
                    Toast.makeText(this, "Calendar integration needs calendar permissions in order to work!", Toast.LENGTH_SHORT).show();
                    finish();
            }
            else
                loadCalendarUI();
        }).launch(new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR});

    }

    public void loadCalendarUI() {
        Log.d("SchoolTests", "Calendar permission granted!");
        CalendarManager manager = new CalendarManager();
        manager.loadAvaliableCalendarIDs(this);

        FirebaseManager firebaseManager = new FirebaseManager();
        firebaseManager.getUserFilterProfile(this::setFilterProfile);
        progressBar = findViewById(R.id.progressBarCalendar);

        FileManager fileManager = new FileManager(getApplicationContext().getFilesDir());
        savedCalName = null;
        try {
            long calID = fileManager.getCalendarID();
            savedCalName = manager.getNameFromID(calID);
        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }


        jumpToSettingsBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            intent.putExtra("openAppSettings", true);
            startActivity(intent);

        });
        selectAdapter = new ArrayAdapter<>(this.getBaseContext(), R.layout.spinner_item, new ArrayList<>(manager.availableCalendarNames()));
        selectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        calendarSelect.setAdapter(selectAdapter);

        if(savedCalName != null)
            calendarSelect.setSelection(selectAdapter.getPosition(savedCalName));


        updateButton.setOnClickListener((v) ->  {
            progressBar.setVisibility(View.VISIBLE);
            updateButton.setClickable(false);
            try {
                long calID = fileManager.getCalendarID();
                savedCalName = manager.getNameFromID(calID);

                String selectedCalendar = selectAdapter.getItem(calendarSelect.getSelectedItemPosition());

                HashMap<String, Long> savedEventIDs = new HashMap<>();
                if(!selectedCalendar.equals(savedCalName))
                    fileManager.saveCalendarID(manager.getIDFromName(selectedCalendar));
                else
                    savedEventIDs = fileManager.getEventIDs();

                List<Test> calendarTests = fileManager.getLocalTests();
                if(filterProfile != null)
                    calendarTests = calendarTests.stream().filter(filterProfile::doesPassFilter).collect(Collectors.toList());

                HashMap<String, Long> updatedEventIDs = manager.syncCalendarExport(
                        calendarTests,
                        this,
                        selectAdapter.getItem(calendarSelect.getSelectedItemPosition()),
                        savedEventIDs);

                fileManager.saveEventIDs(updatedEventIDs);

                successfulSyncDialog(selectedCalendar).show();

                Log.d("SchoolTests", "New calendar ID: " + manager.getIDFromName(selectedCalendar));
                Log.d("SchoolTests", "Saved test event IDs count: " + fileManager.getEventIDs().size());

            } catch (IOException | JSONException exception) {
                Toast.makeText(this, "Failed to save or load event IDs in/from JSON", Toast.LENGTH_SHORT).show();
                exception.printStackTrace();
            }
            updateButton.setClickable(true);
            progressBar.setVisibility(View.INVISIBLE);
        });

        }

    public void setFilterProfile(FilterProfile filterProfile) {
        this.filterProfile = filterProfile;
    }

    public AlertDialog successfulSyncDialog(String calendarName) {
        return new AlertDialog.Builder(this)
                .setTitle("Calendar Sync")
                .setMessage(calendarName + " has successfully been synced with the tests list!")
                .setPositiveButton("View Calendar", (dialog, id) -> {
                    long time = System.currentTimeMillis();
                    Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();

                    builder.appendPath("time");
                    ContentUris.appendId(builder, time);
                    Intent openCalendar = new Intent(Intent.ACTION_VIEW);
                    openCalendar.setData(builder.build());

                    startActivity(openCalendar);
                })
                .setNegativeButton("Close", (dialog, id) -> {
                    dialog.cancel();
                }).create();
    }
}