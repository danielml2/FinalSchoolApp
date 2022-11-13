package me.danielml.finalschoolapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.managers.FileManager;
import me.danielml.finalschoolapp.objects.Test;

public class MainActivity extends AppCompatActivity {

    private FileManager fileManager;
    private List<Test> tests;
    private long lastUpdatedTime;

    private LinearLayout testsView;
    private TextView lastUpdatedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileManager = new FileManager(getApplicationContext().getFilesDir());
        lastUpdatedText = findViewById(R.id.lastUpdatedText);
        testsView = findViewById(R.id.testsView);

        try {
            tests = fileManager.getLocalTests();
            lastUpdatedTime = fileManager.getLocalLastUpdated();

            tests.sort((test1, test2) -> {
                if(test1.getDueDate() == test2.getDueDate())
                    return 0;
                else
                    return test1.getDueDate() < test2.getDueDate() ? -1 : 1;
            });
            tests.stream()
                    .filter(test -> System.currentTimeMillis() < test.getDueDate())
                    .forEach(test -> testsView.addView(buildView(test)));
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.forLanguageTag("he-IL"));
            SimpleDateFormat hourAndMinute = new SimpleDateFormat("HH:mm");
            Date lastUpdated = new Date(lastUpdatedTime);
            lastUpdatedText.setText("עודכן לאחרונה ב" + dateFormat.format(lastUpdated) + " בשעה " + hourAndMinute.format(lastUpdated));

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }






    }

    public View buildView(Test test) {
        LinearLayout parentLayout = new LinearLayout(this);
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        titleView.setText("שכבה " + getGradeName(test.getGradeNum()) + " " + test.getType().getName() + " " + test.getSubject().getDefaultName());
        detailsView.setText("לכיתות: " + classNumsText);
        dateView.setText(dateFormatted);
        creationText.setText(test.getCreationText());

        reportBtn.setOnClickListener((view) -> {

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
                return "יא'";
            case 12:
                return "יב'";
        }
        return "";
    }
}