package com.dev.fondson.NoteLocker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.MediaStore;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

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
                // handles cropped image results
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    InputStream inputStream;
                    File wallpaperFile = new File(MainActivity.WALLPAPER_FULL_PATH);
                    try {
                        DisplayMetrics widthMetrics = new DisplayMetrics();
                        ((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(widthMetrics);
                        int width = widthMetrics.widthPixels;

                        DisplayMetrics heightMetrics = new DisplayMetrics();
                        ((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(heightMetrics);
                        int height = heightMetrics.heightPixels;

                        inputStream = this.getContentResolver().openInputStream(result.getUri());
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                        Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);
                        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                        // write wallpaper file
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

                        Drawable wallpaperDrawable= DrawableContainer.createFromPath(wallpaperFile.getAbsolutePath());
                        MainActivity.getBackground().setImageDrawable(wallpaperDrawable);
                        new PaletteTask().execute(MainActivity.drawableToBitmap(wallpaperDrawable));
                        MainActivity.itemsAdapter.notifyParentDataSetChanged(true);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    startActivity(new Intent(this,MainActivity.class));
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!MainActivity.loggingOut) {
            super.onBackPressed();
        }
        return;
    }
    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener,Preference.OnPreferenceClickListener {
        private SeekBarPreference darkTintSeekBar;
        private SharedPreferences sharedPreferences;
        private Preference googleAccountPref;
        private Preference quickUnlockPref;
        private Resources resources;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings);

            Preference wallpaperPref = findPreference("pref_key_wallpaper");
            Preference tutorialPref = findPreference("pref_key_tutorial");
            googleAccountPref = findPreference("pref_key_google_account");
            darkTintSeekBar = (SeekBarPreference)findPreference("pref_key_darkTint");
            //Preference transferPref = findPreference("pref_key_transfer_data");
            Preference betaLinkPref = findPreference("pref_key_beta_link");
            quickUnlockPref = findPreference("pref_key_quick_unlock");

            resources = getResources();

            // Set listener :
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            // Set seekbar summary :
            int alphaValue = sharedPreferences.getInt("pref_key_darkTint", resources.getInteger(R.integer.DARK_TINT_DEFAULT));
            darkTintSeekBar.setSummary(("$%").replace("$", ""+alphaValue));

            wallpaperPref.setOnPreferenceClickListener(this);
            googleAccountPref.setOnPreferenceClickListener(this);
            tutorialPref.setOnPreferenceClickListener(this);
            //transferPref.setOnPreferenceClickListener(this);
            betaLinkPref.setOnPreferenceClickListener(this);
        }

        @Override
        public void onStart() {
            if (MainActivity.userEmail!=null) {
                googleAccountPref.setSummary(MainActivity.userEmail);
            }
            else{
                googleAccountPref.setSummary("None");
            }

            setQuickUnlockPrefText();
            super.onStart();
        }

        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case "pref_key_google_account":
                    if (MainActivity.mGoogleApiClient.isConnected() && isOnline()) {
                        MainActivity.loggingOut = true;
                        Auth.GoogleSignInApi.signOut(MainActivity.mGoogleApiClient);
                        MainActivity.mAuth.signOut();
                        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(MainActivity.mGoogleApiClient);
                        //MainActivity.mAuth = FirebaseAuth.getInstance();
                        startActivityForResult(signInIntent, MainActivity.GOOGLE_ACCOUNT_SIGN_IN_CODE);
                    }
                    else if (isOnline()){
                        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(MainActivity.mGoogleApiClient);
                        //MainActivity.mAuth = FirebaseAuth.getInstance();
                        startActivityForResult(signInIntent, MainActivity.GOOGLE_ACCOUNT_SIGN_IN_CODE);
                    }else{
                        Toast.makeText(getActivity(), "Cannot access Google accounts without internet connection.",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "pref_key_wallpaper":
                    //requests permissions needed for users to select background image on M or above
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M
                            && getContext().checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE")!= PackageManager.PERMISSION_GRANTED){
                        requestPermissions(MainActivity.wallpaperPerms, 200);
                    }
                    else{launchGalleryPicker();}
                    break;
                case "pref_key_tutorial":
                    //  Launch app intro
                    Intent i = new Intent(getActivity(), Intro.class);
                    startActivity(i);
                    break;
                case "pref_key_beta_link":
                    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText("https://play.google.com/apps/testing/com.dev.fondson.NoteLocker");
                    } else {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", "https://play.google.com/apps/testing/com.dev.fondson.NoteLocker");
                        clipboard.setPrimaryClip(clip);
                    }
                    Toast.makeText(getActivity(), "Copied beta link to clipboard.",
                            Toast.LENGTH_SHORT).show();
                    break;

            }
            return true;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case "pref_key_darkTint":
                    // Set seekbar summary :
                    int alphaValue = sharedPreferences.getInt("pref_key_darkTint", resources.getInteger(R.integer.DARK_TINT_DEFAULT));
                    darkTintSeekBar.setSummary(("$%").replace("$", "" + alphaValue));
                    MainActivity.getDarkTint().setAlpha(1f - (float) alphaValue / 100);
                    break;
                case "pref_key_quick_unlock":
                    setQuickUnlockPrefText();
            }
        }

        private void setQuickUnlockPrefText(){
            if (!sharedPreferences.getBoolean("pref_key_quick_unlock", true)){
                quickUnlockPref.setSummary("Swipe to unlock");
            }else{
                quickUnlockPref.setSummary("Tap to unlock");
            }
        }

        //handles permission requests
        @Override
        public void onRequestPermissionsResult(int permsRequestCode, String[] permissions,int[] grantResults){
            switch (permsRequestCode){
                case 200:
                    if (grantResults.length>0
                            && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                        launchGalleryPicker();
                    }
                    break;
//                case MainActivity.CALENDAR_PERMS:
//                    if (grantResults.length>=0
//                            && grantResults[0]==PackageManager.PERMISSION_DENIED) {
//                        calendarPref.getEditor().putBoolean(PREF_KEY_CALENDAR,false).apply();
//                        calendarPref.setChecked(false);
//                    }
//                    break;
            }
        }

        private void launchGalleryPicker(){
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.setType("image/*");
            photoPickerIntent.putExtra("return-data", true);
            startActivityForResult(photoPickerIntent,MainActivity.WALLPAPER_CODE);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (data!=null) {
                switch (requestCode) {
                    //initiates image-cropping activity
                    case MainActivity.WALLPAPER_CODE:
                        DisplayMetrics widthMetrics = new DisplayMetrics();
                        ((WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(widthMetrics);
                        int width = widthMetrics.widthPixels;

                        DisplayMetrics heightMetrics = new DisplayMetrics();
                        ((WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(heightMetrics);
                        int height = heightMetrics.heightPixels;

                        CropImage.activity(data.getData()).setAspectRatio(width,height).setFixAspectRatio(true).start(getActivity());
                        break;
                    case MainActivity.GOOGLE_ACCOUNT_SIGN_IN_CODE:
                        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                        if (result.isSuccess()) {
                            if (!MainActivity.mGoogleApiClient.isConnected()) {
                                MainActivity.mGoogleApiClient.connect();
                            }

                            // Signed in successfully, show authenticated UI.
                            GoogleSignInAccount acct = result.getSignInAccount();
                            Firebase.authWithGoogle(acct, MainActivity.mAuth, getActivity());
                            MainActivity.userEmail = acct.getEmail();
                            googleAccountPref.setSummary(MainActivity.userEmail);
                        }
                        else {
                            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(MainActivity.mGoogleApiClient);
                            startActivityForResult(signInIntent, MainActivity.GOOGLE_ACCOUNT_SIGN_IN_CODE);
                        }
                        break;
                }
            }
        }

        public boolean isOnline() {
            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        }
    }
}
