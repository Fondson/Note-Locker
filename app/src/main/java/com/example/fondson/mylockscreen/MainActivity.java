package com.example.fondson.mylockscreen;

import android.Manifest;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

import io.github.homelocker.lib.HomeKeyLocker;

public class MainActivity extends AppCompatActivity {

    //public static final HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
    public static DBAdapter db;
    public static KeyListener listener;
    private EditText etInput;
    private String WALLPAPER_PATH;
    private RelativeLayout rl;
    private LinearLayout ll;
    private ArrayList<ArrayList<Item>> state;
    private ArrayList<Item> itemArr;
    private ArrayList<Item> completedItemsArr;
    private String[] perms={"android.permission.READ_EXTERNAL_STORAGE","android.permission.WRITE_EXTERNAL_STORAGE"};
    private ExpandableListView expandableListView;
    private ItemsAdapter itemsAdapter;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rl = (RelativeLayout) findViewById(R.id.rl);

        //open database and create tables
        db=new DBAdapter(this);
        db.open();

        //set initial wallpaper
        WALLPAPER_PATH=Environment.getExternalStorageDirectory().getAbsolutePath()+"/.notelocker/wallpaper.jpg";
        File wallpaperFile= new File(WALLPAPER_PATH);
        if(wallpaperFile.exists()) {
            Drawable wallpaper=Drawable.createFromPath(WALLPAPER_PATH);
            rl.setBackground(wallpaper);
        }
        else{
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            rl.setBackground(wallpaperDrawable);
        }

        rl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
        ll = (LinearLayout) findViewById(R.id.llMain);
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (16 * scale + 0.5f); //standard padding by Android Design Guidelines
        ll.setPadding(dpAsPixels, dpAsPixels + getStatusBarHeight(), dpAsPixels, dpAsPixels);
        startService(new Intent(this, UpdateService.class));


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
        expandableListView.expandGroup(0);
        itemsAdapter.notifyDataSetChanged();


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
        UnlockBar unlock = (UnlockBar) findViewById(R.id.unlock);
        // Attach listener
        unlock.setOnUnlockListener(new UnlockBar.OnUnlockListener() {
            @Override
            public void onUnlock() {
                moveTaskToBack(true);
            }
        });

        //settings button
        ImageButton btnSettings=(ImageButton) findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //requests permissions needed for users to select background image on M or above
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    requestPermissions(perms, 200);
                }
                else{launchGalleryPicker();}
            }
        });
        /*final SeekBar sb = (SeekBar) findViewById(R.id.seekBar);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() > 80) {
                } else {
                    seekBar.setThumb(getResources().getDrawable(R.mipmap.ic_launcher));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 80) {
                    moveTaskToBack(true);

                }
            }
        });
        */
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void launchGalleryPicker(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra("crop", "true");
        photoPickerIntent.putExtra("output", Uri.fromFile(new File(WALLPAPER_PATH)));
        startActivityForResult(photoPickerIntent, 1);
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


    //handles permission requests
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions,int[] granResults){
        switch (permsRequestCode){
            //launches intent for user to select image from gallery
            case 200:
                launchGalleryPicker();
                break;
        }
    }

    //handles picture-cropping results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null) {
            if (requestCode==1){
                rl.setBackground(Drawable.createFromPath(WALLPAPER_PATH));
            }
        }
    }

    // Don't finish Activity on Back press
    @Override
    public void onBackPressed() {
        return;
    }

    protected void onPause() {
        hideKeyboard();
        //homeKeyLocker.unlock();
        startService(new Intent(this, UpdateService.class));
        super.onPause();
    }

    protected void onResume() {
        //homeKeyLocker.lock(this);
        ((EditText) findViewById(R.id.editText)).setText("");
        itemsAdapter.notifyDataSetChanged();
        UnlockBar unlock = (UnlockBar) findViewById(R.id.unlock);
        unlock.reset();
        //final SeekBar sb = (SeekBar) findViewById(R.id.seekBar);
        //sb.setProgress(0);
        super.onResume();
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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.fondson.mylockscreen/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.fondson.mylockscreen/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
