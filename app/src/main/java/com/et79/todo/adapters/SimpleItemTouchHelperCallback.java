package com.et79.todo.adapters;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.et79.todo.R;
import com.et79.todo.adapters.ItemTouchHelperAdapter;

/**
 * Created by eisuke on 2016/11/22.
 */

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private static final String TAG = "SimpleItemTouchHelperCallback";

    private final ItemTouchHelperAdapter mAdapter;

    //  This constructor takes an ItemTouchHelperAdapter parameter. When implemented in
    //  FirebaseRestaurantListAdapter, the ItemTouchHelperAdapter instance will pass the gesture event back to the
    //  Firebase adapter where we will define what occurs when an item is moved or dismissed.

    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    //  The method below informs the ItemTouchHelperAdapter that drag gestures are enabled.
    //  We could also disable drag gestures by returning 'false'.

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    //  The method below informs the ItemTouchHelperAdapter that swipe gestures are enabled.
    //  We could also disable them by returning 'false'.

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    //  getMovementFlags informs the ItemTouchHelper which movement directions are supported.
    // For example, when a user drags a list item, they press 'Down' to begin the drag and lift their finger, 'Up',  to end the drag.

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlags = ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    //  The method below notifies the adapter that an item has moved.
    //  This triggers the onItemMove override in our Firebase adapter,
    //  which will eventually handle updating the restaurants ArrayList to reflect the item's new position.

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source,
                          RecyclerView.ViewHolder target) {
        if (source.getItemViewType() != target.getItemViewType()) {
            return false;
        }
        mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    //  The method below notifies the adapter that an item was dismissed.
    //  This triggers the onItemDismiss override in our Firebase adapter
    //  which will eventually handle deleting this item from the user's "Saved Restaurants" in Firebase.

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Get RecyclerView item from the ViewHolder
            View itemView = viewHolder.itemView;

            Paint p = new Paint();
            if (dX > 0) {
                /* Set your color for positive displacement */
                p.setColor(ContextCompat.getColor(itemView.getContext(), R.color.colorAccent));

                // Draw Rect with varying right side, equal to displacement dX
                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                        (float) itemView.getBottom(), p);

                // Set the image icon for Right swipe
                Resources res = itemView.getContext().getResources();
                Bitmap icon = BitmapFactory.decodeResource(res, R.drawable.ic_done_white_36dp);
                c.drawBitmap(icon,
                        (float) itemView.getLeft() + res.getDimension(R.dimen.activity_horizontal_margin)/2,
                        (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight())/2,
                        p);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
