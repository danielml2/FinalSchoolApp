package me.danielml.finalschoolapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import me.danielml.finalschoolapp.managers.CalendarManager;
import me.danielml.finalschoolapp.managers.FileManager;
import me.danielml.finalschoolapp.managers.FirebaseManager;

public class SyncService extends Service {

    private Looper serviceLooper;
    private Handler syncHandler;
    private FileManager fileManager;
    private FirebaseManager firebaseManager;

    private Notification notification;
    private long lastChecked;
    private boolean running = false;
    private final long DELAY = 30000;

    private boolean autoUpdateCalendar = false;
    private CalendarManager calendarManager;
    private long calID = -1;

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("SchoolTestsSync", Process.THREAD_PRIORITY_BACKGROUND);
        fileManager = new FileManager(getApplicationContext().getFilesDir());
        firebaseManager = new FirebaseManager();
        calendarManager = new CalendarManager();

        thread.start();
        try {
            lastChecked = fileManager.getLastCheck();
            autoUpdateCalendar = fileManager.getAutoUpdate();
            if(autoUpdateCalendar)
            {
                calID = fileManager.getCalendarID();
                calID = calendarManager.doesCalendarExist(calID, getApplicationContext()) ? calID : -1;
            }
        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }
        serviceLooper = thread.getLooper();


        syncHandler = new Handler(serviceLooper);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        notification = staticRunningNotification();
        startForeground(1, notification);
        Message msg = syncHandler.obtainMessage();
        msg.arg1 = startId;
        syncHandler.sendEmptyMessage(0);
        long timeSinceLastUpdate = System.currentTimeMillis() - lastChecked;

        long customDelay = DELAY - timeSinceLastUpdate;
        boolean customFirstDelay = timeSinceLastUpdate >= 0 && timeSinceLastUpdate <= DELAY;

        Log.d("SchoolTests", "Time since last update: " + timeSinceLastUpdate);
        Log.d("SchoolTests", "Custom delay: " + customFirstDelay);
        Log.d("SchoolTests", "Calendar ID stored: " + calID);


        if(!running) {
            syncHandler.post(() -> {
                boolean firstRun = customFirstDelay;
                running = true;
            while(running)
            {
                try {
                    Thread.sleep(firstRun ? customDelay : DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(firstRun) firstRun = false;
                sync();

            }
        });
        }

        return START_STICKY;
    }

    public void sync() {
        firebaseManager.getLastUpdatedTime((lastUpdatedDB) -> {
            try {
                long localLastUpdate = fileManager.getLocalLastUpdated();
                if(localLastUpdate < lastUpdatedDB) {
                    Log.d("SchoolTests Sync (Background)", "Local data is out of date, syncing data from database");
                    firebaseManager.getCurrentTests((tests) -> {
                        Log.d("SchoolTests Sync (Background)", "Downloaded database data, new tests count: " + tests.size());
                        try {
                            Log.d("SchoolTests Sync", "Saving database data locally...");
                            fileManager.saveDBDataLocally(lastUpdatedDB, tests);
                            Log.d("SchoolTests Sync (Background)", "Saved database data locally!");
                            if(autoUpdateCalendar && calID != -1) {
                                Log.d("SchoolTests Sync (Background)", "Updating calendar...");
                                HashMap<String, Long> savedEventIDs = fileManager.getEventIDs();
                                savedEventIDs = calendarManager.syncCalendarExport(tests, getApplicationContext(), calID, savedEventIDs);
                                fileManager.saveEventIDs(savedEventIDs);
                            } else if(autoUpdateCalendar && calID == -1) {
                                Log.e("SchoolTests Sync (Background)", "Invalid calendar ID!");
                            }

                        } catch (JSONException | IOException e) {
                            Log.e("SchoolTests Sync (Background)", "Failed saving local data! Data is not up to date!");
                            e.printStackTrace();
                        }
                    });
                } else {
                    Log.d("SchoolTests Sync (Background)", "Local data is up to date!");
                }
            } catch (IOException | JSONException e) {
                Log.e("SchoolTests Sync (Background)", "Failed loading local last updated. Exiting...");
                e.printStackTrace();
            }
        });
        lastChecked = System.currentTimeMillis();
        try {
            fileManager.saveLastCheck(lastChecked);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Notification staticRunningNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "SchoolTests");

        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("SchoolTests", "Test", importance);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        channel.setDescription("test");

        return builder.setContentTitle("yeah")
                .setContentText("beep boop")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).build();
    }

    @Override
    public void onDestroy() {
        Log.d("SchoolTests", "Service has ended!");
    }
}