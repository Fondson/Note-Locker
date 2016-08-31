package com.dev.fondson.NoteLocker;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.app.WallpaperManager;
import android.app.backup.BackupManager;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //public static final HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
    public static DBAdapter db;
    public static String userEmail;
    public static KeyListener listener;
    public static final int WALLPAPER_CODE = 10;
    public static final int GOOGLE_ACCOUNT_SIGN_IN_CODE = 9;
    public static String WALLPAPER_PATH;
    public static String WALLPAPER_FULL_PATH;
    public static String[] perms={"android.permission.READ_EXTERNAL_STORAGE"};//,"android.permission.WRITE_EXTERNAL_STORAGE","android.permision.READ_INTERNAL_STORAGE","android.permission.WRITE_INTERNAL_STORAGE"};
    private static RelativeLayout rl;
    private static ImageView darkTint;
    public static GoogleApiClient mGoogleApiClient;
    private EditText etInput;
    private static LinearLayout ll;
    private ArrayList<ArrayList<Item>> state;
    private ArrayList<Item> itemArr;
    private ArrayList<Item> completedItemsArr;
    private ExpandableListView expandableListView;
    private ItemsAdapter itemsAdapter;
    private UnlockBar unlock;
    private SlidrConfig config;
    private SlidrInterface slidrInterface;


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

        //open database and create tables
        db=new DBAdapter(this);
        db.open();

        itemArr = new ArrayList<Item>();
        completedItemsArr =new ArrayList<Item>();
        db.switchTable(DBAdapter.DATABASE_TABLE_ITEMS);
        getAllItems(db.getAllRows(),itemArr);
        db.switchTable(DBAdapter.DATABASE_TABLE_COMPLETED_ITEMS);
        getAllItems(db.getAllRows(),completedItemsArr);
        state = new ArrayList<ArrayList<Item>>();

        expandableListView=(ExpandableListView) findViewById(R.id.exlvItems);
        state.add(itemArr);
        state.add(completedItemsArr);
        itemsAdapter = new ItemsAdapter(this,state);
        expandableListView.setAdapter(itemsAdapter);
        itemsAdapter.notifyDataSetChanged();

        introCheck();

        rl = (RelativeLayout) findViewById(R.id.rl);
        //set initial wallpaper
        WALLPAPER_PATH = getFilesDir().getAbsolutePath();
        WALLPAPER_FULL_PATH = WALLPAPER_PATH + "/wallpaper.jpg";
        File wallpaperFile= new File(WALLPAPER_FULL_PATH);

        if(wallpaperFile.exists()
                && this.checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE")== PackageManager.PERMISSION_GRANTED) {
//            Bitmap bitmap = BitmapFactory.decodeFile(wallpaperFile.getAbsolutePath());
//            bitmap =Bitmap.createScaledBitmap(bitmap, 2048, 2048, true);
            Drawable wallpaper=Drawable.createFromPath(WALLPAPER_FULL_PATH);// BitmapDrawable(getResources(), bitmap);
            rl.setBackground(wallpaper);
        }
        else{
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            rl.setBackground(wallpaperDrawable);
        }
//        rl.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                hideKeyboard();
//                fullScreencall();
//                return false;
//            }
//        });
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
                                                      itemArr.add(0, new Item(newId, etInput.getText().toString().trim(), false));
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

        // Retrieve layout elements
//        unlock = (UnlockBar) findViewById(R.id.unlock);
//         Attach listener
//        unlock.setOnUnlockListener(new UnlockBar.OnUnlockListener() {
//            @Override
//            public void onUnlock() {
//                moveTaskToBack(true);
//            }
//        });
//
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


    public static void getAllItems(Cursor cursor,ArrayList<Item> arrayList){;
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

                arrayList.add(0,new Item(id,item,selected));
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
        //homeKeyLocker.unlock();
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
        //homeKeyLocker.lock(this);
        //unlock.reset();
        ((EditText) findViewById(R.id.editText)).setText("");
        itemsAdapter.notifyDataSetChanged();

        ((View)findViewById(R.id.slidable_content)).setAlpha(1f);
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
