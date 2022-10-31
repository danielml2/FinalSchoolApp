package me.danielml.finalschoolapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import me.danielml.finalschoolapp.R;

@SuppressLint("CustomSplashScreen")
// I know this isn't the correct way to do custom splash screens, and that there's a built in feature for it, but that's what my CS teacher accepts.
// So it will be this way, even with the delay being completely irrelevant.
public class SplashScreen extends AppCompatActivity {

    private int dotsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        TextView loadingText = findViewById(R.id.loadingText);


        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        },3000);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                loadingText.setText("Arbitrary Load Screen commencing" + addDots(dotsCount));
                dotsCount += 1;
                dotsCount %= 5;
            }
        }, 0, 250);
    }

    public String addDots(int dotsAmount) {
        String dots = "";
        for(int i = 0; i < dotsAmount; i++)
            dots += ".";
        return dots;
    }
}