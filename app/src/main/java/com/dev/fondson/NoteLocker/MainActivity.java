package com.dev.fondson.NoteLocker;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.app.WallpaperManager;
import android.app.backup.BackupManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.fondson.NoteLocker.model.SlidrConfig;
import com.dev.fondson.NoteLocker.model.SlidrInterface;
import com.dev.fondson.NoteLocker.model.SlidrListener;
import com.dev.fondson.NoteLocker.model.SlidrPosition;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    public static final int WALLPAPER_CODE = 10;
    public static final int GOOGLE_ACCOUNT_SIGN_IN_CODE = 9;
    public static DBAdapter db;
    public static String userEmail;
    public static KeyListener listener;
    public static String WALLPAPER_PATH;
    public static String WALLPAPER_FULL_PATH;
    public static String[] wallpaperPerms={"android.permission.READ_EXTERNAL_STORAGE"};//,"android.permission.WRITE_EXTERNAL_STORAGE","android.permision.READ_INTERNAL_STORAGE","android.permission.WRITE_INTERNAL_STORAGE"};
    public static String[] calendarPerms = {"android.permission.READ_CALENDAR","android.permission.WRITE_CALENDAR"};
    public static final int CALENDAR_PERMS = 0;
    private static RelativeLayout rl;
    private static ImageView darkTint;
    private static LinearLayout ll;
    public static GoogleApiClient mGoogleApiClient;
    private EditText etInput;
    private ArrayList<ArrayList<?>> itemsArray;
    private ArrayList<CalendarItem> calendarItemArr;
    private ArrayList<UserItem> userItemArr;
    private ArrayList<UserItem> completedItemsArr;
    private ExpandableListView expandableListView;
    private ItemsAdapter itemsAdapter;
    private UnlockBar unlock;
    private SlidrConfig config;
    private SlidrInterface slidrInterface;
    private Calendar beginTime;

    public static final String[] INSTANCE_PROJECTION = new String[] {
            CalendarContract.Instances.EVENT_ID,      // 0
            CalendarContract.Instances.BEGIN,         // 1
            CalendarContract.Instances.TITLE,         // 2
            CalendarContract.Instances.END            // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_BEGIN_INDEX = 1;
    private static final int PROJECTION_TITLE_INDEX = 2;
    private static final int PROJECTION_END_INDEX = 3;

    private static final String[] EVENT_PROJECTION =
            new String[]{
                    CalendarContract.Events.EVENT_LOCATION};

    private static final int PROJECTION_LOCATION_INDEX = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
        //getWindow().setStatusBarColor(Color.TRANSPARENT);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //open database
        db=new DBAdapter(this);
        db.open();

        userItemArr = new ArrayList<UserItem>();
        completedItemsArr =new ArrayList<UserItem>();
        db.switchTable(DBAdapter.DATABASE_TABLE_ITEMS);
        getAllItems(db.getAllRows(),userItemArr);
        db.switchTable(DBAdapter.DATABASE_TABLE_COMPLETED_ITEMS);
        getAllItems(db.getAllRows(),completedItemsArr);

        calendarItemArr = new ArrayList<CalendarItem>();
        beginTime = Calendar.getInstance();
        getCalendarEvents(calendarItemArr);

        itemsArray = new ArrayList<ArrayList<?>>();

        expandableListView=(ExpandableListView) findViewById(R.id.exlvItems);
        itemsArray.add(calendarItemArr);
        itemsArray.add(userItemArr);
        itemsArray.add(completedItemsArr);
        itemsAdapter = new ItemsAdapter(this,itemsArray);
        expandableListView.setAdapter(itemsAdapter);
        expandableListView.expandGroup(itemsAdapter.CALENDAR);
        expandableListView.expandGroup(itemsAdapter.NOT_COMPLETED);
        itemsAdapter.notifyDataSetChanged();

        introCheck();

        rl = (RelativeLayout) findViewById(R.id.rl);
        //set initial wallpaper
        WALLPAPER_PATH = getFilesDir().getAbsolutePath();
        WALLPAPER_FULL_PATH = WALLPAPER_PATH + "/wallpaper.jpg";
        File wallpaperFile= new File(WALLPAPER_FULL_PATH);

        if(wallpaperFile.exists()
                && this.checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE")== PackageManager.PERMISSION_GRANTED) {
            Drawable wallpaper=Drawable.createFromPath(WALLPAPER_FULL_PATH);
            rl.setBackground(wallpaper);
        }
        else{
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            rl.setBackground(wallpaperDrawable);
        }

        ll = (LinearLayout) findViewById(R.id.llMain);
        ll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                fullScreencall();
                return false;
            }
        });
        darkTint=(ImageView)findViewById(R.id.ivDarkTint);
        ((View)darkTint).setAlpha((float)PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getInt("pref_key_darkTint", 50)/100);

        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (16 * scale + 0.5f); //standard padding by Android Design Guidelines
        ll.setPadding(dpAsPixels, dpAsPixels + getStatusBarHeight(), dpAsPixels, dpAsPixels);
        startService(new Intent(this, UpdateService.class));

        etInput = (EditText) findViewById(R.id.editText);
        etInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                                              @Override
                                              public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
                                                  if (!(etInput.getText().toString().trim().matches(""))) {
                                                      db.switchTable(DBAdapter.DATABASE_TABLE_ITEMS);
                                                      Long newId=db.insertRow(etInput.getText().toString().trim());
                                                      //writeFile(etInput.getText().toString().trim());
                                                      userItemArr.add(0, new UserItem(newId, etInput.getText().toString().trim(), false));
                                                      itemsAdapter.notifyDataSetChanged();
                                                      etInput.setText("");
                                                      requestBackup(MainActivity.this);
                                                  }
                                                  return true;

                                              }
                                          }
        );
        etInput.setOnClickListener(new EditText.OnClickListener() {
            @Override
            public void onClick(View view) {
                //homeKeyLocker.unlock();
                InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etInput, InputMethodManager.SHOW_FORCED);
            }
        });
        listener = etInput.getKeyListener();

        //settings button
        ImageButton btnSettings=(ImageButton) findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
        });

        config = new SlidrConfig.Builder()
                                .position(SlidrPosition.BOTTOM)
                                .sensitivity(0.75f)
                                .scrimColor(Color.TRANSPARENT)
                                .scrimStartAlpha(1f)
                                .scrimEndAlpha(0f)
                                .velocityThreshold(2400)
                                .distanceThreshold(0.25f)
                                .edge(true)
                                .edgeSize(0.18f) // The % of the screen that counts as the edge, default 18%
                                .build();


        slidrInterface=Slidr.attach(this, config);

        ShimmerFrameLayout slideUpShimmer = (ShimmerFrameLayout) findViewById(R.id.slide_up_shimmer);
        slideUpShimmer.setDuration(1500);
        slideUpShimmer.startShimmerAnimation();
    }

    private void introCheck(){
        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    Intent i = new Intent(MainActivity.this, Intro.class);
                    startActivity(i);

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();

                    expandableListView.expandGroup(itemsAdapter.COMPLETED);
                }
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();

        // Start the thread
        t.start();
    }

    public static void requestBackup(Context context) {
        BackupManager bm = new BackupManager(context);
        bm.dataChanged();
    }

    private void getCalendarEvents(ArrayList<CalendarItem> calendarItems){
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.PREF_KEY_CALENDAR,true)
                && checkCallingOrSelfPermission("android.permission.READ_CALENDAR")== PackageManager.PERMISSION_GRANTED
                && checkCallingOrSelfPermission("android.permission.WRITE_CALENDAR")== PackageManager.PERMISSION_GRANTED) {
            try {
                Cursor cur = null;
                ContentResolver cr = getContentResolver();

                // Specify the date range you want to search for recurring
                // event instances
                beginTime = Calendar.getInstance();
                beginTime.set(Calendar.HOUR_OF_DAY, 0);
                beginTime.set(Calendar.MINUTE, 0);
                beginTime.set(Calendar.SECOND, 0);
                beginTime.set(Calendar.MILLISECOND, 1);
                beginTime.setTimeZone(TimeZone.getTimeZone("UTC"));

                long startMillis = beginTime.getTimeInMillis();
                Calendar endTime = Calendar.getInstance();
                endTime.set(Calendar.HOUR_OF_DAY, 23);
                endTime.set(Calendar.MINUTE, 59);
                endTime.set(Calendar.SECOND, 59);
                endTime.set(Calendar.MILLISECOND, 1);
                endTime.setTimeZone(TimeZone.getTimeZone("UTC"));
                long endMillis = endTime.getTimeInMillis();

                Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
                ContentUris.appendId(builder, startMillis);
                ContentUris.appendId(builder, endMillis);


                // Submit the query
                cur = cr.query(builder.build(),
                        INSTANCE_PROJECTION,
                        null,
                        null,
                        CalendarContract.Instances.BEGIN + " DESC");

                while (cur.moveToNext()) {
                    String title = null;
                    String location = null;
                    long eventID = 0;
                    long beginVal = 0;
                    long endVal = 0;

                    // Get the field values
                    eventID = cur.getLong(PROJECTION_ID_INDEX);
                    beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
                    title = cur.getString(PROJECTION_TITLE_INDEX);
                    endVal = cur.getLong(PROJECTION_END_INDEX);

                    try {
                        Cursor cursor = cr.query(
                                CalendarContract.Events.CONTENT_URI,
                                EVENT_PROJECTION,
                                CalendarContract.Events._ID + " = ? ",
                                new String[]{Long.toString(eventID)},
                                null);
                        if (cursor.moveToFirst()) {
                            location = cursor.getString(PROJECTION_LOCATION_INDEX);
                        }
                        cursor.close();
                    } catch (SecurityException e) {
                        Toast.makeText(this, "Cannot get calendar event start time.\nError: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        Log.d("calEvent", "Event ID Error " + e.getMessage());
                    }

                    // add to calendarItems
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(beginVal);
                    DateFormat formatter = new SimpleDateFormat("E-MMM dd");
                    String date = formatter.format(calendar.getTime());
                    formatter = new SimpleDateFormat("h:mm a");
                    String timeBegin = formatter.format(calendar.getTime());
                    calendar.setTimeInMillis(endVal);
                    String timeEnd = formatter.format(calendar.getTime());
                    if (timeBegin.equals(timeEnd)) {
                        timeBegin = "All day";
                        timeEnd = null;
                    }
                    if (location.isEmpty()) location = null;
                String item = formatter.format(calendar.getTime()) + "\n" + title + "\nat " + location;
                Log.d("calEvent",item);
                    calendarItems.add(0, new CalendarItem(date, timeBegin, timeEnd, title, location));
                }
                Log.d("calEvent", "cursor done");
                beginTime=Calendar.getInstance();
                beginTime.set(Calendar.HOUR_OF_DAY, 0);
                beginTime.set(Calendar.MINUTE, 0);
                beginTime.set(Calendar.SECOND, 0);
                beginTime.set(Calendar.MILLISECOND, 1);
                cur.close();
            } catch (Exception e) {
                Toast.makeText(this, "Cannot get calendar events.\nError: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                Log.d("calEvent", e.getMessage());
            }
        }
    }

    public static void getAllItems(Cursor cursor,ArrayList<UserItem> arrayList){;
        // Reset cursor to start, checking to see if there's data:
        if (cursor.moveToFirst()) {
            do {
                // Process the data:

                int id = cursor.getInt(DBAdapter.COL_ROWID);
                String item = cursor.getString(DBAdapter.COL_ITEM);
                boolean selected=false;
                if (cursor.getInt(DBAdapter.COL_SELECTED)==1){
                    selected=true;
                }

                arrayList.add(0,new UserItem(id,item,selected));
            } while(cursor.moveToNext());
        }
    }


    public static ImageView getDarkTint(){
        return darkTint;
    }

    // Don't finish Activity on Back press
    @Override
    public void onBackPressed() {
        return;
    }

    protected void onPause() {
        super.onPause();
        hideKeyboard();
        startService(new Intent(this, UpdateService.class));
    }

    public void fullScreencall() {
        if(Build.VERSION.SDK_INT < 19){ //19 or above api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else {
            //for lower api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
    protected void onResume() {

        fullScreencall();

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.PREF_KEY_CALENDAR,true)
                && checkCallingOrSelfPermission("android.permission.READ_CALENDAR")== PackageManager.PERMISSION_GRANTED
                && checkCallingOrSelfPermission("android.permission.WRITE_CALENDAR")== PackageManager.PERMISSION_GRANTED) {
            if (calendarItemArr.size()==0) {
                getCalendarEvents(calendarItemArr);
                itemsAdapter.notifyDataSetChanged();
                expandableListView.expandGroup(itemsAdapter.CALENDAR);
            }
            else {
                Calendar currentTime = Calendar.getInstance();
                currentTime.getTime();
                currentTime.set(Calendar.HOUR_OF_DAY, 0);
                currentTime.set(Calendar.MINUTE, 0);
                currentTime.set(Calendar.SECOND, 0);
                currentTime.set(Calendar.MILLISECOND, 1);
                if (beginTime.compareTo(currentTime) != 0) {
                    Log.d("calEvent", String.valueOf(beginTime.compareTo(currentTime)));
                    calendarItemArr.clear();
                    getCalendarEvents(calendarItemArr);
                    itemsAdapter.notifyDataSetChanged();
                }
            }
        }

        ((EditText) findViewById(R.id.editText)).setText("");
        itemsAdapter.notifyDataSetChanged();

        ((View)findViewById(R.id.slidable_content)).setAlpha(1f);
        startService(new Intent(this, UpdateService.class));
        super.onResume();
    }
    public static RelativeLayout getBackground(){
        return rl;
    }
    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null) {
            switch (requestCode) {
                case GOOGLE_ACCOUNT_SIGN_IN_CODE:
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    if (result.isSuccess()) {
                        // Signed in successfully, show authenticated UI.
                        GoogleSignInAccount acct = result.getSignInAccount();
                        userEmail=acct.getEmail();
                    }
                    break;
            }
        }
    }

    //handles permission requests
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions,int[] grantResults){
        switch (permsRequestCode){
            case CALENDAR_PERMS:
                if (grantResults.length>0
                        && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    Log.d("calEvent", "perms granted");
                }
                break;
        }
    }

    @Override
    public void onStart() {
        if (userEmail==null && isOnline()) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, GOOGLE_ACCOUNT_SIGN_IN_CODE);
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        mGoogleApiClient.disconnect();
        startService(new Intent(this, UpdateService.class));
    }
}
