package me.danielml.finalschoolapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.objects.Test;

public class ReportActivity extends AppCompatActivity  {

    private LinearLayout testView;
    private Spinner issueTypeSelect;
    private HashMap<String, String> selectValuesToDbIDs = new HashMap<>();
    private Test test;

    private ArrayAdapter<String> selectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        testView = findViewById(R.id.reportedTestView);

        Intent intent = getIntent();
        if(intent != null) {
            test = (Test) intent.getExtras().getSerializable("reportedTest");
            LayoutInflater inflater = getLayoutInflater();
            View v = inflater.inflate(R.layout.test_layout, testView);
            buildViewWithoutReportBtn(v);
        }
        issueTypeSelect = findViewById(R.id.issueTypeSelect);

        selectValuesToDbIDs.put("TWO_TESTS", "רשומים שני מועדים, אבל קיים רק מועד האחד");
        selectValuesToDbIDs.put("INCORRECT_TEST_TYPE", "רשום סוג אחר של מועד ממה שהוא אמור להיות");
        selectValuesToDbIDs.put("DOESNT_EXIST", "המבחן הזה לא קיים יותר בלוח מבחנים");
        selectValuesToDbIDs.put("CLASS_NUMS_WRONG", "הכיתות שקשורות למועד הזה לא נכונים");

        selectAdapter = new ArrayAdapter<String>(this.getBaseContext(), R.layout.spinner_item, new ArrayList<>(selectValuesToDbIDs.values()));
        selectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        issueTypeSelect.setAdapter(selectAdapter);


    }

    // I am not proud of having to copy-paste the method here, but there isn't really a viable solution structure-wise
    public void buildViewWithoutReportBtn(View v) {
        TextView titleView = v.findViewById(R.id.testTitleTV);
        TextView detailsView = v.findViewById(R.id.testDetailsTV);
        TextView dateView = v.findViewById(R.id.dateTV);
        TextView creationText = v.findViewById(R.id.creationTextTV);
        Button reportBtn = v.findViewById(R.id.reportBtn);
        reportBtn.setVisibility(View.INVISIBLE);

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