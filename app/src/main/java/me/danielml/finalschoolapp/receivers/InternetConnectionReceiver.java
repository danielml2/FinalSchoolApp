package me.danielml.finalschoolapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.telephony.AvailableNetworkInfo;
import android.util.Log;

import me.danielml.finalschoolapp.activities.OfflineActivity;

public class InternetConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SchoolTests", "Connection changed.");
        ConnectivityManager connectivityManager = context.getSystemService(ConnectivityManager.class);

        NetworkInfo network = connectivityManager.getActiveNetworkInfo();
        boolean connected = (network != null && network.isAvailable());

        Log.d("SchoolTests", "Connected: " + connected);
        if(!connected)
            context.startActivity(new Intent(context, OfflineActivity.class));

    }
}