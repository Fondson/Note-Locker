package com.example.fondson.mylockscreen;

/**
 * Created by Fondson on 2015-12-21.
 */
public class Item {
    String name = null;
    boolean selected = false;

    public Item(String name, boolean selected) {
        this.name = name;
        this.selected = selected;
    }
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
