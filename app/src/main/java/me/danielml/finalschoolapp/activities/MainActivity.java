package me.danielml.finalschoolapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.managers.FileManager;
import me.danielml.finalschoolapp.objects.Test;

public class MainActivity extends AppCompatActivity {

    private FileManager fileManager;
    private List<Test> tests;
    private long lastUpdatedTime;

    private LinearLayout testsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileManager = new FileManager(getApplicationContext().getFilesDir());

        try {
            tests = fileManager.getLocalTests();
            lastUpdatedTime = fileManager.getLocalLastUpdated();


        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }


        testsView = findViewById(R.id.testsView);
        for(int i = 0; i < tests.size(); i++) {
            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.test_layout, testsView);
        }



    }
}