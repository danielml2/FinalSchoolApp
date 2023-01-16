package me.danielml.finalschoolapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.stream.IntStream;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.managers.FirebaseManager;
import me.danielml.finalschoolapp.objects.FilterProfile;

public class SettingsActivity extends AppCompatActivity {

    private Spinner gradeSpinner;
    private Spinner classNumSpinner;
    private ArrayAdapter<String> gradeAdapter;
    private ArrayAdapter<Integer> classNumAdapter;

    private LinearLayout majorsSelectUI;
    private Spinner majorASpinner;
    private Spinner majorBSpinner;

    private FirebaseManager firebaseManager;

    private String[] gradeNames;
    private Integer[] classNums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        gradeNames = getResources().getStringArray(R.array.gradeNames);
        classNums = IntStream.of(getResources().getIntArray(R.array.classNums)).boxed().toArray(Integer[]::new);

        firebaseManager = new FirebaseManager();

        gradeSpinner = findViewById(R.id.gradeSpinner);
        gradeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, gradeNames);
        gradeSpinner.setAdapter(gradeAdapter);

        classNumSpinner = findViewById(R.id.classNumSpinner);
        classNumAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, classNums);
        classNumSpinner.setAdapter(classNumAdapter);

        majorsSelectUI = findViewById(R.id.majorsLayout);

        firebaseManager.getUserFilterProfile(this::updateUIWithProfile);
    }

    public void updateUIWithProfile(FilterProfile profile) {

        Log.d("SchoolTests","Profile Grade Num: " + profile.getGradeNum());
        Log.d("SchoolTests","Profile Class Num: " + profile.getClassNum());
        gradeSpinner.setSelection((profile.getGradeNum()-1) - gradeNames.length);
        classNumSpinner.setSelection(profile.getClassNum()-1);
    }


}