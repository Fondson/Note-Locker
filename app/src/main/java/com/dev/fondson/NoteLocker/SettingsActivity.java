package com.dev.fondson.NoteLocker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREF_KEY_OFF="pref_key_off";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();


    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private SeekBarPreference darkTintSeekBar;
        private SharedPreferences sharedPreferences;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings);

            Preference wallpaperPref = (Preference) findPreference("pref_key_wallpaper");
            Preference tutorialPref = (Preference) findPreference("pref_tutorial");
            darkTintSeekBar = (SeekBarPreference)findPreference("pref_key_darkTint");

            // Set listener :
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            // Set seekbar summary :
            int alphaValue = sharedPreferences.getInt("pref_key_darkTint", 50);
            darkTintSeekBar.setSummary(("$%").replace("$", ""+alphaValue));

            wallpaperPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //requests permissions needed for users to select background image on M or above
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                        requestPermissions(MainActivity.perms, 200);
                    }
                    else{launchGalleryPicker();}
                    return true;
                }

            });

            tutorialPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //  Launch app intro
                    Intent i = new Intent(getActivity(), Intro.class);
                    startActivity(i);

                    return true;
                }

            });
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            if (key.equals("pref_key_darkTint")) {
                // Set seekbar summary :
                int alphaValue = sharedPreferences.getInt("pref_key_darkTint", 50);
                darkTintSeekBar.setSummary(("$%").replace("$", "" + alphaValue));
                ((View)MainActivity.getDarkTint()).setAlpha((float)alphaValue/100);
//                Toast.makeText(getActivity(), "hi",
//                        Toast.LENGTH_LONG).show();
            }
        }


        //handles permission requests
        @Override
        public void onRequestPermissionsResult(int permsRequestCode, String[] permissions,int[] grantResults){
            switch (permsRequestCode){
                case 200:
                    if (grantResults.length>0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        launchGalleryPicker();
                    }
                    break;
            }
        }

        private void launchGalleryPicker(){
            File wallpaperPath = new File(MainActivity.WALLPAPER_PATH);
            File wallpaper = new File(MainActivity.WALLPAPER_FULL_PATH);
            try {
                wallpaperPath.mkdirs();
                wallpaper.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            photoPickerIntent.setType("image/*");
            photoPickerIntent.putExtra("crop", "true");
            photoPickerIntent.putExtra("output", Uri.fromFile(wallpaper));
            startActivityForResult(photoPickerIntent,MainActivity.WALLPAPER_CODE);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (data!=null) {
                switch (requestCode) {
                    //handles picture-cropping results
                    case MainActivity.WALLPAPER_CODE:
                        Drawable wallpaper = Drawable.createFromPath(MainActivity.WALLPAPER_FULL_PATH);
                        MainActivity.getBackground().setBackground(wallpaper);
                        Intent intent = new Intent(getActivity(),MainActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        }
    }
}
