package com.example.fondson.mylockscreen;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import com.example.fondson.mylockscreen.AutoStart;
import com.example.fondson.mylockscreen.UpdateService;

import io.github.homelocker.lib.HomeKeyLocker;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        //final HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
        //homeKeyLocker.lock(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, UpdateService.class));
        ScrollView sv = (ScrollView)findViewById((R.id.sv));
        final LinearLayout ll = (LinearLayout)findViewById(R.id.llMain);
        ll.setOrientation(LinearLayout.VERTICAL);
        final EditText etInput = (EditText) findViewById(R.id.editText);
        etInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                                              @Override
                                              public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
                                                  if (arg1 == EditorInfo.IME_ACTION_NEXT && !(etInput.getText().toString().trim().matches(""))) {
                                                      final CheckBox cb = new CheckBox(MainActivity.this);
                                                      cb.setText(etInput.getText().toString().trim());
                                                      cb.setTextSize(17);
                                                      ll.addView(cb, 0);
                                                      etInput.setText("");
                                                      cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                                                        @Override
                                                                                        public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                                                                                            if (isChecked) {
                                                                                                Handler handler = new Handler();
                                                                                                handler.postDelayed(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        ll.removeView(cb);
                                                                                                        Toast.makeText(MainActivity.this, cb.getText() + " removed.", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                }, 250);}}
                                                                                    });
                                                      return true;}
                                                  return false;}}
        );
        final SeekBar sb = (SeekBar)findViewById(R.id.seekBar);
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
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (progress > 80) {
                    //homeKeyLocker.unlock();
                    moveTaskToBack(true);
                }

            }
        });

        }

    // Don't finish Activity on Back press
    @Override
    public void onBackPressed() {
        return;
    }
    protected void onResume() {
        final SeekBar sb = (SeekBar) findViewById(R.id.seekBar);
        sb.setProgress(0);
        super.onResume();
    }
    protected void onPause() {
        hideKeyboard();
        startService(new Intent(this, UpdateService.class));
        super.onPause();
    }
    public void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
