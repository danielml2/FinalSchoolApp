package me.danielml.finalschoolapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import me.danielml.finalschoolapp.R;

public class OfflineActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.offline_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.connectionCheck) {
            ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);

            NetworkInfo network = connectivityManager.getActiveNetworkInfo();
            boolean connected = (network != null && network.isAvailable());

            Log.d("SchoolTests", "Connected: " + connected);
            if(connected)
                startActivity(new Intent(this, SplashScreen.class));
            else
                Toast.makeText(this, "You are still offline.", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}