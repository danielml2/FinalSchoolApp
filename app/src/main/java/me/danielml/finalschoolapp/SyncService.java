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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.os.ProcessCompat;

public class SyncService extends Service {

    private Looper serviceLooper;
    private Handler syncHandler;

    public SyncService() {
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);

        thread.start();
        serviceLooper = thread.getLooper();

        syncHandler = new Handler(serviceLooper);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(62318, staticRunningNotification());
        Message msg = syncHandler.obtainMessage();
        msg.arg1 = startId;
        syncHandler.sendEmptyMessage(0);
        syncHandler.post(() -> {
            int a = 0;
            while(true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("SchoolTests", a + "");
                a++;
            }
        });

        return START_STICKY;
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
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).build();
    }

    @Override
    public void onDestroy() {
        Log.d("SchoolTests", "Service has ended!");
    }
}