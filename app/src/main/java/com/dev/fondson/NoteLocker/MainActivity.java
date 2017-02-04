package com.dev.fondson.NoteLocker;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
//import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Calendar;
import java.util.ListIterator;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    public static final int WALLPAPER_CODE = 10;
    public static final int GOOGLE_ACCOUNT_SIGN_IN_CODE = 9;
    public static final int FIREBASE_MESSAGE_CODE = 1;
    static final int MIN_DISTANCE = 150;
    public static boolean transfer = false;
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
    public static FirebaseAuth mAuth;
    private static FirebaseDatabase firebaseDatabase;
    private static DatabaseReference toDoDatabase;
    private static DatabaseReference completedDatabase;
    public static Handler mHandler;
    private View slideUpView;
    public static boolean loggingOut;
    private static DragLinearLayout dragll;
    public static ItemListAdapter itemsAdapter;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText etInput;
    private boolean firstLogIn = false;
    private LinkedList<ItemList> itemsList;
    private LinkedList<CalendarItem> calendarItemArr;
    private LinkedList<UserItem> userItemsList;
    private LinkedList<UserItem> completedItemsList;
    //private ExpandableListView expandableListView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private Calendar beginTime;
    private String pastUser;

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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
        //getWindow().setStatusBarColor(Color.TRANSPARENT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up firebase authentication
        firebaseDatabase = Firebase.getDatabaseInstance();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                Log.d("firebasetag", "AM I EVEN CALLED?");
                if (user != null && pastUser!=userEmail) {
                    pastUser=userEmail;
                    // User is signed in
                    Log.d("firebasetag", "onAuthStateChanged:signed_in:" + user.getUid());
                    if (userEmail != null) {
                        try {
                            // firebase database
                            toDoDatabase = Firebase.getToDoRef();
                            completedDatabase = Firebase.getCompletedRef();
                            if (userItemsList == null || completedItemsList == null) {
                                Log.d("setupitem", "auth ");
                                setUpItemList();
                            }
                            userItemsList.clear();
                            completedItemsList.clear();
                            itemsAdapter.notifyParentDataSetChanged(true);
                            setUpToDoListener();
                            setUpCompletedListener();
                        } catch (Exception e) {
                            Log.d("exception", e.getMessage());
                        }
                    }
                } else {
                    // User is signed out
                    Log.d("firebasetag", "onAuthStateChanged:signed_out");
                }
            }
        };

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onStart();
                swipeContainer.setRefreshing(false);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        rl = (RelativeLayout) findViewById(R.id.rl);

        ll = (LinearLayout) findViewById(R.id.llMain);
        ll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                fullScreencall();
                return false;
            }
        });
        slideUpView = findViewById(R.id.slide_up_shimmer);
        dragll = (DragLinearLayout) findViewById(R.id.dragll);
        dragll.setViewDraggable(ll, slideUpView);

        // get existing wallpaper
        WALLPAPER_PATH = getFilesDir().getAbsolutePath();
        WALLPAPER_FULL_PATH = WALLPAPER_PATH + "/wallpaper.jpg";
        File wallpaperFile= new File(WALLPAPER_FULL_PATH);
        Drawable wallpaper;
        if(wallpaperFile.exists()
                && this.checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED) {
            wallpaper=Drawable.createFromPath(WALLPAPER_FULL_PATH);
            getBackground().setBackground(wallpaper);
        }
        //set default wallpaper
        else{
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            wallpaper = wallpaperManager.getDrawable();
            rl.setBackground(wallpaper);
        }
        new PaletteTask().execute(drawableToBitmap(wallpaper));
        darkTint=(ImageView)findViewById(R.id.ivDarkTint);
        ((View)darkTint).setAlpha((float)PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getInt("pref_key_darkTint", 50)/100);
        float scale = getResources().getDisplayMetrics().density;

        //****************************************************************************************************
        int dpAsPixels = (int) (16 * scale + 0.5f); //standard padding by Android Design Guidelines
        ll.setPadding(dpAsPixels, dpAsPixels + getStatusBarHeight(), dpAsPixels, 0);
        //****************************************************************************************************

        etInput = (EditText) findViewById(R.id.editText);
        etInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                                              @Override
                                              public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
                                                  if (!(etInput.getText().toString().trim().matches(""))) {
                                                      Firebase.writeNewToDoItem(etInput.getText().toString().trim(), false);
                                                      etInput.setText("");
                                                  }
                                                  return true;

                                              }
                                          }
        );
        etInput.setOnClickListener(new EditText.OnClickListener() {
            @Override
            public void onClick(View view) {
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

//        // set up slide up interface
//        config = new SlidrConfig.Builder()
//                                .position(SlidrPosition.BOTTOM)
//                                .sensitivity(0.75f)
//                                .scrimColor(Color.TRANSPARENT)
//                                .scrimStartAlpha(1f)
//                                .scrimEndAlpha(0f)
//                                .velocityThreshold(2400)
//                                .distanceThreshold(0.25f)
//                                .edge(true)
//                                .edgeSize(0.18f) // The % of the screen that counts as the edge, default 18%
//                                .build();
//        slidrInterface = Slidr.attach(this, config);

        // facebook shimmer effect for slide up text
        ShimmerFrameLayout slideUpShimmer = (ShimmerFrameLayout) slideUpView;
        slideUpShimmer.setDuration(1500);
        slideUpShimmer.startShimmerAnimation();

        // experimental feature to regularly restart service
        Intent ishintent = new Intent(this, UpdateService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, ishintent, 0);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);
        alarm.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000 * 60 * 20, pintent);

        // handles firebase crowd messages/alerts
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if (message.what == FIREBASE_MESSAGE_CODE) {
                    Toast.makeText(getApplicationContext(), (String)message.obj,
                            Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private void doIntro(final SharedPreferences getPrefs){
        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                firstLogIn = true;
                //  Launch app intro
                Intent i = new Intent(MainActivity.this, Intro.class);
                startActivity(i);
                //  Make a new preferences editor
                SharedPreferences.Editor e = getPrefs.edit();
                //  Edit preference to make it false because we don't want this to run again
                e.putBoolean("firstStart", false);
                //  Apply changes
                e.apply();
            }
        });
        // Start the thread
        t.start();
    }

    public static void requestBackup(Context context) {
        BackupManager bm = new BackupManager(context);
        bm.dataChanged();
    }

    private void setUpToDoListener(){
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("todochild", "onChildAdded:" + dataSnapshot.getKey());
                // A new todo item has been added, add it to the displayed list
                userItemsList.add(0, dataSnapshot.getValue(UserItem.class));
                itemsAdapter.notifyChildInserted(ItemList.TODO, 0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("todochild", "onChildChanged:" + dataSnapshot.getKey());
                UserItem newItem = dataSnapshot.getValue(UserItem.class);
                changeToDoItem(newItem.getKey(), newItem.getName(), newItem.isSelected());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("todochild", "onChildRemoved:" + dataSnapshot.getKey());
                removeToDoItem(dataSnapshot.getValue(UserItem.class).getKey());
                //itemsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("todochild", "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("todochild", "postComments:onCancelled", databaseError.toException());
                if (!loggingOut) {
                    Toast.makeText(getApplicationContext(), "Something went wrong, please login again and wait a few seconds",
                            Toast.LENGTH_SHORT).show();
                }
            }

            private void removeToDoItem(String key){
                ListIterator<UserItem> iterator = userItemsList.listIterator();
                while (iterator.hasNext()){
                    if (key.equals(iterator.next().getKey())){
                        final int index = iterator.nextIndex() - 1;
                        iterator.remove();
                        itemsAdapter.notifyChildRemoved(ItemList.TODO, index);
                        break;
                    }
                }
            }

            private void changeToDoItem(String key, String name, Boolean selected){
                ListIterator<UserItem> iterator = userItemsList.listIterator();
                while (iterator.hasNext()){
                    UserItem item = iterator.next();
                    if (key.equals(item.getKey())){
                        item.setName(name);
                        item.setSelected(selected);
                        itemsAdapter.notifyChildChanged(ItemList.TODO, iterator.nextIndex() - 1);
                        break;
                    }
                }
            }
        };
        toDoDatabase.addChildEventListener(childEventListener);
    }

    private void setUpCompletedListener(){
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("completedchild", "onChildAdded:" + dataSnapshot.getKey());
                // A new todo item has been added, add it to the displayed list
                completedItemsList.add(0, dataSnapshot.getValue(UserItem.class));
                itemsAdapter.notifyChildInserted(ItemList.COMPLETED, 0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("completedchild", "onChildChanged:" + dataSnapshot.getKey());
                UserItem newItem = dataSnapshot.getValue(UserItem.class);
                changeCompletedItem(newItem.getKey(), newItem.getName(), newItem.isSelected());
                //itemsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("completedchild", "onChildRemoved:" + dataSnapshot.getKey());
                removeCompletedItem(dataSnapshot.getValue(UserItem.class).getKey());
                //itemsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("completedchild", "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("completedchild", "postComments:onCancelled", databaseError.toException());
                if (!loggingOut) {
                    Toast.makeText(getApplicationContext(), "Something went wrong, please login again and wait a few seconds",
                            Toast.LENGTH_SHORT).show();
                }
            }

            private void removeCompletedItem(String key){
                ListIterator<UserItem> iterator = completedItemsList.listIterator();
                while (iterator.hasNext()){
                    if (key.equals(iterator.next().getKey())){
                        final int index = iterator.nextIndex() - 1;
                        iterator.remove();
                        itemsAdapter.notifyChildRemoved(ItemList.COMPLETED, index);
                        break;
                    }
                }
            }

            private void changeCompletedItem(String key, String name, Boolean selected){
                ListIterator<UserItem> iterator = completedItemsList.listIterator();
                while (iterator.hasNext()){
                    UserItem item = iterator.next();
                    if (key.equals(item.getKey())){
                        item.setName(name);
                        item.setSelected(selected);
                        itemsAdapter.notifyChildChanged(ItemList.COMPLETED, iterator.nextIndex() - 1);
                        break;
                    }
                }
            }
        };
        completedDatabase.addChildEventListener(childEventListener);
    }

    private void getCalendarEvents(LinkedList<CalendarItem> calendarItems){
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.PREF_KEY_CALENDAR,false)
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

    private void setUpItemList(){
        Log.d("setupitem", "setUpItemList: " + String.valueOf(firstLogIn));
        userItemsList = new LinkedList<UserItem>();
        completedItemsList =new LinkedList<UserItem>();
        //calendarItemArr = new LinkedList<CalendarItem>();
        //beginTime = Calendar.getInstance();
        //getCalendarEvents(calendarItemArr);
        itemsList = new LinkedList<ItemList>();
        //itemsArray.add(calendarItemArr);
        itemsList.add(new ItemList("To Do", userItemsList));
        itemsList.add(new ItemList("Completed", completedItemsList));
        recyclerView = (RecyclerView) findViewById(R.id.itemList);
        itemsAdapter = new ItemListAdapter(this, itemsList);
        recyclerView.setAdapter(itemsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        itemsAdapter = new ItemsAdapter(this,itemsArray);
//        expandableListView=(ExpandableListView) findViewById(R.id.exlvItems);
//        expandableListView.setAdapter(itemsAdapter);
//        expandableListView.expandGroup(itemsAdapter.CALENDAR);
//        expandableListView.expandGroup(itemsAdapter.NOT_COMPLETED);
        if (firstLogIn) {
//            expandableListView.expandGroup(itemsAdapter.COMPLETED);
            Firebase.writeNewToDoItem("Check me to move me to the Completed list.", true);
            Firebase.writeNewCompletedItem("Uncheck me to move me to the To do list or press the \"X\" to permanently delete me.",true);
        }
//        itemsAdapter.notifyDataSetChanged();
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
        super.onResume();
        fullScreencall();
        hideKeyboard();
//        if (userEmail != null && calendarItemArr != null) {
//            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.PREF_KEY_CALENDAR, false)
//                    && checkCallingOrSelfPermission("android.permission.READ_CALENDAR") == PackageManager.PERMISSION_GRANTED
//                    && checkCallingOrSelfPermission("android.permission.WRITE_CALENDAR") == PackageManager.PERMISSION_GRANTED) {
//                if (calendarItemArr.size() == 0) {
//                    //getCalendarEvents(calendarItemArr);
//                    //itemsAdapter.notifyDataSetChanged();
//                    //expandableListView.expandGroup(itemsAdapter.CALENDAR);
//                } else {
//                    Calendar currentTime = Calendar.getInstance();
//                    currentTime.getTime();
//                    currentTime.set(Calendar.HOUR_OF_DAY, 0);
//                    currentTime.set(Calendar.MINUTE, 0);
//                    currentTime.set(Calendar.SECOND, 0);
//                    currentTime.set(Calendar.MILLISECOND, 1);
//                    if (beginTime.compareTo(currentTime) != 0) {
//                        Log.d("calEvent", String.valueOf(beginTime.compareTo(currentTime)));
//                        calendarItemArr.clear();
//                        //getCalendarEvents(calendarItemArr);
//                        //itemsAdapter.notifyDataSetChanged();
//                    }
//                }
//                //itemsAdapter.notifyDataSetChanged();
//            }
//        }
        ((EditText) findViewById(R.id.editText)).setText("");
        startService(new Intent(this, UpdateService.class));
    }

    public static ViewGroup getBackground(){
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

    public Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
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
                    SharedPreferences getPrefs = PreferenceManager
                            .getDefaultSharedPreferences(getBaseContext());
                    Log.d("mainActivityResult", "result :" + String.valueOf(result.isSuccess()));
                    if (result.isSuccess()) {
                        // Signed in successfully, show authenticated UI.
                        GoogleSignInAccount acct = result.getSignInAccount();
                        Firebase.authWithGoogle(acct, mAuth, MainActivity.this);
                        userEmail=acct.getEmail();

                        if (!userEmail.equals(getPrefs.getString("userEmail", ""))){
                            //  Make a new preferences editor
                            SharedPreferences.Editor e = getPrefs.edit();
                            //  Edit preference to make it false because we don't want this to run again
                            e.putString("userEmail", userEmail);
                            //  Apply changes
                            e.apply();
                        }
                    }
                    else {
                        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                        startActivityForResult(signInIntent, GOOGLE_ACCOUNT_SIGN_IN_CODE);
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
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        boolean isFirstStart = getPrefs.getBoolean("firstStart", true);
        if (isFirstStart && !firstLogIn) {
            doIntro(getPrefs);
        }
        else {
            if (userEmail == null && isOnline()) {
                Log.d("onStart", "online");
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, GOOGLE_ACCOUNT_SIGN_IN_CODE);
            } else if (userEmail == null) {
                Log.d("onStart", "not online");
                userEmail = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext()).getString("userEmail", null);
                Log.d("setupitem", "start ");
                setUpItemList();
            }
            mAuth.addAuthStateListener(mAuthListener);
        }
        hideKeyboard();
        super.onStart();
    }

    @Override
    public void onStop() {
        hideKeyboard();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        startService(new Intent(this, UpdateService.class));
    }
}
