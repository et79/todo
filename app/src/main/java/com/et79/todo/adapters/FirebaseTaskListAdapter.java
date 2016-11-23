package com.et79.todo.adapters;

import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.et79.todo.ui.FirebaseTaskViewHolder;
import com.et79.todo.models.TodoTask;
import com.et79.todo.ui.MainActivity;
import com.et79.todo.util.ItemTouchHelperAdapter;
import com.et79.todo.util.OnStartDragListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by eisuke on 2016/11/22.
 */

public class FirebaseTaskListAdapter extends FirebaseRecyclerAdapter<TodoTask, FirebaseTaskViewHolder> implements ItemTouchHelperAdapter {

    private DatabaseReference mRef;
    private OnStartDragListener mOnStartDragListener;
    private Context mContext;

    public FirebaseTaskListAdapter(Class<TodoTask> modelClass, int modelLayout,
                                   Class<FirebaseTaskViewHolder> viewHolderClass,
                                   Query ref, OnStartDragListener onStartDragListener, Context context) {

        super(modelClass, modelLayout, viewHolderClass, ref);
        mRef = ref.getRef();
        mOnStartDragListener = onStartDragListener;
        mContext = context;
    }

    @Override
    protected void populateViewHolder(final FirebaseTaskViewHolder viewHolder, TodoTask model, int position) {
        viewHolder.bindTodoTask(model);
        ((MainActivity) mContext).progressBarVisible(ProgressBar.INVISIBLE);

        viewHolder.taskTitleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mOnStartDragListener.onStartDrag(viewHolder);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        getRef(position).removeValue();
    }

    @Override
    public FirebaseTaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        {
            final FirebaseTaskViewHolder vh = super.onCreateViewHolder(parent, viewType);
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) mContext).startTaskEditActivity(vh.getTodoTask());
                }
            });
            return vh;
        }

//        return super.onCreateViewHolder(parent, viewType);
    }
}