package com.dev.fondson.NoteLocker;

import com.joestelmach.natty.*;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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
import java.util.Date;
import java.util.LinkedList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ItemPickerDialogFragment.OnItemSelectedListener{
    public static final int WALLPAPER_CODE = 10;
    public static final int GOOGLE_ACCOUNT_SIGN_IN_CODE = 9;
    public static final int FIREBASE_MESSAGE_CODE = 1;
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
    public static LinkedList<ItemList> itemsList;
    public static LinkedList<UserItem> userItemsList;
    public static LinkedList<UserItem> completedItemsList;
    private ItemRecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private Calendar beginTime;
    private String pastUser;
    private SharedPreferences sharedPreferences;

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
    private boolean quickUnlock = false;
    private Resources resources;
    private ChildEventListener toDoChildEventListener;
    private ChildEventListener completedChildEventListener;
    private ItemTouchHelper touchHelper;
    private ItemTouchHelper.Callback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
//                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
        //getWindow().setStatusBarColor(Color.TRANSPARENT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        resources = getResources();

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
                    // firebase database
                    toDoDatabase = Firebase.getToDoRef();
                    completedDatabase = Firebase.getCompletedRef();
                    initialDataLoad();
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

        recyclerView = (ItemRecyclerView) findViewById(R.id.itemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setUpItemList();
                initialDataLoad();
                onStart();
                if (itemsAdapter != null) itemsAdapter.notifyParentDataSetChanged(true);
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
        quickUnlock = sharedPreferences.getBoolean("pref_key_quick_unlock", false);
        dragll = (DragLinearLayout) findViewById(R.id.dragll);
        setUpUnlock();

        // get existing wallpaper
        WALLPAPER_PATH = getFilesDir().getAbsolutePath();
        WALLPAPER_FULL_PATH = WALLPAPER_PATH + "/wallpaper.jpg";
        File wallpaperFile= new File(WALLPAPER_FULL_PATH);
        Drawable wallpaper;
        darkTint=(ImageView)findViewById(R.id.ivDarkTint);
        if(wallpaperFile.exists()
                && this.checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED) {
            wallpaper=Drawable.createFromPath(WALLPAPER_FULL_PATH);
            darkTint.setImageDrawable(wallpaper);
        }
        //set default wallpaper
        else{
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            wallpaper = wallpaperManager.getDrawable();
            darkTint.setImageDrawable(wallpaper);
        }
        new PaletteTask().execute(drawableToBitmap(wallpaper));
        darkTint.setAlpha(1f - (float)sharedPreferences.getInt("pref_key_darkTint", resources.getInteger(R.integer.DARK_TINT_DEFAULT))/100);

        etInput = (EditText) findViewById(R.id.editText);
        etInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                                              @Override
                                              public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
                                                  if (!(etInput.getText().toString().trim().equals(""))) {
                                                      String text = etInput.getText().toString().trim();
                                                      String key = Firebase.writeNewToDoItem(text, false);
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

    private void initialDataLoad(){
        if (userEmail != null) {
            try {
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
        if (toDoChildEventListener != null) toDoDatabase.removeEventListener(toDoChildEventListener);
        toDoChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                UserItem item = dataSnapshot.getValue(UserItem.class);
                Log.d("todochild", "onChildAdded:" + dataSnapshot.getKey());
                // A new todo item has been added, add it to the displayed list
                userItemsList.add(0, item);
                try {
                    itemsAdapter.notifyChildInserted(ItemList.TODO, 0);
                }catch (Exception e){
                    itemsAdapter.notifyParentChanged(ItemList.TODO);
                }
                long diff = item.notification.getTime() - Calendar.getInstance().getTime().getTime();
                if (diff > 0){
                    item.notification.scheduleNotification(getApplicationContext(), item,
                            item.notification.getNotification(getApplicationContext(), item), diff);
                    item.notification.setDateTimeString(
                            new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(item.notification.getTime())
                                    + " - " + DateFormat.getTimeInstance(DateFormat.SHORT).format(item.notification.getTime()));
                    changeToDoItem(item.getKey());
                }else if (item.notification.getTime() != -1) {
                    Log.d("todochildtime", "onChildAdded:" + String.valueOf(item.notification.getTime()));
                    checkNotif(item);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("todochild", "onChildChanged:" + dataSnapshot.getKey());
                UserItem newItem = dataSnapshot.getValue(UserItem.class);
                if (!ItemListAdapter.MOVING) changeToDoItem(newItem.getKey());
                //checkNotif(newItem);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("todochild", "onChildRemoved:" + dataSnapshot.getKey());
                UserItem item = dataSnapshot.getValue(UserItem.class);
                removeToDoItem(item.getKey());
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
                        try {
                            itemsAdapter.notifyChildRemoved(ItemList.TODO, index);
                        }catch (Exception e){
                            itemsAdapter.notifyParentChanged(ItemList.TODO);
                        }
                        break;
                    }
                }
            }

            private void changeToDoItem(String key){
                ListIterator<UserItem> iterator = userItemsList.listIterator();
                while (iterator.hasNext()){
                    UserItem item = iterator.next();
                    if (key.equals(item.getKey())){
                        long diff = item.notification.getTime() - Calendar.getInstance().getTime().getTime();
                        if (diff > 0){
                            item.notification.setDateTimeString(
                                    new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(item.notification.getTime())
                                            + " - " + DateFormat.getTimeInstance(DateFormat.SHORT).format(item.notification.getTime()));
                        }
                        try {
                            itemsAdapter.notifyChildChanged(ItemList.TODO, iterator.nextIndex() - 1);
                            Log.d("changeToDoItemdebug", "changeToDoItem: " + String.valueOf(iterator.nextIndex() - 1)
                            + " " + item.getName() + " " + item.notification.getTime());
                        }catch (Exception e){
                            itemsAdapter.notifyParentChanged(ItemList.TODO);
                        }
                        break;
                    }
                }
            }

            private void checkNotif(UserItem item){
                Parser parser = new Parser();
                String text = item.getName();
                String key = item.getKey();
                List<DateGroup> groups = parser.parse(text);
                for(DateGroup group : groups) {
                    List<Date> dates = group.getDates();
                    Log.d("natty", "dategroup exists");
                    Date date = dates.get(0);
                    Log.d("natty", date.toString());
                    NattyDetectDialog dialog = new NattyDetectDialog();
                    Bundle bundle = new Bundle();
                    bundle.putString("key", item.getKey());
                    bundle.putLong("time", date.getTime());

                    dialog.setArguments(bundle);

                    dialog.show(getSupportFragmentManager(), "tag");
                }
            }
        };
        toDoDatabase.addChildEventListener(toDoChildEventListener);
    }

    private void setUpCompletedListener(){
        if (completedChildEventListener != null) completedDatabase.removeEventListener(completedChildEventListener);
        completedChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("completedchild", "onChildAdded:" + dataSnapshot.getKey() + " " + dataSnapshot.getValue(UserItem.class).getKey());
                // A new todo item has been added, add it to the displayed list
                completedItemsList.add(0, dataSnapshot.getValue(UserItem.class));
                try {
                    itemsAdapter.notifyChildInserted(ItemList.COMPLETED, 0);
                }catch (Exception e){
                    itemsAdapter.notifyParentChanged(ItemList.COMPLETED);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("completedchild", "onChildChanged:" + dataSnapshot.getKey());
                UserItem newItem = dataSnapshot.getValue(UserItem.class);
                if (!ItemListAdapter.MOVING) changeCompletedItem(newItem.getKey());
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
                        try {
                            itemsAdapter.notifyChildRemoved(ItemList.COMPLETED, index);
                        }catch (Exception e){
                            itemsAdapter.notifyParentChanged(ItemList.COMPLETED);
                        }
                        break;
                    }
                }
            }

            private void changeCompletedItem(String key){
                ListIterator<UserItem> iterator = completedItemsList.listIterator();
                while (iterator.hasNext()){
                    UserItem item = iterator.next();
                    if (key.equals(item.getKey())){
                        try {
                            itemsAdapter.notifyChildChanged(ItemList.COMPLETED, iterator.nextIndex() - 1);
                        }catch (Exception e){
                            itemsAdapter.notifyParentChanged(ItemList.COMPLETED);
                        }
                        break;
                    }
                }
            }
        };
        completedDatabase.addChildEventListener(completedChildEventListener);
    }

    private void setUpItemList(){
        Log.d("setupitem", "setUpItemList: " + String.valueOf(firstLogIn));
        userItemsList = new LinkedList<UserItem>();
        completedItemsList =new LinkedList<UserItem>();
        itemsList = new LinkedList<ItemList>();
        itemsList.add(new ItemList("To Do", userItemsList));
        itemsList.add(new ItemList("Completed", completedItemsList));
        itemsAdapter = new ItemListAdapter(this, itemsList);
        recyclerView.setAdapter(itemsAdapter);
        callback = new SimpleItemTouchHelperCallback(itemsAdapter);
        if (touchHelper != null) touchHelper.attachToRecyclerView(null);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
        if (firstLogIn && sharedPreferences.getBoolean("firstStart", true)) {
            Firebase.writeNewToDoItem("Check me to move me to the Completed list.", true);
            Firebase.writeNewCompletedItem("Uncheck me to move me to the To do list or press the \"X\" to permanently delete me.",true);
        }
    }

    private void setUpUnlock(){
        TextView unlockView = (TextView) findViewById(R.id.slide_up_label);
        if (!quickUnlock){
            unlockView.setText(R.string.slide_up);
            slideUpView.setOnClickListener(null);
            dragll.setViewDraggable(ll, slideUpView);
        }else{
            unlockView.setText(R.string.tap_unlock);
            slideUpView.setOnTouchListener(null);
            slideUpView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    moveTaskToBack(true);
                }
            });
        }
        if (itemsAdapter != null) itemsAdapter.notifyParentDataSetChanged(true);
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
        startService(new Intent(this, UpdateService.class));
    }

    public static ImageView getBackground(){
        return darkTint;
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

    public static Bitmap drawableToBitmap (Drawable drawable) {
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
                    Log.d("mainActivityResult", "result :" + String.valueOf(result.isSuccess()));
                    if (result.isSuccess()) {
                        // Signed in successfully, show authenticated UI.
                        GoogleSignInAccount acct = result.getSignInAccount();
                        Firebase.authWithGoogle(acct, mAuth, MainActivity.this);
                        userEmail=acct.getEmail();

                        if (!userEmail.equals(sharedPreferences.getString("userEmail", ""))){
                            //  Make a new preferences editor
                            SharedPreferences.Editor e = sharedPreferences.edit();
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
    public void onItemSelected(ItemPickerDialogFragment fragment, ItemPickerDialogFragment.Item item, int index) {
        String selectedValue = item.getStringValue();
        Log.d("itempickerdialog", "onItemSelected: " + selectedValue);

        Bundle bund = fragment.getArguments();
        String key = bund.getString("key");
        bund.putLong("time", Calendar.getInstance().getTime().getTime());

        UserItem userItem = null;
        ListIterator<UserItem> iterator = userItemsList.listIterator();
        while (iterator.hasNext()){
            userItem = iterator.next();
            if (key.equals(userItem.getKey())){
                break;
            }
        }
        switch (selectedValue){
            case "Edit":
                showTextDialog(userItem);
                break;
            case "Notif":
                Intent pickTime = new Intent(this, TimePickerActivity.class);
                pickTime.putExtras(bund);
                startActivity(pickTime);
                break;
        }
    }

    private void showTextDialog(final UserItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String oldItem = item.getName();

        builder.setTitle("Change item:");
        // Inflate new view
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.edit_item, null);
        // Set up the input
        final EditText input = (EditText) viewInflated.findViewById(R.id.newItemInput);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setText(oldItem);
        input.setSelection(oldItem.length());
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Pattern.quote is used to escape special regex characters if present
                if (!(input.getText().toString().trim()).isEmpty()) {
                    String newItemName = input.getText().toString().trim();
                    item.setName(newItemName);
                    Firebase.updateToDoItem(item);
                }else{
                    Toast.makeText(getBaseContext(), "Invalid item.",
                            Toast.LENGTH_SHORT).show();
                }
                fullScreencall();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                fullScreencall();
            }
        });

        builder.show();
    }

    @Override
    public void onStart() {
        boolean isFirstStart = sharedPreferences.getBoolean("firstStart", true);
        if (isFirstStart && !firstLogIn) {
            doIntro(sharedPreferences);
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
                userEmail = sharedPreferences.getString("userEmail", null);
                Log.d("setupitem", "start ");
                setUpItemList();
            }
            mAuth.addAuthStateListener(mAuthListener);

            if (sharedPreferences.getBoolean("pref_key_quick_unlock",false) != quickUnlock){
                quickUnlock = sharedPreferences.getBoolean("pref_key_quick_unlock",false);
                setUpUnlock();
            }
        }
        etInput.setText("");
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
