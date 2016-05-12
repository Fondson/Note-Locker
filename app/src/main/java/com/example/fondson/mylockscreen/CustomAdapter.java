package com.example.fondson.mylockscreen;

import android.app.Service;
import android.content.Context;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Fondson on 2015-12-21.
 */
public class CustomAdapter extends ArrayAdapter<Item>{
    private ArrayList<Item> itemList;
    Context context;

    public CustomAdapter(Context context,
                           ArrayList<Item> itemList) {
        super(context, R.layout.row, itemList);
        this.context=context;
        this.itemList = itemList;
    }

    public class ViewHolder {
        TextView name;
        CheckBox checkBox;
        //LinearLayout linearLayout;
    }

    public ViewHolder holder = null;
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final Item item = itemList.get(position);

        if (convertView == null) {
            LayoutInflater vi = ((Activity)context).getLayoutInflater();
            convertView = vi.inflate(R.layout.row, parent, false);
            holder = new ViewHolder();
            holder.name = (EditText) convertView.findViewById(R.id.editText1);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
            convertView.setTag(holder);
            }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final View row=convertView;
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton arg0, boolean isChecked) {
                if (isChecked) {
                    //holder.linearLayout.animate().setDuration(200).alpha(0);
                    arg0.setChecked(false);
                    row.animate().setDuration(300).alpha(0);
                    enableDisableViewGroupClickable(parent,false);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            removeFile(item.getName());
                            MainActivity.db.deleteRow(item.getId());
                            itemList.remove(position);
                            enableDisableViewGroupClickable(parent,true);
                            notifyDataSetChanged();
                            row.setAlpha(1);
                            //Toast.makeText(context, item.getName() + " removed.", Toast.LENGTH_SHORT).show();
                        }
                    }, 300);
                }
            }
        });
        holder.name.setKeyListener(null);
        holder.name.setText(item.getName());
        holder.checkBox.setChecked(item.isSelected());
        holder.name.setTag(item);

        return convertView;

    }
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    //enables/disables clickable property for all views in viewGroup
    public static void enableDisableViewGroupClickable(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            view.setClickable(enabled);
            if (view instanceof ViewGroup) {
                enableDisableViewGroupClickable((ViewGroup) view, enabled);
            }
        }
    }
}
