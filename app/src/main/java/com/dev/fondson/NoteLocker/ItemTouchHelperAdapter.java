package com.dev.fondson.NoteLocker;

/**
 * Created by Fondson on 2017-02-17.
 */

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}