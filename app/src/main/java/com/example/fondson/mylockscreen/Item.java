package com.example.fondson.mylockscreen;

/**
 * Created by Fondson on 2015-12-21.
 */
public class Item {
    long id;
    String name = null;
    boolean selected = false;

    public Item(long id, String name, boolean selected) {
        this.id=id;
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
    public boolean isSelected() {
        return this.selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
