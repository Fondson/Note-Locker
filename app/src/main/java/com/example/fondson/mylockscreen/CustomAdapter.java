package com.example.fondson.mylockscreen;

import android.content.Context;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

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

    private class ViewHolder {
        TextView name;
        CheckBox selected;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        final Item item = itemList.get(position);

        if (convertView == null) {
            LayoutInflater vi = ((Activity)context).getLayoutInflater();
            convertView = vi.inflate(R.layout.row, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.textView1);
            holder.selected = (CheckBox) convertView.findViewById(R.id.checkBox1);
            convertView.setTag(holder);
            }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                if (isChecked) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            itemList.remove(position);
                            Toast.makeText(context, item.getName() + " removed.", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                        }
                    }, 220);
                }
            }
        });
        holder.name.setText(item.getName());
        holder.selected.setChecked(item.isSelected());
        holder.name.setTag(item);


        return convertView;

    }
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
