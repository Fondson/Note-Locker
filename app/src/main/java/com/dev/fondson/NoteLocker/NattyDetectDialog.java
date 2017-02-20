package com.dev.fondson.NoteLocker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.ListIterator;

/**
 * Created by Fondson on 2017-02-12.
 */

public class NattyDetectDialog extends DialogFragment{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle extras = getArguments();
        String key = extras.getString("key");
        UserItem item = null;
        ListIterator<UserItem> iterator = MainActivity.userItemsList.listIterator();
        while (iterator.hasNext()){
            item = iterator.next();
            if (key.equals(item.getKey())){
                break;
            }
        }

        final UserItem fItem = item;
        builder.setMessage("Set notification for:\n\"" + item.getName() + "\"")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent pickTime = new Intent(getActivity(), TimePickerActivity.class);
                        pickTime.putExtras(getArguments());
                        startActivity(pickTime);
                    }
                })
                .setNegativeButton("Cancel",  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fItem.notification.setTime(-1);
                        Firebase.updateToDoItem(fItem);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
