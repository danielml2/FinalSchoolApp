package me.danielml.finalschoolapp.activities;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.managers.CalendarManager;

public class CalendarIntegration extends AppCompatActivity {

    private Spinner calendarSelect;
    private ArrayAdapter<String> selectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_integration);

        calendarSelect = findViewById(R.id.calendarSelect);

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

        selectAdapter = new ArrayAdapter<String>(this.getBaseContext(), R.layout.spinner_item, new ArrayList<>(manager.availableCalendarNames()));
        selectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        calendarSelect.setAdapter(selectAdapter);
    }


}