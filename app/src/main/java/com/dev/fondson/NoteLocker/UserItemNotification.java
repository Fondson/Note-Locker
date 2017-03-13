package com.dev.fondson.NoteLocker;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Fondson on 2017-02-16.
 */

public class UserItemNotification {
    private long time = 0;
    private String dateTimeString = "No notification";
    private transient Intent intent;
    private transient PendingIntent pendingIntent;
    private transient AlarmManager alarmManager;

    public UserItemNotification() {
        Log.d("wtf is going on", "UserItemNotification: hello");
    }

    public String getDateTimeString(){
        return dateTimeString;
    }

    public void setDateTimeString(String s){
        dateTimeString = s;
    }

    public long getTime(){
        return time;
    }
    public void setTime(long time){
        this.time = time;
    }

    public void setIntent(Intent intent){
        this.intent = intent;
    }
    public Intent getIntent(){
        return intent;
    }
    public void setPendingIntent(PendingIntent pendingIntent){
        this.pendingIntent = pendingIntent;
    }
    public PendingIntent getPendingIntent(){
        return pendingIntent;
    }
    public void setAlarmManager(AlarmManager alarmManager){
        this.alarmManager = alarmManager;
    }
    public AlarmManager getAlarmManager(){
        return alarmManager;
    }

    public void scheduleNotification(Context context, UserItem item, Notification notification, long delay) {
        int notificationId = item.getKey().hashCode();
        intent = new Intent(context, NotificationPublisher.class);
        intent.putExtra(NotificationPublisher.NOTIFICATION_ID, notificationId);
        intent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        intent.putExtra("key", item.getKey());
        pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d("testingtheory", "scheduleNotification: " + this.toString());
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
        else alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    public void cancelNotif(){
        if (alarmManager != null) alarmManager.cancel(pendingIntent);
    }

    public Notification getNotification(Context context, UserItem item) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(item.getName());
        builder.setSmallIcon(R.drawable.notif_icon);
        //Vibration: delay, vibration, silent, vibration, silent
        builder.setVibrate(new long[] { 0, 1000, 0, 0, 0 });
        builder.setLights(Color.RED, 1500, 2000);
        Intent clickIntent = new Intent(context, MainActivity.class);
        PendingIntent clickPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        clickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(clickPendingIntent);

        // put in key "remove" and value "item.key" in an intent and start a service with a pending intent to move desired item.
        Intent moveIntent = new Intent();
        Bundle itemBundle = new Bundle();
        itemBundle.putString("key", item.getKey());
        moveIntent.putExtras(itemBundle);

        builder.setAutoCancel(true);
        return builder.build();
    }
}
