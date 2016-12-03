package com.dev.fondson.NoteLocker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Fondson on 2015-12-21.
 */
public class UserItem {
    long id = 0;
    String key = null;
    String name = null;
    boolean selected = false;

    public UserItem(){

    }

    public UserItem(long id, String name, boolean selected) {
        this.id=id;
        this.name = name;
        this.selected = selected;
    }

    public UserItem(String key, String name, boolean selected) {
        this.key = key;
        this.name = name;
        this.selected = selected;
    }

    public long getId(){return this.id;}
    public void setId(long id){this.id=id;}
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
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public Map<String, Object> toMap(){
        HashMap<String, Object> item = new HashMap<>();
        item.put("key", key);
        item.put("name", name);
        item.put("selected", selected);

        return item;
    }
}
