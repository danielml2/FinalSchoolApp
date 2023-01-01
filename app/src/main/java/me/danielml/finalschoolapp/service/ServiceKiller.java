package me.danielml.finalschoolapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceKiller extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.hasExtra("endService")) {
            Log.d("SchoolTests", "Ending sync service");
            context.stopService(new Intent(context.getApplicationContext(), SyncService.class));
        }
    }
}