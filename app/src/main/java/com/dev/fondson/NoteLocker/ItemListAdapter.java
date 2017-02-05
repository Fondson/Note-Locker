package com.dev.fondson.NoteLocker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.dev.fondson.NoteLocker.databinding.ItemlistTextviewBinding;
import com.dev.fondson.NoteLocker.databinding.UserItemBinding;

import java.util.LinkedList;

/**
 * Created by Fondson on 2017-02-04.
 */

public class ItemListAdapter extends ExpandableRecyclerAdapter<ItemList, UserItem, ItemListViewHolder, ItemViewHolder> {
    private LayoutInflater inflater;
    private Context context;

    public ItemListAdapter(Context context, @NonNull LinkedList<ItemList> parentItemList) {
        super(parentItemList);
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    // onCreate ...
    @Override
    public ItemListViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        ItemlistTextviewBinding binding = ItemlistTextviewBinding.inflate(inflater, parentViewGroup, false);
        return new ItemListViewHolder(binding);
    }

    @Override
    public ItemViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        UserItemBinding binding = UserItemBinding.inflate(inflater, childViewGroup, false);
        return new ItemViewHolder(binding);
    }

    // onBind ...
    @Override
    public void onBindParentViewHolder(@NonNull ItemListViewHolder itemListViewHolder, int parentPosition, @NonNull ItemList itemList) {
        itemListViewHolder.bind(itemList);
    }

    @Override
    public void onBindChildViewHolder(@NonNull ItemViewHolder itemViewHolder, int parentPosition, int childPosition, @NonNull final UserItem item) {
        itemViewHolder.bind(context, item);
        UserItemBinding binding = itemViewHolder.binding;
        binding.itemName.setPaintFlags(0);

        binding.checkBox.setOnCheckedChangeListener(null);
        binding.itemName.setOnLongClickListener(null);
        binding.imageButton.setOnClickListener(null);

        if (parentPosition == ItemList.TODO) {
            binding.imageButton.setImageDrawable(context.getResources().getDrawable(R.drawable.star_selector));
            binding.imageButton.setSelected(item.isSelected());
            binding.checkBox.setChecked(false);
            binding.checkBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton arg0, boolean isChecked) {
                    if (isChecked) {
                        Firebase.removeToDoItem(item.getKey());
                        Firebase.writeNewCompletedItem(item.getName(),item.isSelected());
                    }
                }
            });

            binding.itemName.setOnLongClickListener(new View.OnLongClickListener() {
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
                                item.setName(newItemName);
                                Firebase.updateToDoItem(item);
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
            binding.imageButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View button) {
                    //Set the button's appearance
                    boolean selected =!button.isSelected();
                    button.setSelected(selected);
                    item.setSelected(selected);
                    if (selected){
                        item.setSelected(true);
                    }
                    Firebase.updateToDoItem(item);

                }

            });
        }
        else if (parentPosition == ItemList.COMPLETED) {
            binding.checkBox.setChecked(true);
            binding.imageButton.setImageDrawable(context.getResources().getDrawable(R.drawable.x));
            binding.itemName.setPaintFlags(binding.itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            binding.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton arg0, boolean isChecked) {
                    if (!isChecked) {
                        Firebase.removeCompletedItem(item.getKey());
                        Firebase.writeNewToDoItem(item.getName(),item.isSelected());
                    }
                }
            });
            binding.imageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View button) {
                    Firebase.removeCompletedItem(item.getKey());
                }

            });
        }
        Log.d("onBindChild", "onBindChildViewHolder: binded to " + item.getName());
    }
}
