package com.dev.fondson.NoteLocker;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.fondson.NoteLocker.databinding.ActivityTimePickerBinding;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ListIterator;
import java.util.Locale;

public class TimePickerActivity extends AppCompatActivity implements View.OnClickListener
        , TimePickerDialog.OnTimeSetListener
        , DatePickerDialog.OnDateSetListener {
    private ActivityTimePickerBinding activityTimePickerBinding;
    private Calendar dateTime;
    private DateFormat dateFormat;
    private TextView dateTextView;
    private TextView timeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityTimePickerBinding = DataBindingUtil.setContentView(this, R.layout.activity_time_picker);
        activityTimePickerBinding.setActivity(this);
        getSupportActionBar().setTitle("Set date and time");
        dateTextView = activityTimePickerBinding.dateTextView;
        timeTextView = activityTimePickerBinding.timeTextView;
        dateTextView.setOnClickListener(this);
        timeTextView.setOnClickListener(this);

        dateTime = Calendar.getInstance();
        dateTime.setTimeInMillis(getIntent().getExtras().getLong("time"));
        dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        dateTextView.setText(getDate());
        timeTextView.setText(getTime());

        Log.d("nattyDate", getDate());
    }
    public String getDate(){
        return dateFormat.format(dateTime.getTime());
    }
    public String getTime(){
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(dateTime.getTime());
    }

    @Override
    public void onClick(View v) {
        final int viewId = v.getId();
        if (viewId == dateTextView.getId()){
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    this,
                    dateTime.get(Calendar.YEAR),
                    dateTime.get(Calendar.MONTH),
                    dateTime.get(Calendar.DAY_OF_MONTH)
            );
            dpd.setMinDate(Calendar.getInstance());
            dpd.show(getFragmentManager(), "Datepickerdialog");
        }
        else if (viewId == timeTextView.getId()){
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    this,
                    dateTime.get(Calendar.HOUR),
                    dateTime.get(Calendar.MINUTE),
                    dateTime.get(Calendar.SECOND),
                    false
            );
            Calendar cal = Calendar.getInstance();
            if (cal.get(Calendar.DAY_OF_MONTH) == dateTime.get(Calendar.DAY_OF_MONTH)) {
                tpd.setMinTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
            }
            tpd.show(getFragmentManager(), "Timepickerdialog");
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        dateTime.set(year, monthOfYear, dayOfMonth);
        dateTextView.setText(getDate());
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateTime.set(Calendar.MINUTE, minute);
        dateTime.set(Calendar.SECOND, 0);
        timeTextView.setText(getTime());
        Log.d("pickertime", getTime());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.picker_action_bar_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // set item notif
                String key = getIntent().getExtras().getString("key");
                UserItem userItem = null;
                ListIterator<UserItem> iterator = MainActivity.userItemsList.listIterator();
                while (iterator.hasNext()){
                    userItem = iterator.next();
                    if (key.equals(userItem.getKey())){
                        break;
                    }
                }
                userItem.notification.cancelNotif(); // cancels any previous notifs
                userItem.notification.setTime(dateTime.getTime().getTime());
                Firebase.updateToDoItem(userItem);

                String itemName = userItem.getName();
                StringBuilder sb = new StringBuilder();
                sb.append("\"" + itemName + "\" " + "set for ");
                // get time diff
                long diff = userItem.notification.getTime() - Calendar.getInstance().getTime().getTime();
                if (diff > 0) {
                    long timeInSeconds = diff / 1000;
                    long hours, minutes, seconds, days;
                    days = timeInSeconds / (3600 * 24);
                    timeInSeconds = timeInSeconds - (days * (3600 * 24));
                    hours = timeInSeconds / 3600;
                    timeInSeconds = timeInSeconds - (hours * 3600);
                    minutes = timeInSeconds / 60;
                    timeInSeconds = timeInSeconds - (minutes * 60);
                    seconds = timeInSeconds;

                    // configure notif
                    userItem.notification.scheduleNotification(this, userItem, userItem.notification.getNotification(this, userItem), diff);

                    Log.d("diffdebug", String.valueOf(dateTime.getTime().toString()));

                    // build the output string
                    if (days != 0) {
                        if (days > 1) sb.append(String.valueOf(days) + " days ");
                        else sb.append(String.valueOf(Math.abs(days)) + " day ");
                    }
                    if (hours != 0) {
                        if (hours > 1) sb.append(String.valueOf(hours) + " hrs ");
                        else sb.append(String.valueOf(Math.abs(hours)) + " hr ");
                    }
                    if (minutes != 0) {
                        if (minutes > 0) sb.append(String.valueOf(minutes) + " mins ");
                        else sb.append(String.valueOf(Math.abs(minutes)) + " min ");
                    }
                    if (seconds != 0) {
                        if (seconds > 1) sb.append(String.valueOf(seconds) + " seconds ");
                        else sb.append(String.valueOf(Math.abs(seconds)) + " second ");
                    }
                    sb.append("from now.");
                }else{
                    // configure notif
                    userItem.notification.scheduleNotification(this, userItem, userItem.notification.getNotification(this, userItem), diff);

                    // build output string
                    sb.append("now.");
                }

                Toast.makeText(this, sb.toString()
                        , Toast.LENGTH_LONG).show();
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                break;
        }
        return true;
    }

}
