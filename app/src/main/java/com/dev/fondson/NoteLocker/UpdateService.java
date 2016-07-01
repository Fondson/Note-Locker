package com.dev.fondson.NoteLocker;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

/**
 * Created by Fondson on 2015-12-04.
 */
public class UpdateService extends Service{

    private BroadcastReceiver mReceiver = new AutoStart();
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        //((KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE)).newKeyguardLock("IN").disableKeyguard();
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        screenStateFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY-1);
        registerReceiver(mReceiver, screenStateFilter);
        //startForeground();
        return START_STICKY;
    }
    // Run service in foreground so it is less likely to be killed by system
    private void startForeground() {
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setTicker(getResources().getString(R.string.app_name))
                .setContentText("Running")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(null)
                .setOngoing(true)
                .build();
        startForeground(9999,notification);
    }
    // Unregister receiver
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        startService(new Intent(UpdateService.this,UpdateService.class));
    }


}