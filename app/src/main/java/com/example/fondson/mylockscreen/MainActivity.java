package com.example.fondson.mylockscreen;

import android.Manifest;
import android.app.Service;
import android.app.WallpaperManager;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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

import io.github.homelocker.lib.HomeKeyLocker;

public class MainActivity extends AppCompatActivity {

    //public static final HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
    private ListView lv;
    private RelativeLayout rl;
    private LinearLayout ll;
    private ArrayList<Item> itemArr;
    private CustomAdapter adapter;
    public static String fileName = "items.txt";
    private KeyListener listener;
    private String[] perms={"android.permission.READ_EXTERNAL_STORAGE","android.permission.WRITE_EXTERNAL_STORAGE"};
    private Uri selectedImageUri;
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

        //set initial wallpaper
        File wallpaperFile= new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/.notelocker/wallpaper.jpg");
        if(wallpaperFile.exists()) {
            Drawable wallpaper=Drawable.createFromPath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/.notelocker/wallpaper.jpg");
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
        adapter = new CustomAdapter(this, itemArr);
        lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(adapter);
        repopulateList();
        adapter.notifyDataSetChanged();
        final EditText etInput = (EditText) findViewById(R.id.editText);
        etInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                                              @Override
                                              public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
                                                  if (arg1 == EditorInfo.IME_ACTION_NEXT && !(etInput.getText().toString().trim().matches(""))) {
                                                      writeFile(etInput.getText().toString().trim());
                                                      itemArr.add(0, new Item(etInput.getText().toString().trim(), false));
                                                      adapter.notifyDataSetChanged();
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
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int pos, long id) {
                // TODO Auto-generated method stub
                //homeKeyLocker.unlock();
                final Item item = (Item) arg0.getItemAtPosition(pos);
                final EditText editText = (EditText) arg1;
                editText.setKeyListener(listener);
                editText.requestFocus();
                editText.setSelection(editText.getText().length());

                InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
                editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                                                       @Override
                                                       public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
                                                           if (!(editText.getText().toString().trim().matches(item.getName())) && !(editText.getText().toString().trim().matches(""))) {
                                                               replaceFile(item.getName(), editText.getText().toString().trim());
                                                               //String pastName = item.getName();
                                                               item.setName(editText.getText().toString().trim());
                                                               //Toast.makeText(MainActivity.this, pastName + " changed to " + item.getName(), Toast.LENGTH_SHORT).show();
                                                           }
                                                           editText.setKeyListener(null);
                                                           hideKeyboard();
                                                           editText.setText(item.getName());
                                                           return true;
                                                       }
                                                   }
                );
                return true;
            }
        });
        // Retrieve layout elements
        UnlockBar unlock = (UnlockBar) findViewById(R.id.unlock);
        // Attach listener
        unlock.setOnUnlockListener(new UnlockBar.OnUnlockListener() {
            @Override
            public void onUnlock() {
                moveTaskToBack(true);
            }
        });
        ImageButton btnSettings=(ImageButton) findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //requests permissions needed for users to select background image
                requestPermissions(perms, 200);
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

    public void writeFile(String item) {
        try {
            FileOutputStream fileOutputStream = openFileOutput(fileName, MODE_APPEND);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(item);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void repopulateList() {
        try {
            String item;
            FileInputStream fileInputStream = openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            while ((item = bufferedReader.readLine()) != null) {
                itemArr.add(0, new Item(item, false));
            }
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //handles permission requests
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions,int[] granResults){
        switch (permsRequestCode){
            //launches intent for user to select image from gallery
            case 200:
//                Intent intent = new Intent(
//                        Intent.ACTION_PICK,
//                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                intent.setType("image/*");
//                startActivityForResult(
//                        Intent.createChooser(intent, "Select File"),
//                        1);
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.putExtra("crop", "true");
                startActivityForResult(photoPickerIntent, 1);
                break;
        }
    }

    //handles picture-cropping results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Uri selectedImageUri = data.getData();
//        String[] projection = { MediaStore.MediaColumns.DATA};
//        CursorLoader cursorLoader = new CursorLoader(this,selectedImageUri, projection, null, null,
//                null);
//        Cursor cursor =cursorLoader.loadInBackground();
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
//        cursor.moveToFirst();
//        String selectedImagePath = cursor.getString(column_index);
//        Drawable d = Drawable.createFromPath(selectedImagePath);
//        rl.setBackground(d);
        if (data!=null) {
            selectedImageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap selectedBitmap = BitmapFactory.decodeStream(imageStream);
//                Bundle extras = data.getExtras();
//                Bitmap selectedBitmap = extras.getParcelable("data");
                Drawable d = new BitmapDrawable(getResources(), selectedBitmap);
                rl.setBackground(d);
                moveFile(selectedImageUri.getPath(),"wallpaper.jpg",Environment.getExternalStorageDirectory().getAbsolutePath()+"/.notelocker/");
                //deleteFileFromMediaStore(getContentResolver(), new File(selectedImageUri.getPath()));
                imageStream.close();
            }
            catch (Exception e){}
//            try {
//                FileOutputStream fileOutputStream = openFileOutput("wallpaper.txt",MODE_PRIVATE);
//                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
//                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
//                bufferedWriter.write(selectedImageUri.getPath());
//                bufferedWriter.close();
//            }
//            catch(Exception e){}
        }
    }

    private void moveFile(String inputPath, String inputFile,String outputPath) {
//        try {
//
//            File from = new File(inputPath);
//            File to = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/.notelocker/"+inputFile);
//            from.renameTo(to);
//            Toast.makeText(this, "hi i moved",
//                    Toast.LENGTH_LONG).show();
//
//        }
//        catch (Exception e) {
//            Toast.makeText(this, "hi i throw exceptions",
//                    Toast.LENGTH_LONG).show();
//        }
        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputPath).delete();
            deleteFileFromMediaStore(getContentResolver(),new File(inputPath));

        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    public static void deleteFileFromMediaStore(final ContentResolver contentResolver, final File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        final Uri uri = MediaStore.Files.getContentUri("external");
        final int result = contentResolver.delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[]{canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
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
        adapter.notifyDataSetChanged();
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

    public void replaceFile(String item, String itemReplace) {
        try {
            FileOutputStream fileOutputStream = this.openFileOutput("myTempFile.txt", this.MODE_APPEND);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter writer = new BufferedWriter(outputStreamWriter);

            FileInputStream fileInputStream = this.openFileInput(MainActivity.fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String lineToReplace = item;
            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if (trimmedLine.equals(lineToReplace)) {
                    writer.write(itemReplace);
                    writer.newLine();
                    continue;
                }
                writer.write(currentLine);
                writer.newLine();
            }
            writer.close();
            reader.close();
            (new File(this.getFilesDir() + "/" + MainActivity.fileName)).delete();
            File file = new File(this.getFilesDir() + "/" + "myTempFile.txt");
            file.renameTo(new File(this.getFilesDir() + "/" + MainActivity.fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
