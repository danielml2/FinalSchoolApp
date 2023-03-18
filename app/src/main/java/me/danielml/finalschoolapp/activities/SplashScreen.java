package me.danielml.finalschoolapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.service.SyncService;
import me.danielml.finalschoolapp.activities.login.UserLoginActivity;
import me.danielml.finalschoolapp.managers.FileManager;
import me.danielml.finalschoolapp.managers.FirebaseManager;
import me.danielml.finalschoolapp.service.SyncService;
import me.danielml.finalschoolapp.activities.login.UserLoginActivity;
import me.danielml.finalschoolapp.managers.FileManager;
import me.danielml.finalschoolapp.managers.FirebaseManager;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    private int dotsCount = 0;
    private FirebaseManager dbManager;
    private FileManager fileManager;

    private Intent mainScreen;
    private boolean loading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        TextView loadingText = findViewById(R.id.loadingText);
        dbManager = new FirebaseManager();
        fileManager = new FileManager(getApplicationContext().getFilesDir());
        mainScreen = new Intent(this, MainActivity.class);



        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if(loading)
                {
                    loadingText.setText("Arbitrary Load Screen commencing" + addDots(dotsCount));
                    dotsCount += 1;
                    dotsCount %= 5;
                } else {
                    cancel();
                }
            }
        }, 0, 250);
        syncDataAndSignIn();
    }

    @Override
    protected void onRestart() {
        syncDataAndSignIn();
        super.onRestart();
    }

    public void syncDataAndSignIn() {
        if(!dbManager.isSignedIn())
            startActivity(new Intent(this, UserLoginActivity.class));
        else {
            dbManager.getLastUpdatedTime((lastUpdatedDB) -> {
                try {
                    long localLastUpdate = fileManager.getLocalLastUpdated();
                    if(localLastUpdate < lastUpdatedDB) {
                        Log.d("SchoolTests Sync", "Local data is out of date, syncing data from database");
                        dbManager.getCurrentTests((tests) -> {
                            Log.d("SchoolTests Sync", "Downloaded database data, new tests count: " + tests.size());
                            try {
                                Log.d("SchoolTests Sync", "Saving database data locally...");
                                fileManager.saveDBDataLocally(lastUpdatedDB, tests);
                                Log.d("SchoolTests Sync", "Saved database data locally!");
                            } catch (JSONException | IOException e) {
                                Log.e("SchoolTests Sync", "Failed saving local data! Data is not up to date!");
                                e.printStackTrace();
                            }
                            startActivity(mainScreen);
                            loading = false;
                        });
                    } else {
                        Log.d("SchoolTests Sync", "Local data is up to date!");
                        startActivity(mainScreen);
                        loading = false;
                    }
                } catch (IOException | JSONException e) {
                    Log.e("SchoolTests Sync", "Failed loading local last updated. Exiting...");
                    e.printStackTrace();
                    finish();
                }
            });
        }
    }

    public String addDots(int dotsAmount) {
        String dots = "";
        for(int i = 0; i < dotsAmount; i++)
            dots += ".";
        return dots;
    }
}