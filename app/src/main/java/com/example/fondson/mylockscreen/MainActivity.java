package com.example.fondson.mylockscreen;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fondson.mylockscreen.AutoStart;
import com.example.fondson.mylockscreen.UpdateService;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
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
                    ll.addView(cb);
                    etInput.setText("");
                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                            if (isChecked) {
                                ll.removeView(arg0);
                                Toast.makeText(MainActivity.this, cb.getText()+" removed.", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
                    return true;
                }
                return false;

        }});

    }

    // Don't finish Activity on Back press
    @Override
    public void onBackPressed() {
        return;
    }
    protected void onPause() {
        startService(new Intent(this, UpdateService.class));
        super.onPause();
    }
}
