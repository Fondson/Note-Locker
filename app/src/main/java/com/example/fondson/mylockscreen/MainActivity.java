package com.example.fondson.mylockscreen;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import com.example.fondson.mylockscreen.AutoStart;
import com.example.fondson.mylockscreen.UpdateService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.github.homelocker.lib.HomeKeyLocker;

public class MainActivity extends AppCompatActivity {

    public static final HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
    private ListView lv;
    private RelativeLayout rl;
    private LinearLayout ll;
    private ArrayList<Item> itemArr;
    private CustomAdapter adapter;
    public static String fileName="items.txt";
    private KeyListener listener;
    //public static SharedPreferences prefSet;
    //public static SharedPreferences.Editor prefEdit;
    //public static Set<String> itemArrString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Window window = this.getWindow();
        //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        rl = (RelativeLayout) findViewById(R.id.rl);
        rl.setBackground(wallpaperDrawable);
        rl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
        ll=(LinearLayout)findViewById(R.id.llMain);
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (16 * scale + 0.5f); //standard padding by Android Design Guidelines
        ll.setPadding(dpAsPixels,dpAsPixels+getStatusBarHeight(),dpAsPixels,dpAsPixels);
        startService(new Intent(this, UpdateService.class));
        //final LinearLayout ll = (LinearLayout)findViewById(R.id.llItems);
        //ll.setOrientation(LinearLayout.VERTICAL);
        //disableCheck();
        itemArr = new ArrayList<Item>();
        adapter=new CustomAdapter(this,itemArr);
        lv = (ListView)findViewById(R.id.listView);
        lv.setAdapter(adapter);
        //prefSet=getPreferences(0);
        //prefEdit=prefSet.edit();
        //itemArrString=prefSet.getStringSet("items", new LinkedHashSet<String>());
        //if (!itemArrString.isEmpty()){
        //    for (String item:itemArrString){
        //        itemArr.add(0, new Item(item, false));
        //    }
        //}
        repopulateList();
        adapter.notifyDataSetChanged();
        final EditText etInput = (EditText) findViewById(R.id.editText);
        etInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                                              @Override
                                              public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
                                                  if (arg1 == EditorInfo.IME_ACTION_NEXT && !(etInput.getText().toString().trim().matches(""))) {
                                                     // final CheckBox cb = new CheckBox(MainActivity.this);
                                                     // cb.setText(etInput.getText().toString().trim());
                                                     // cb.setTextSize(17);
                                                     // ll.addView(cb, 0);
                                                     //itemArrString.add(etInput.getText().toString().trim());
                                                     //prefEdit.putStringSet("item", itemArrString);
                                                     //prefEdit.commit();
                                                      writeFile(etInput.getText().toString().trim());
                                                     itemArr.add(0, new Item(etInput.getText().toString().trim(), false));
                                                     adapter.notifyDataSetChanged();
                                                     etInput.setText("");
                                                     // cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                     //     @Override
                                                     //     public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                                                     //         if (isChecked) {
                                                     //             Handler handler = new Handler();
                                                     //             handler.postDelayed(new Runnable() {
                                                     //                 @Override
                                                     //                 public void run() {
                                                     //                     ll.removeView(cb);
                                                     //                     Toast.makeText(MainActivity.this, cb.getText() + " removed.", Toast.LENGTH_SHORT).show();
                                                     //                 }
                                                     //             }, 220);
                                                     //         }
                                                     //     }
                                                     // });
                                                      return true;
                                                  }
                                                  else{
                                                      return true;
                                                  }
                                              }
                                          }
        );
        etInput.setOnClickListener(new EditText.OnClickListener() {
            @Override
            public void onClick(View view) {
                //enableCheck();
                homeKeyLocker.unlock();
                InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etInput, InputMethodManager.SHOW_FORCED);
            }
        });
        //ScrollView sv = (ScrollView)findViewById(R.id.sv);
        //sv.setOnTouchListener(new View.OnTouchListener() {
        //    long down;

        //    @Override
        //    public boolean onTouch(View v, MotionEvent event) {

        //    }
        //});
        listener=etInput.getKeyListener();
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int pos, long id) {
                // TODO Auto-generated method stub
                homeKeyLocker.unlock();
                final Item item=(Item)arg0.getItemAtPosition(pos);
                final EditText editText =(EditText)arg1;
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
                                                               String pastName = item.getName();
                                                               item.setName(editText.getText().toString().trim());
                                                               Toast.makeText(MainActivity.this, pastName + " changed to " + item.getName(), Toast.LENGTH_SHORT).show();
                                                               // cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                               //     @Override
                                                               //     public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                                                               //         if (isChecked) {
                                                               //             Handler handler = new Handler();
                                                               //             handler.postDelayed(new Runnable() {
                                                               //                 @Override
                                                               //                 public void run() {
                                                               //                     ll.removeView(cb);
                                                               //                     Toast.makeText(MainActivity.this, cb.getText() + " removed.", Toast.LENGTH_SHORT).show();
                                                               //                 }
                                                               //             }, 220);
                                                               //         }
                                                               //     }
                                                               // });
                                                           } else {
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
        final SeekBar sb = (SeekBar) findViewById(R.id.seekBar);
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
    }
    public void writeFile(String item){
        try{
            FileOutputStream fileOutputStream=openFileOutput(fileName, MODE_APPEND);
            OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter=new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(item);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void repopulateList(){
        try{
            String item;
            FileInputStream fileInputStream=openFileInput(fileName);
            InputStreamReader inputStreamReader=new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer=new StringBuffer();
            while((item=bufferedReader.readLine())!=null){
                itemArr.add(0,new Item(item,false));
            }
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Don't finish Activity on Back press
    @Override
    public void onBackPressed() {
        return;
    }
    protected void onPause() {
        hideKeyboard();
        homeKeyLocker.unlock();
        startService(new Intent(this, UpdateService.class));
        super.onPause();
    }
    protected void onResume() {
        //homeKeyLocker.lock(this);
        adapter.notifyDataSetChanged();
        final SeekBar sb = (SeekBar) findViewById(R.id.seekBar);
        sb.setProgress(0);
        //disableCheck();
        super.onResume();
    }
    public void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
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
    public void replaceFile(String item,String itemReplace){
        try{
            FileOutputStream fileOutputStream = this.openFileOutput("myTempFile.txt", this.MODE_APPEND);
            OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fileOutputStream);
            BufferedWriter writer=new BufferedWriter(outputStreamWriter);

            FileInputStream fileInputStream = this.openFileInput(MainActivity.fileName);
            InputStreamReader inputStreamReader=new InputStreamReader(fileInputStream);
            BufferedReader reader=new BufferedReader(inputStreamReader);

            String lineToReplace = item;
            String currentLine;

            while((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if(trimmedLine.equals(lineToReplace)) {
                    writer.write(itemReplace);
                    writer.newLine();
                    continue;
                }
                writer.write(currentLine);
                writer.newLine();
            }
            writer.close();
            reader.close();
            (new File(this.getFilesDir()+"/"+MainActivity.fileName)).delete();
            File file = new File(this.getFilesDir()+"/"+"myTempFile.txt");
            file.renameTo(new File(this.getFilesDir()+"/"+MainActivity.fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
