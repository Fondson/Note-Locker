package com.example.fondson.mylockscreen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

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

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings);

            Preference myPref = (Preference) findPreference("pref_key_wallpaper");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //requests permissions needed for users to select background image on M or above
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                        requestPermissions(MainActivity.perms, 200);
                    }
                    else{launchGalleryPicker();}
                    return true;
                }

            });
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

        private void launchGalleryPicker(){
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            photoPickerIntent.setType("image/*");
            photoPickerIntent.putExtra("crop", "true");
            photoPickerIntent.putExtra("output", Uri.fromFile(new File(MainActivity.WALLPAPER_PATH)));
            startActivityForResult(photoPickerIntent,MainActivity.WALLPAPER_CODE);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (data!=null) {
                switch (requestCode) {
                    //handles picture-cropping results
                    case MainActivity.WALLPAPER_CODE:
                        MainActivity.getBackground().setBackground(Drawable.createFromPath(MainActivity.WALLPAPER_PATH));
                        Intent intent = new Intent(getActivity(),MainActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        }
    }
}
