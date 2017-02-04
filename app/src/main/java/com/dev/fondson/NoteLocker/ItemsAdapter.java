package com.dev.fondson.NoteLocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.fondson.NoteLocker.databinding.RowBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Calendar;

/**
 * Created by Fondson on 2016-05-16.
 */
public class ItemsAdapter extends BaseExpandableListAdapter{
    public static int CALENDAR = 0;
    public static int NOT_COMPLETED = 1;
    public static int COMPLETED = 2;
    private static String[] HEADERS = {"Agenda", "To do", "Completed"};
    private Context context;
    private LinkedList<LinkedList<?>> state;
    private LinkedList<UserItem> notCompletedItems;
    private LinkedList<UserItem> completedItems;
    private LinkedList<UserItem> calendarItems;

    public ItemsAdapter(Context context, LinkedList<LinkedList<?>> state){
        this.context=context;
        this.state=state;
        calendarItems = (LinkedList<UserItem>)state.get(CALENDAR);
        notCompletedItems=(LinkedList<UserItem>)state.get(NOT_COMPLETED);
        completedItems=(LinkedList<UserItem>)state.get(COMPLETED);
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
        String headerText;
        TextView header= (TextView) convertView.findViewById(R.id.header);
        header.setVisibility(View.VISIBLE);
        headerText = HEADERS[groupPosition] + " (" + getChildrenCount(groupPosition) + ")";
        if (groupPosition==CALENDAR) {
            DateFormat formatter = new SimpleDateFormat("E-MMM dd");
            headerText += " for " + formatter.format(Calendar.getInstance().getTime());
            if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsActivity.PREF_KEY_CALENDAR,false)
                    || context.checkCallingOrSelfPermission("android.permission.READ_CALENDAR")!= PackageManager.PERMISSION_GRANTED
                    || context.checkCallingOrSelfPermission("android.permission.WRITE_CALENDAR")!= PackageManager.PERMISSION_GRANTED) {
                header.setVisibility(View.GONE);
                ((ExpandableListView)parent).collapseGroup(CALENDAR);
                Log.d("caldebug", "hiding cal");
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
        ImageView darkTint;
        TextView timeBegin;
        TextView timeEnd;
        TextView title;
        TextView location;
        RowBinding binding;
    }
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            holder = new ViewHolder();
            holder.binding = RowBinding.inflate(inflater, parent, false);//inflater.inflate(R.layout.row,parent,false);

            convertView = holder.binding.getRoot();
            holder.name = (EditText) convertView.findViewById(R.id.editText1);
            holder.darkTint = (ImageView) convertView.findViewById(R.id.itemDarkTint);
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
        else{
            holder=(ViewHolder) convertView.getTag();
        }
        holder.name.setKeyListener(null);
        //holder.darkTint.setBackgroundColor(darkColour);
        holder.name.setPaintFlags(0);

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
            holder.binding.setItem(userItem);
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
                                Firebase.removeCompletedItem(userItem.getKey());
                                Firebase.writeNewToDoItem(userItem.getName(),userItem.isSelected());
                                enableDisableViewGroupClickable(parent, true);
                            }
                        }, 250);
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
                            Firebase.removeCompletedItem(userItem.getKey());
                            enableDisableViewGroupClickable(parent, true);
                        }
                    }, 250);
                }

            });
        }
        else if (groupPosition==NOT_COMPLETED){
            final UserItem userItem = (UserItem)getChild(groupPosition,childPosition);
            holder.binding.setItem(userItem);
            holder.name.setText(userItem.getName());
            holder.checkBox.setChecked(false);
            holder.imageButton.setImageDrawable(context.getResources().getDrawable(R.drawable.star_selector));

            holder.imageButton.setSelected(userItem.isSelected());
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton arg0, boolean isChecked) {
                    if (isChecked) {
                        arg0.setChecked(false);
                        holder.imageButton.setSelected(false);
                        enableDisableViewGroupClickable(parent, false);
                        Animation fadeout = new AlphaAnimation(1.f, 0.f);
                        fadeout.setDuration(300);
                        row.startAnimation(fadeout);
                        row.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Firebase.removeToDoItem(userItem.getKey());
                                Firebase.writeNewCompletedItem(userItem.getName(),userItem.isSelected());
                                enableDisableViewGroupClickable(parent, true);
                            }
                        }, 250);
                    }
                }
            });

            final View cView = convertView;
            holder.name.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final EditText editText = (EditText) view;
                    showTextDialog(editText);
                    return true;
                }

                private void showTextDialog(final EditText editText){
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    String oldItem = editText.getText().toString();

                    builder.setTitle("Change item:");
                    // Inflate new view
                    View viewInflated = LayoutInflater.from(context).inflate(R.layout.edit_item, null);
                    // Set up the input
                    final EditText input = (EditText) viewInflated.findViewById(R.id.newItemInput);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setText(oldItem);
                    input.setSelection(editText.getText().length());
                    builder.setView(viewInflated);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Pattern.quote is used to escape special regex characters if present
                            if (!(input.getText().toString().trim()).isEmpty()) {
                                String newItemName = input.getText().toString().trim();
                                userItem.setName(newItemName);
                                Firebase.updateToDoItem(userItem);
                                editText.setText(newItemName);
                            }else{
                                Toast.makeText(context, "Invalid item.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            fullScreencall();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            fullScreencall();
                        }
                    });

                    builder.show();
                }

                private void fullScreencall() {
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
            });
            holder.imageButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View button) {
                    //Set the button's appearance
                    boolean selected =!button.isSelected();
                    button.setSelected(selected);
                    userItem.setSelected(selected);
                    if (selected){
                        userItem.setSelected(true);
                    }
                    Firebase.updateToDoItem(userItem);

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
