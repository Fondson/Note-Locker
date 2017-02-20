package com.dev.fondson.NoteLocker;

import com.bignerdranch.expandablerecyclerview.model.Parent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Fondson on 2017-02-04.
 */

public class ItemList implements Parent<UserItem> {
    public static int TODO = 0;
    public static int COMPLETED = 1;
    private String name;
    private LinkedList<UserItem> itemList;

    public ItemList(String name, LinkedList<UserItem> userItems) {
        this.name = name;
        itemList = userItems;
    }

    @Override
    public LinkedList<UserItem> getChildList() {
        return itemList;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return true;
    }

    public String getName(){
        return name;
    }
}
