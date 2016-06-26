package com.dev.fondson.NoteLocker;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by Fondson on 2016-05-16.
 */
public class ItemsAdapter extends BaseExpandableListAdapter{
    public static int NOT_COMPLETED = 0;
    public static int COMPLETED = 1;
    private static String[] HEADERS = {"To do" ,"Completed"};
    private Context context;
    private ArrayList<ArrayList<Item>> state;
    private ArrayList<Item> notCompletedItems;
    private ArrayList<Item> completedItems;

    public ItemsAdapter(Context context, ArrayList<ArrayList<Item>> state){
        this.context=context;
        this.state=state;
        notCompletedItems=state.get(0);
        completedItems=state.get(1);
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
    public Item getChild(int groupPosition, int childPosition) {
        return state.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return state.get(groupPosition).get(childPosition).getId();
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
        convertView.setClickable(false);
        if (groupPosition==NOT_COMPLETED){
            ((ExpandableListView)parent).expandGroup(NOT_COMPLETED);
            convertView.setClickable(true); //tip: this makes convertView NOT clickable along with convertView.setClickable(false) above???
        }
        TextView header= (TextView) convertView.findViewById(R.id.header);
        String headerText = HEADERS[groupPosition] + " (" + getChildrenCount(groupPosition) + ")";
        header.setText(headerText);
        return convertView;
    }

    public static class ViewHolder{
        EditText name;
        CheckBox checkBox;
        ImageButton imageButton;
    }
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {
        final Item item = getChild(groupPosition,childPosition);
        final ViewHolder holder;
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row,parent,false);

            holder = new ViewHolder();
            holder.name = (EditText) convertView.findViewById(R.id.editText1);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
            holder.imageButton = (ImageButton) convertView.findViewById(R.id.imageButton1);

            convertView.setTag(holder);
        }
        else{holder=(ViewHolder) convertView.getTag();}
        //final KeyListener editable = name.getKeyListener();
        holder.name.setKeyListener(null);
        holder.name.setText(item.getName());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.name.setOnLongClickListener(null);
        holder.imageButton.setOnClickListener(null);

        final View row = convertView;
        if (groupPosition==COMPLETED) {
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
                                MainActivity.db.deleteRow(item.getId());
                                completedItems.remove(childPosition);
                                enableDisableViewGroupClickable(parent, true);

                                MainActivity.db.switchTable(DBAdapter.DATABASE_TABLE_ITEMS);
                                Long newId=MainActivity.db.insertRow(item.getName(),(item.isSelected())?1 :0);
                                notCompletedItems.add(0, new Item(newId, item.getName(), item.isSelected()));
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
                            MainActivity.db.deleteRow(item.getId());
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
            holder.checkBox.setChecked(false);
            holder.name.setPaintFlags(0);
            holder.imageButton.setImageDrawable(context.getResources().getDrawable(R.drawable.star_selector));
           // holder.imageButton.setSelected(!holder.imageButton.isSelected());

            if (item.isSelected()){
                holder.imageButton.setSelected(true);
            }
            else{
                holder.imageButton.setSelected(false);
            }
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
                                MainActivity.db.deleteRow(item.getId());
                                notCompletedItems.remove(childPosition);
                                enableDisableViewGroupClickable(parent, true);

                                MainActivity.db.switchTable(DBAdapter.DATABASE_TABLE_COMPLETED_ITEMS);
                                Long newId=MainActivity.db.insertRow(item.getName(),(item.isSelected())?1 :0);
                                //writeFile(etInput.getText().toString().trim());
                                completedItems.add(0, new Item(newId, item.getName(), item.isSelected()));
                                notifyDataSetChanged();
                                MainActivity.requestBackup(context);
                                //Toast.makeText(context, item.getName() + " removed.", Toast.LENGTH_SHORT).show();
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
                    //final Item item = (Item) view.getItemAtPosition(pos);
                    final EditText editText = (EditText) view;
                    editText.setKeyListener(MainActivity.listener);
                    editText.requestFocus();
                    editText.setSelection(editText.getText().length());

                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
                    editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                                                           @Override
                                                           public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
                                                               //Pattern.quote is used to escape special regex characters in item if present
                                                               if (!(Pattern.quote(editText.getText().toString().trim()).matches(Pattern.quote(item.getName()))) && !(Pattern.quote(editText.getText().toString().trim()).matches(""))) {
                                                                   //replaceFile(item.getName(), editText.getText().toString().trim());
                                                                   String newItemName = editText.getText().toString().trim();
                                                                   MainActivity.db.switchTable(DBAdapter.DATABASE_TABLE_ITEMS);
                                                                   MainActivity.db.updateRow(item.getId(), newItemName);
                                                                   item.setName(newItemName);
                                                                   //Toast.makeText(MainActivity.this, pastName + " changed to " + item.getName(), Toast.LENGTH_SHORT).show();
                                                               }
                                                               editText.setKeyListener(null);
                                                               hideKeyboard();
                                                               fullScreencall();
                                                               editText.setText(item.getName());
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
                    item.setSelected(selected);

                    int intBool=0;
                    if (selected){
                        item.setSelected(true);
                        intBool =1;
                    }

                    MainActivity.db.switchTable(DBAdapter.DATABASE_TABLE_ITEMS);
                    MainActivity.db.updateRowSelected(item.getId(),item.getName(),intBool);
                    MainActivity.requestBackup(context);

                }

            });
        }

        //Toast.makeText(context, "hii", Toast.LENGTH_SHORT).show();
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
