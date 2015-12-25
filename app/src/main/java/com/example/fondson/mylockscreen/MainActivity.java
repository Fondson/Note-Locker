package com.example.fondson.mylockscreen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
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

    final HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
    private ListView lv;
    private ArrayList<Item> itemArr;
    private CustomAdapter adapter;
    public static String fileName="items.txt";
    //public static SharedPreferences prefSet;
    //public static SharedPreferences.Editor prefEdit;
    //public static Set<String> itemArrString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            }
        });
        //ScrollView sv = (ScrollView)findViewById(R.id.sv);
        //sv.setOnTouchListener(new View.OnTouchListener() {
        //    long down;

        //    @Override
        //    public boolean onTouch(View v, MotionEvent event) {

        //    }
        //});
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
        homeKeyLocker.lock(this);
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
}
