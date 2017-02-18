package com.dev.fondson.NoteLocker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
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

import java.util.ArrayList;
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

        binding.rlToolbar.setVisibility(View.GONE);
        binding.checkBox.setOnCheckedChangeListener(null);
        binding.imageButton.setOnClickListener(null);

        if (parentPosition == ItemList.TODO) {
            binding.rlToolbar.setVisibility(View.VISIBLE);
            binding.imageButton.setImageDrawable(context.getResources().getDrawable(R.drawable.star_selector));
            binding.imageButton.setSelected(item.isSelected());
            binding.checkBox.setChecked(false);
            binding.checkBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton arg0, boolean isChecked) {
                    if (isChecked) {
                        Log.d("testingtheory", "adapter: " + item.notification.toString());
                        item.notification.cancelNotif();
                        Firebase.removeToDoItem(item.getKey());
                        Firebase.writeNewCompletedItem(item.getName(),item.isSelected());
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
            binding.itemSettings.setOnClickListener(new View.OnClickListener(){
                public void onClick(View button) {
                    ArrayList<ItemPickerDialogFragment.Item> pickerItems = new ArrayList<>();
                    pickerItems.add(new ItemPickerDialogFragment.Item("Edit item", "Edit"));
                    pickerItems.add(new ItemPickerDialogFragment.Item("Set notification", "Notif"));

                    ItemPickerDialogFragment dialog = ItemPickerDialogFragment.newInstance(
                            "Choose action",
                            pickerItems,
                            -1
                    );
                    Bundle bund = dialog.getArguments();
                    bund.putString("key", item.getKey());
                    dialog.show(((Activity)context).getFragmentManager(), "ItemPicker");
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
