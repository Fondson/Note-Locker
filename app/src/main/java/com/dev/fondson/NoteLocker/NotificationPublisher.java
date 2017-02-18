package com.dev.fondson.NoteLocker;

/**
 * Created by Fondson on 2017-02-12.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ListIterator;

public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public static int ID = 0;

    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManager.notify(id, notification);

        String key = intent.getStringExtra("key");
        UserItem item = null;
        ListIterator<UserItem> iterator = MainActivity.userItemsList.listIterator();
        while (iterator.hasNext()){
            item = iterator.next();
            if (key.equals(item.getKey())){
                item.notification.setDateTimeString("No notification");
                item.notification.setTime(-1);
                Firebase.updateToDoItem(item);
                break;
            }
        }

    }
}