package com.dev.fondson.NoteLocker;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.dev.fondson.NoteLocker.databinding.ItemlistTextviewBinding;

/**
 * Created by Fondson on 2017-02-04.
 */

public class ItemListViewHolder extends ParentViewHolder {
    private final ItemlistTextviewBinding binding;

    public ItemListViewHolder(ItemlistTextviewBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ItemList itemList) {
        binding.setItemList(itemList);
        binding.executePendingBindings();
    }
}
