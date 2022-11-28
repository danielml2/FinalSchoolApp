package me.danielml.finalschoolapp.activities;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.managers.CalendarManager;
import me.danielml.finalschoolapp.managers.FileManager;
import me.danielml.finalschoolapp.objects.Subject;
import me.danielml.finalschoolapp.objects.Test;
import me.danielml.finalschoolapp.objects.TestType;

public class CalendarIntegration extends AppCompatActivity {

    private Spinner calendarSelect;
    private ArrayAdapter<String> selectAdapter;

    private Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_integration);

        calendarSelect = findViewById(R.id.calendarSelect);
        updateButton = findViewById(R.id.exportToCalendarButton);


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

        Test test;
        try {
            FileManager fileManager = new FileManager(this.getFilesDir());
            test = fileManager.getLocalTests().get(0);
        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
            ArrayList<Integer> list = new ArrayList<>();
            test = new Test(Subject.ANATOMY, System.currentTimeMillis(), TestType.QUIZ, 5, list);
        }
        Test finalTest = test;
        updateButton.setOnClickListener((v) ->  {
            manager.addEvent(this, finalTest, selectAdapter.getItem(calendarSelect.getSelectedItemPosition()));
        });
        }


}