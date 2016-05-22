package com.example.fondson.mylockscreen;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/**
 * Created by Fondson on 2016-05-10.
 */
public class DBAdapter {
    /////////////////////////////////////////////////////////////////////
    //	Constants & Data
    /////////////////////////////////////////////////////////////////////
    // For logging:
    private static final String TAG = "DBAdapter";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final int COL_ROWID = 0;

    public static final String KEY_ITEM = "item";
    public static final int COL_ITEM = 1;

    public static final String KEY_SELECTED = "selected";
    public static final int COL_SELECTED = 2;


    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_ITEM,KEY_SELECTED};

    // DB info: it's name, and the table we are using.
    public static final String DATABASE_NAME = "NoteLockerDB";
    private static String DATABASE_TABLE;
    public final static String DATABASE_TABLE_ITEMS = "itemTable";
    public final static String DATABASE_TABLE_COMPLETED_ITEMS = "completedItemTable";

    public static final int DATABASE_VERSION = 1;

    private static String DATABASE_CREATE_ITEM_SQL ="create table " + DATABASE_TABLE_ITEMS
                                                    + " (" + KEY_ROWID + " integer primary key autoincrement, "
                                                    + KEY_ITEM + " string not null"
                                                    + KEY_SELECTED +" integer not null);";
    private static String DATABASE_CREATE_COMPLETED_ITEM_SQL ="create table " + DATABASE_TABLE_COMPLETED_ITEMS
                                                            + " (" + KEY_ROWID + " integer primary key autoincrement, "
                                                            + KEY_ITEM + " string not null"
                                                            + KEY_SELECTED +" integer not null);";



    // Context of application
    private final Context context;

    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    public DBAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    public void switchTable(String table){
        DATABASE_TABLE=table;
    }

    // Open the database connection.
    public DBAdapter open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }

    // Add a new set of values to the database.
    public long insertRow(String item) {
        // Create row's data:
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ITEM,item);

        // Insert it into the database.
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    // Add a new set of values to the database.
    public long insertRow(String item,int selected) {
        // Create row's data:
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ITEM,item);
        initialValues.put(KEY_SELECTED,selected);

        // Insert it into the database.
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }

    public void deleteAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // Return all data in the database.
    public Cursor getAllRows() {
        String where = null;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Get a specific row (by rowId)
    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, "by Id desc", null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Get a specific row (by item)
    public Cursor getRow(String item){
        String where=String.format(KEY_ITEM + "= %s",item);
        Cursor c=db.query(DATABASE_TABLE,ALL_KEYS,where,null,null,null,null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    //checks if item exists in table already
    public boolean exists(String item){
        String where=String.format(KEY_ITEM + "= %s",item);
        Cursor c=db.query(DATABASE_TABLE,ALL_KEYS,where,null,null,null,null);
        if (c!=null){
            return true;
        }
        return false;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, String item) {
        String where = KEY_ROWID + "=" + rowId;

        // Create row's data:
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_ITEM,item);

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRowID(long rowId, String item, long newRowId) {
        String where = KEY_ROWID + "=" + rowId;

        // Create row's data:
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_ITEM,item);
        newValues.put(KEY_ROWID,newRowId);

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRowSelected(long rowId, String item, int newSelected) {
        String where = KEY_ROWID + "=" + rowId;

        // Create row's data:
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_ITEM,item);
        newValues.put(KEY_SELECTED,newSelected);

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }


    /////////////////////////////////////////////////////////////////////
    //	Private Helper Classes:
    /////////////////////////////////////////////////////////////////////

    /**
     * Private class which handles database creation and upgrading.
     * Used to handle low-level database access.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE_ITEM_SQL);
            _db.execSQL(DATABASE_CREATE_COMPLETED_ITEM_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_COMPLETED_ITEMS);
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ITEMS);

            // Recreate new database:
            onCreate(_db);
        }
    }
}
