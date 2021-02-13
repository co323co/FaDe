package com.example.fade.Alarm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class AlarmService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        final PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(UploadWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager
                .getInstance(this)
                //.enqueue(saveRequest);
                //.enqueueUniquePeriodicWork("gallery_update", ExistingPeriodicWorkPolicy.KEEP,saveRequest);
                .enqueueUniquePeriodicWork("gallery_update", ExistingPeriodicWorkPolicy.KEEP, saveRequest);

        super.onTaskRemoved(rootIntent);

    }
}
