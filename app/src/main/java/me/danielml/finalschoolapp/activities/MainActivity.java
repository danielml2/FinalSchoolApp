package me.danielml.finalschoolapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
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


        tests.sort((test1, test2) -> {
            if(test1.getDueDate() == test2.getDueDate())
                return 0;
            else
                return test1.getDueDate() < test2.getDueDate() ? -1 : 1;
        });
        testsView = findViewById(R.id.testsView);
        tests.forEach(test -> testsView.addView(buildView(test)));



    }

    public View buildView(Test test) {
        LinearLayout parentLayout = new LinearLayout(this);
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.test_layout, parentLayout);


        TextView titleView = v.findViewById(R.id.testTitleTV);
        TextView detailsView = v.findViewById(R.id.testDetailsTV);
        TextView dateView = v.findViewById(R.id.dateTV);

        titleView.setText("(Grade " + test.getGradeNum() + "): " + test.getSubject() + " " + test.getType());
        detailsView.setText("For classes: " + test.getClassNums().toString());
        dateView.setText("at: " + test.getDateFormatted());



        return parentLayout;
    }
}