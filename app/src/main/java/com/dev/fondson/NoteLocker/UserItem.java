package com.dev.fondson.NoteLocker;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Fondson on 2015-12-21.
 */
public class UserItem {
    private long id = 0;
    private String key = null;
    private String name = null;
    private boolean selected = false;
    private int colour = 0;

    // constructor used by Firebase
    public UserItem(){}

    public UserItem(String key, String name, boolean selected) {
        this.key = key;
        this.name = name;
        this.selected = selected;
        setColour();
    }

    public long getId(){
        return this.id;
    }
    public void setId(long id){
        this.id=id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getKey() {
        return this.key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public boolean isSelected() {
        return this.selected;
    }
    public int getColour(){
        return colour;
    }
    public void setColour(int colour) {
        this.colour = colour;
        Log.d("COLOUR", "setColour: " + String.valueOf(colour));
    }
    public void setColour(){
        int intColour;
        if (selected) intColour = PaletteTask.vibrantColour;
        else intColour = PaletteTask.mutedColour;
        setColour(intColour);
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
        setColour();
    }
    public Map<String, Object> toMap(){
        HashMap<String, Object> item = new HashMap<>();
        item.put("key", key);
        item.put("name", name);
        item.put("selected", selected);

        return item;
    }
}
