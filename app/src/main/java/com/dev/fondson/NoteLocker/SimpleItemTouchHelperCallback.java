package com.dev.fondson.NoteLocker;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

/**
 * Created by Fondson on 2017-02-17.
 */

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mAdapter;

    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return viewHolder instanceof ItemViewHolder ? makeMovementFlags(dragFlags, swipeFlags) : 0;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder,
                          ViewHolder target) {
        Log.d("onMoveVieldholder", "onMove: " + viewHolder.getClass() + " " + target.getClass());
        return mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
    }

    @Override
    public void onSwiped(ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            ItemListAdapter.MOVING = true;
            Log.d("TrackingMovingVar", "onSelectedChanged: " + String.valueOf(ItemListAdapter.MOVING));
        }
        Log.d("onSelectedChanged", "onSelectedChanged: " + String.valueOf(actionState));

        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        ItemListAdapter.MOVING = false;
        Log.d("TrackingMovingVar", "clearView: " + String.valueOf(ItemListAdapter.MOVING));
    }

    @Override
    public boolean canDropOver(RecyclerView recyclerView, ViewHolder current,
                               ViewHolder target) {
        return current.getClass() == target.getClass();
    }
}
