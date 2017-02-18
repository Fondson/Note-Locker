package com.dev.fondson.NoteLocker;

import android.content.Context;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.dev.fondson.NoteLocker.databinding.UserItemBinding;

/**
 * Created by Fondson on 2017-02-04.
 */

public class ItemViewHolder extends ChildViewHolder {
    public final UserItemBinding binding;

    public ItemViewHolder(UserItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Context context, UserItem item) {
        binding.setItem(item);
        binding.setNotif(item.notification);
        binding.executePendingBindings();
    }
}
