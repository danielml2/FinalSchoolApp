package me.danielml.finalschoolapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.managers.CalendarManager;

public class CalendarIntegration extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_integration);

        CalendarManager manager = new CalendarManager();
        manager.printCalendars(this);
    }
}