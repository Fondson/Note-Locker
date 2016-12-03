package com.dev.fondson.NoteLocker;

import android.os.Message;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Fondson on 2016-10-20.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String msg = "";

        //msg += "From: " + remoteMessage.getFrom() + "\n";

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            msg += "Data: " + remoteMessage.getData() + "\n";
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            msg += remoteMessage.getNotification().getBody();
        }

        Message message = MainActivity.mHandler.obtainMessage(MainActivity.FIREBASE_MESSAGE_CODE, msg);
        message.sendToTarget();
    }
}
