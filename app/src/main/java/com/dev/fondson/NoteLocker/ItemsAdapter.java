package com.dev.fondson.NoteLocker;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Created by Fondson on 2016-05-16.
 */
public class ItemsAdapter extends BaseExpandableListAdapter{
    public static int CALENDAR = 0;
    public static int NOT_COMPLETED = 1;
    public static int COMPLETED = 2;
    private static String[] HEADERS = {"Agenda","To do" ,"Completed"};
    private Context context;
    private ArrayList<ArrayList<?>> state;
    private ArrayList<UserItem> notCompletedItems;
    private ArrayList<UserItem> completedItems;
    private ArrayList<UserItem> calendarItems;

    public ItemsAdapter(Context context, ArrayList<ArrayList<?>> state){
        this.context=context;
        this.state=state;
        calendarItems=(ArrayList<UserItem>)state.get(CALENDAR);
        notCompletedItems=(ArrayList<UserItem>)state.get(NOT_COMPLETED);
        completedItems=(ArrayList<UserItem>)state.get(COMPLETED);
    }

    @Override
    public int getGroupCount() {
        return state.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return state.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return state.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return state.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        if (state.get(groupPosition).get(childPosition) instanceof UserItem) {
            return ((UserItem)state.get(groupPosition).get(childPosition)).getId();
        }
        return -1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.state,parent,false);
        }
        //convertView.setClickable(false);
//        if (groupPosition==NOT_COMPLETED || groupPosition==CALENDAR){
//            ((ExpandableListView)parent).expandGroup(groupPosition);
//            convertView.setClickable(true); //tip: this makes convertView NOT clickable along with convertView.setClickable(false) above???
//        }//
        String headerText;
        TextView header= (TextView) convertView.findViewById(R.id.header);
        header.setVisibility(View.VISIBLE);
        DateFormat formatter = new SimpleDateFormat("E-MMM dd");
        headerText = HEADERS[groupPosition] + " (" + getChildrenCount(groupPosition) + ")";
        if (groupPosition==CALENDAR) {
            headerText += " for " + formatter.format(Calendar.getInstance().getTime());
            if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsActivity.PREF_KEY_CALENDAR,true)
                    || context.checkCallingOrSelfPermission("android.permission.READ_CALENDAR")!= PackageManager.PERMISSION_GRANTED
                    || context.checkCallingOrSelfPermission("android.permission.WRITE_CALENDAR")!= PackageManager.PERMISSION_GRANTED) {
                header.setVisibility(View.GONE);
                ((ExpandableListView)parent).collapseGroup(CALENDAR);
            }
        }
        header.setText(headerText);
        return convertView;
    }

    public static class ViewHolder{
        EditText name;
        CheckBox checkBox;
        ImageButton imageButton;
        LinearLayout time;
        LinearLayout event;
        //TextView date;
        ImageView darkTint;
        TextView timeBegin;
        TextView timeEnd;
        TextView title;
        TextView location;
    }
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row,parent,false);

            holder = new ViewHolder();
            holder.name = (EditText) convertView.findViewById(R.id.editText1);
            holder.darkTint = (ImageView) convertView.findViewById(R.id.ivCalendarDarkTint);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
            holder.imageButton = (ImageButton) convertView.findViewById(R.id.imageButton1);
            holder.time = (LinearLayout) convertView.findViewById(R.id.llTime);
            holder.event = (LinearLayout) convertView.findViewById(R.id.llEvent);
            holder.timeBegin = (TextView) convertView.findViewById(R.id.txtTimeBegin);
            holder.timeEnd = (TextView) convertView.findViewById(R.id.txtTimeEnd);
            holder.title = (TextView) convertView.findViewById(R.id.txtTitle);
            holder.location = (TextView) convertView.findViewById(R.id.txtLocation);

            convertView.setTag(holder);
        }
        else{holder=(ViewHolder) convertView.getTag();}
        //final KeyListener editable = name.getKeyListener();
        holder.name.setKeyListener(null);
        holder.darkTint.setVisibility(View.GONE);

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.name.setOnLongClickListener(null);
        holder.imageButton.setOnClickListener(null);

        holder.checkBox.setVisibility(View.VISIBLE);
        holder.imageButton.setVisibility(View.VISIBLE);
        holder.name.setVisibility(View.VISIBLE);
        holder.time.setVisibility(View.GONE);
        holder.event.setVisibility(View.GONE);
        final View row = convertView;
        if (groupPosition==COMPLETED) {
            final UserItem userItem = (UserItem)getChild(groupPosition,childPosition);
            holder.name.setText(userItem.getName());
            holder.checkBox.setChecked(true);
            holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.imageButton.setImageDrawable(context.getResources().getDrawable(R.drawable.x));
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton arg0, boolean isChecked) {
                    if (!isChecked) {
                        arg0.setChecked(true);
                        enableDisableViewGroupClickable(parent, false);


                        Animation fadeout = new AlphaAnimation(1.f, 0.f);
                        fadeout.setDuration(300);
                        row.startAnimation(fadeout);
                        row.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.db.switchTable(DBAdapter.DATABASE_TABLE_COMPLETED_ITEMS);
                                MainActivity.db.deleteRow(userItem.getId());
                                completedItems.remove(childPosition);
                                enableDisableViewGroupClickable(parent, true);

                                MainActivity.db.switchTable(DBAdapter.DATABASE_TABLE_ITEMS);
                                Long newId=MainActivity.db.insertRow(userItem.getName(),(userItem.isSelected())?1 :0);
                                notCompletedItems.add(0, new UserItem(newId, userItem.getName(), userItem.isSelected()));
                                MainActivity.requestBackup(context);

                                notifyDataSetChanged();
                            }
                        }, 300);
                    }
                }
            });
            holder.imageButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View button) {
                    enableDisableViewGroupClickable(parent, false);
                    Animation fadeout = new AlphaAnimation(1.f, 0.f);
                    fadeout.setDuration(300);
                    row.startAnimation(fadeout);
                    row.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.db.switchTable(DBAdapter.DATABASE_TABLE_COMPLETED_ITEMS);
                            MainActivity.db.deleteRow(userItem.getId());
                            completedItems.remove(childPosition);
                            enableDisableViewGroupClickable(parent, true);
                            MainActivity.requestBackup(context);

                            notifyDataSetChanged();
                        }
                    }, 300);
                }

            });
        }

        else if (groupPosition==NOT_COMPLETED){
            final UserItem userItem = (UserItem)getChild(groupPosition,childPosition);
            holder.name.setText(userItem.getName());
            holder.checkBox.setChecked(false);
            holder.name.setPaintFlags(0);
            holder.imageButton.setImageDrawable(context.getResources().getDrawable(R.drawable.star_selector));
           // holder.imageButton.setSelected(!holder.imageButton.isSelected());

            holder.imageButton.setSelected(userItem.isSelected());
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton arg0, boolean isChecked) {
                    if (isChecked) {
                        //holder.linearLayout.animate().setDuration(200).alpha(0);
                        arg0.setChecked(false);
                        holder.imageButton.setSelected(false);
                        enableDisableViewGroupClickable(parent, false);
                        Animation fadeout = new AlphaAnimation(1.f, 0.f);
                        fadeout.setDuration(300);
                        row.startAnimation(fadeout);
                        row.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.db.switchTable(DBAdapter.DATABASE_TABLE_ITEMS);
                                MainActivity.db.deleteRow(userItem.getId());
                                notCompletedItems.remove(childPosition);
                                enableDisableViewGroupClickable(parent, true);

                                MainActivity.db.switchTable(DBAdapter.DATABASE_TABLE_COMPLETED_ITEMS);
                                Long newId=MainActivity.db.insertRow(userItem.getName(),(userItem.isSelected())?1 :0);
                                //writeFile(etInput.getText().toString().trim());
                                completedItems.add(0, new UserItem(newId, userItem.getName(), userItem.isSelected()));
                                notifyDataSetChanged();
                                MainActivity.requestBackup(context);
                                //Toast.makeText(context, userItem.getName() + " removed.", Toast.LENGTH_SHORT).show();
                            }
                        }, 300);
                    }
                }
            });

            final View cView = convertView;
            holder.name.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //homeKeyLocker.unlock();
                    //final UserItem userItem = (UserItem) view.getItemAtPosition(pos);
                    final EditText editText = (EditText) view;
                    editText.setKeyListener(MainActivity.listener);
                    editText.requestFocus();
                    editText.setSelection(editText.getText().length());

                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
                    editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                                                           @Override
                                                           public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
                                                               //Pattern.quote is used to escape special regex characters in userItem if present
                                                               if (!(Pattern.quote(editText.getText().toString().trim()).matches(Pattern.quote(userItem.getName()))) && !(Pattern.quote(editText.getText().toString().trim()).matches(""))) {
                                                                   //replaceFile(userItem.getName(), editText.getText().toString().trim());
                                                                   String newItemName = editText.getText().toString().trim();
                                                                   MainActivity.db.switchTable(DBAdapter.DATABASE_TABLE_ITEMS);
                                                                   MainActivity.db.updateRow(userItem.getId(), newItemName);
                                                                   userItem.setName(newItemName);
                                                                   //Toast.makeText(MainActivity.this, pastName + " changed to " + userItem.getName(), Toast.LENGTH_SHORT).show();
                                                               }
                                                               editText.setKeyListener(null);
                                                               hideKeyboard();
                                                               fullScreencall();
                                                               editText.setText(userItem.getName());
                                                               MainActivity.requestBackup(context);
                                                               return true;
                                                           }

                                                           public void hideKeyboard() {
                                                               View view = cView;
                                                               if (view != null) {
                                                                   InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                   imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                                               }
                                                           }
                                                            public void fullScreencall() {
                                                                if(Build.VERSION.SDK_INT < 19){ //19 or above api
                                                                    View v = ((Activity)context).getWindow().getDecorView();
                                                                    v.setSystemUiVisibility(View.GONE);
                                                                } else {
                                                                    //for lower api versions.
                                                                    View decorView = ((Activity)context).getWindow().getDecorView();
                                                                    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                                                                    decorView.setSystemUiVisibility(uiOptions);
                                                                }
                                                            }
                                                       }
                    );
                    return true;
                }
            });
            holder.imageButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View button) {
                    //Set the button's appearance
                    boolean selected =!button.isSelected();
                    button.setSelected(selected);
                    userItem.setSelected(selected);

                    int intBool=0;
                    if (selected){
                        userItem.setSelected(true);
                        intBool =1;
                    }

                    MainActivity.db.switchTable(DBAdapter.DATABASE_TABLE_ITEMS);
                    MainActivity.db.updateRowSelected(userItem.getId(),userItem.getName(),intBool);
                    MainActivity.requestBackup(context);

                }

            });
        }

        else if (groupPosition==CALENDAR) {
            CalendarItem calendarItem = (CalendarItem)getChild(groupPosition,childPosition);

            if (calendarItem.timeBegin.equals("All day")){
                holder.timeBegin.setText(calendarItem.timeBegin);
            }
            else{
                holder.timeBegin.setText(calendarItem.timeBegin + " -");
                holder.timeEnd.setText(calendarItem.timeEnd);
            }
            holder.title.setText(calendarItem.event);
            holder.location.setText(calendarItem.location);
            holder.title.setPaintFlags(holder.name.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);

            if (calendarItem.location==null) holder.location.setVisibility(View.GONE);
            else holder.location.setVisibility(View.VISIBLE);
            if (calendarItem.timeEnd==null) holder.timeEnd.setVisibility(View.GONE);
            else holder.timeEnd.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.GONE);
            holder.imageButton.setVisibility(View.GONE);
            holder.name.setVisibility(View.GONE);
            holder.time.setVisibility(View.VISIBLE);
            holder.event.setVisibility(View.VISIBLE);
            holder.darkTint.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    //enables/disables clickable property for all checkboxes in viewGroup
    public static void enableDisableViewGroupClickable(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof CheckBox || view instanceof ImageButton){
                view.setClickable(enabled);
            }
            if (view instanceof ViewGroup) {
                enableDisableViewGroupClickable((ViewGroup) view, enabled);
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
