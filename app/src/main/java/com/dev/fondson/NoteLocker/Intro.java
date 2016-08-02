package com.dev.fondson.NoteLocker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class Intro extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add your slide's fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(IntroSlide.newInstance(R.layout.first_frag));
        addSlide(IntroSlide.newInstance(R.layout.second_frag));
        addSlide(IntroSlide.newInstance(R.layout.third_frag));
        addSlide(IntroSlide.newInstance(R.layout.fourth_frag));
        addSlide(IntroSlide.newInstance(R.layout.fifth_frag));

        setProgressButtonEnabled(true);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if(Build.VERSION.SDK_INT < 19){ //19 or above api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else {
            //for lower api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }

        setSeparatorColor(921515);
        showSkipButton(false);
        //skipButton.setEnabled(false);
        //skipButton.setAlpha(0);

        // Turn vibration on and set intensity.
        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        loadMainActivity();
    }
    // Don't finish Activity on Back press
    @Override
    public void onBackPressed() {
        return;
    }
    private void loadMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}