package com.dev.fondson.NoteLocker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null) {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:

                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                InputStream inputStream;
                File wallpaperFile = new File(MainActivity.WALLPAPER_FULL_PATH);
                try {
                    inputStream = this.getContentResolver().openInputStream(result.getUri());
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                    Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);
                    //copyInputStreamToFile(inputStream,wallpaperFile);                        //Bitmap bitmap=data.getExtras().getParcelable("data");
                    bitmap =Bitmap.createScaledBitmap(bitmap, 2048, 2048, true);
                    Drawable wallpaper= new BitmapDrawable(getResources(), bitmap);
                    MainActivity.getBackground().setBackground(wallpaper);
                    //copyInputStreamToFile(inputStream,wallpaperFile);
                    if (wallpaperFile.exists())
                        wallpaperFile.delete();
                    try {
                        FileOutputStream out = new FileOutputStream(wallpaperFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
            }
        }
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
//                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M
//                            && getContext().checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE")!= PackageManager.PERMISSION_GRANTED
//                            && getContext().checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE")!= PackageManager.PERMISSION_GRANTED){
//                        requestPermissions(MainActivity.perms, 200);
//                    }
//                    else{launchGalleryPicker();}
                    launchGalleryPicker();
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
//        @Override
//        public void onRequestPermissionsResult(int permsRequestCode, String[] permissions,int[] grantResults){
//            switch (permsRequestCode){
//                case 200:
//                    if (grantResults.length>0
//                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                        launchGalleryPicker();
//                    }
//                    break;
//            }
//        }

        private void launchGalleryPicker(){
//            File wallpaperPath = new File(MainActivity.WALLPAPER_PATH);
//            try {
//                wallpaperPath.mkdirs();
//                wallpaper.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.setType("image/*");
            photoPickerIntent.putExtra("return-data", true);
            //photoPickerIntent.putExtra("crop", "true");
            //photoPickerIntent.putExtra("output", Uri.parse(MainActivity.WALLPAPER_FULL_PATH));
            startActivityForResult(photoPickerIntent,MainActivity.WALLPAPER_CODE);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (data!=null) {
                switch (requestCode) {
                    //handles picture-cropping results
                    case MainActivity.WALLPAPER_CODE:
//                        Bitmap bitmap = BitmapFactory.decodeFile(MainActivity.WALLPAPER_FULL_PATH);
//                        Bitmap bitmap = null;
//                        try {
//                            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(MainActivity.WALLPAPER_FULL_PATH));
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }


                        CropImage.activity(data.getData()).start(getActivity());
                        //startActivity(intent);x

                        break;
                }
            }
        }
        private void copyInputStreamToFile( InputStream in, File file ) {
            try {
                OutputStream out = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len;
                while((len=in.read(buf))>0){
                    out.write(buf,0,len);
                }
                out.close();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
