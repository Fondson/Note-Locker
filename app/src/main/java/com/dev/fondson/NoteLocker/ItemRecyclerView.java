package com.dev.fondson.NoteLocker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by Fondson on 2017-03-01.
 */

public class ItemRecyclerView extends RecyclerView {
    public ItemRecyclerView(Context context) {
        super(context);
    }

    public ItemRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        return 0;
    }
}
