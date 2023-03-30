package me.danielml.finalschoolapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.managers.FirebaseManager;
import me.danielml.finalschoolapp.objects.Test;

public class ReportActivity extends AppCompatActivity  {

    private Test test;
    private HashMap<String, String> selectValuesToDbIDs;
    private FirebaseManager firebaseManager;

    private Spinner issueTypeSelect;
    private ArrayAdapter<String> selectAdapter;

    private LinearLayout testView;
    private Button submitBtn;
    private ProgressBar submitProgressBar;
    private EditText issueDetailsTextbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        selectValuesToDbIDs = new HashMap<>();
        firebaseManager = new FirebaseManager();

        submitProgressBar = findViewById(R.id.submitBar);
        testView = findViewById(R.id.reportedTestView);
        submitBtn = findViewById(R.id.submitBtn);
        issueDetailsTextbox = findViewById(R.id.issueDetails);

        submitProgressBar.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        if(intent != null) {
            test = (Test) intent.getExtras().getSerializable("reportedTest");
            LayoutInflater inflater = getLayoutInflater();
            View v = inflater.inflate(R.layout.test_layout, testView);
            buildViewWithoutReportBtn(v);
        }
        issueTypeSelect = findViewById(R.id.issueTypeSelect);

        selectValuesToDbIDs.put("רשומים שני מועדים, אבל קיים רק מועד האחד","TWO_TESTS");
        selectValuesToDbIDs.put("רשום סוג אחר של מועד ממה שהוא אמור להיות", "INCORRECT_TEST_TYPE");
        selectValuesToDbIDs.put("המבחן הזה לא קיים יותר בלוח מבחנים", "DOESNT_EXIST");
        selectValuesToDbIDs.put("הכיתות שקשורות למועד הזה לא נכונים", "CLASS_NUMS_WRONG");

        selectAdapter = new ArrayAdapter<String>(this.getBaseContext(), R.layout.spinner_item, new ArrayList<>(selectValuesToDbIDs.keySet()));
        selectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        issueTypeSelect.setAdapter(selectAdapter);

        submitBtn.setOnClickListener((v) -> {
            if(issueDetailsTextbox.getText().toString().length() >= 5) {
                String dbIssueID = selectValuesToDbIDs.get(selectAdapter.getItem(issueTypeSelect.getSelectedItemPosition()));
                String details = issueDetailsTextbox.getText().toString();


                submitBtn.setClickable(false);
                submitProgressBar.setVisibility(View.VISIBLE);
                firebaseManager.addReport(test, dbIssueID, details, () -> {
                    finish();
                    Toast.makeText(this, "דוח נוסף בהצלחה!", Toast.LENGTH_SHORT).show();
                    submitBtn.setClickable(true);
                    submitProgressBar.setVisibility(View.INVISIBLE);
                }, () -> {
                    submitProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "לא הצליח לשלוח דוח! בבקשה תנסו שוב", Toast.LENGTH_SHORT).show();
                    submitBtn.setClickable(true);
                });
            } else {
                Toast.makeText(this, "Explanation needs to be at least 5 characters", Toast.LENGTH_SHORT).show();
            }
        });

    }

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