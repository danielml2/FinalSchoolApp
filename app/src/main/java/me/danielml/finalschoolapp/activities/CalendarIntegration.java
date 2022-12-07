package me.danielml.finalschoolapp.activities;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.managers.CalendarManager;
import me.danielml.finalschoolapp.managers.FileManager;

public class CalendarIntegration extends AppCompatActivity {

    private Spinner calendarSelect;
    private ArrayAdapter<String> selectAdapter;

    private Button updateButton;
    private CheckBox autoUpdateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_integration);

        calendarSelect = findViewById(R.id.calendarSelect);
        updateButton = findViewById(R.id.exportToCalendarButton);
        autoUpdateBtn = findViewById(R.id.autoUpdateBox);

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


        FileManager fileManager = new FileManager(getApplicationContext().getFilesDir());
        String calName = null;
        boolean autoUpdate = false;
        try {
            long calID = fileManager.getCalendarID();
            calName = manager.getNameFromID(calID);
            autoUpdate = fileManager.getAutoUpdate();
        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }


        autoUpdateBtn.setChecked(autoUpdate);
        selectAdapter = new ArrayAdapter<>(this.getBaseContext(), R.layout.spinner_item, new ArrayList<>(manager.availableCalendarNames()));
        selectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        calendarSelect.setAdapter(selectAdapter);

        if(calName != null)
            calendarSelect.setSelection(selectAdapter.getPosition(calName));

        String savedName = calName;
        updateButton.setOnClickListener((v) ->  {
            try {
                String selectedCalendar = selectAdapter.getItem(calendarSelect.getSelectedItemPosition());

                if(!selectedCalendar.equals(savedName))
                    fileManager.saveCalendarID(manager.getIDFromName(selectedCalendar));

                HashMap<String, Long> eventIDs = manager.syncCalendarExport(
                        fileManager.getLocalTests(),
                        this,
                        selectAdapter.getItem(calendarSelect.getSelectedItemPosition()),
                        fileManager.getEventIDs());

                fileManager.saveEventIDs(eventIDs);

                Log.d("SchoolTests", "Saved test event IDs count: " + fileManager.getEventIDs().size());

            } catch (IOException | JSONException exception) {
                Toast.makeText(this, "Failed to save or load event IDs in/from JSON", Toast.LENGTH_SHORT).show();
                exception.printStackTrace();
            }
        });
        autoUpdateBtn.setOnCheckedChangeListener((btn, checked) -> {
            Log.d("SchoolTests", "Auto update: " + checked);
            try {
                fileManager.saveAutoUpdate(checked);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
        }


}