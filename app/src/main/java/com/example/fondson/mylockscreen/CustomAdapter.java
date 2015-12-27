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
        CheckBox selected;
    }

    public ViewHolder holder = null;
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Item item = itemList.get(position);

        if (convertView == null) {
            LayoutInflater vi = ((Activity)context).getLayoutInflater();
            convertView = vi.inflate(R.layout.row, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.editText1);
            holder.selected = (CheckBox) convertView.findViewById(R.id.checkBox1);
            convertView.setTag(holder);
            }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        //holder.name.setOnLongClickListener(new View.OnLongClickListener() {
        //    @Override
        //    public boolean onLongClick(View v) {
        //        MainActivity.homeKeyLocker.unlock();
        //        holder.name.setFocusable(true);
        //        holder.name.setFocusableInTouchMode( true );
        //        holder.name.requestFocus();

        //        InputMethodManager imm = (InputMethodManager) context.getSystemService(Service.INPUT_METHOD_SERVICE);
        //       imm.showSoftInput(holder.name, InputMethodManager.SHOW_FORCED);
        //        listener = holder.name.getKeyListener();
        //        holder.name.setKeyListener(listener);
        //        notifyDataSetChanged();
        //        Toast.makeText(context, item.getName() + " longclicked.", Toast.LENGTH_SHORT).show();
        //       return false;
        //   }
        //});
        holder.selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                if (isChecked) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //MainActivity.itemArrString.remove(item.getName());
                            //MainActivity.prefEdit.putStringSet("item", MainActivity.itemArrString);
                            //MainActivity.prefEdit.commit();
                            removeFile(item.getName());
                            itemList.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(context, item.getName() + " removed.", Toast.LENGTH_SHORT).show();
                        }
                    }, 200);
                }
            }
        });
        holder.name.setKeyListener(null);
        holder.name.setText(item.getName());
        holder.selected.setChecked(item.isSelected());
        holder.name.setTag(item);

        return convertView;

    }
    public void removeFile(String item){
        try{
            //File inputFile = new File(MainActivity.fileName);
            //File tempFile = new File("myTempFile.txt");

            //BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            //BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile,true));
            FileOutputStream fileOutputStream=context.openFileOutput("myTempFile.txt", context.MODE_APPEND);
            OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fileOutputStream);
            BufferedWriter writer=new BufferedWriter(outputStreamWriter);

            FileInputStream fileInputStream=context.openFileInput(MainActivity.fileName);
            InputStreamReader inputStreamReader=new InputStreamReader(fileInputStream);
            BufferedReader reader=new BufferedReader(inputStreamReader);

            String lineToRemove = item;
            String currentLine;

            while((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if(trimmedLine.equals(lineToRemove)) continue;
                writer.write(currentLine);
                writer.newLine();
            }
            writer.close();
            reader.close();
            (new File(context.getFilesDir()+"/"+MainActivity.fileName)).delete();
            File file = new File(context.getFilesDir()+"/"+"myTempFile.txt");
            file.renameTo(new File(context.getFilesDir()+"/"+MainActivity.fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
