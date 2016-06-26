package com.dev.fondson.NoteLocker;

/**
 * Created by Fondson on 2016-06-25.
 */
import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;


public class TheBackupAgent extends BackupAgentHelper {
    // The name of the SharedPreferences file
    static final String NOTELOCKERDB = "NoteLockerDB";
    static final String PREFS = "com.dev.fondson.NoteLocker_preferences.xml";

    // A key to uniquely identify the set of backup data
    static final String DB_BACKUP_KEY = "db";
    static final String PREFS_BACKUP_KEY = "prefs";


    @Override
    public void onCreate() {
        FileBackupHelper hosts = new FileBackupHelper(this,
                "../databases/" + NOTELOCKERDB);
        addHelper(DB_BACKUP_KEY, hosts);

        SharedPreferencesBackupHelper helper =
                new SharedPreferencesBackupHelper(this, PREFS);
        addHelper(PREFS_BACKUP_KEY, helper);
    }
}