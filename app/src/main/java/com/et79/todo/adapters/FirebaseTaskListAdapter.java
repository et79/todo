package com.et79.todo.adapters;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.et79.todo.ui.FirebaseTaskViewHolder;
import com.et79.todo.models.TodoTask;
import com.et79.todo.ui.MainActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by eisuke on 2016/11/22.
 */

public class FirebaseTaskListAdapter
        extends FirebaseRecyclerAdapter<TodoTask, FirebaseTaskViewHolder>
        implements ItemTouchHelperAdapter {

    private static final String TAG = "FirebaseTaskListAdapter";

    private DatabaseReference mDbRef;
    private FirebaseTaskListEventListener mFirebaseTaskListEventListener;
    private Context mContext;
    private boolean mIsNeedUpdateIndex = false;

    private ChildEventListener mChildEventListener;
    private ArrayList<TodoTask> mTasks = new ArrayList<>();

    public FirebaseTaskListAdapter(Class<TodoTask> modelClass, int modelLayout,
                                   Class<FirebaseTaskViewHolder> viewHolderClass,
                                   Query ref, FirebaseTaskListEventListener firebaseTaskListEventListener, Context context) {

        super(modelClass, modelLayout, viewHolderClass, ref);
        mDbRef = ref.getRef();
        mFirebaseTaskListEventListener = firebaseTaskListEventListener;
        mContext = context;

        mChildEventListener = mDbRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded");
                mTasks.add(0, dataSnapshot.getValue(TodoTask.class));
                mFirebaseTaskListEventListener.onAddItem();
                mIsNeedUpdateIndex = true;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged");

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved");

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled");

            }
        });
    }

    @Override
    protected void populateViewHolder(final FirebaseTaskViewHolder viewHolder, TodoTask model, int position) {
        Log.d(TAG, "populateViewHolder");
        viewHolder.bindTodoTask(model);
        viewHolder.mTaskReorder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch");
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mFirebaseTaskListEventListener.onStartDrag(viewHolder);
                }
                return false;
            }
        });

        mFirebaseTaskListEventListener.onPopulateViewHolder();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Log.d(TAG, "onItemMove");
        Collections.swap(mTasks, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        mIsNeedUpdateIndex = true;
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        Log.d(TAG, "onItemDismiss");
        mTasks.remove(position);
        getRef(position).removeValue();
        mIsNeedUpdateIndex = true;

        mFirebaseTaskListEventListener.onItemDismiss();
    }

    @Override
    public FirebaseTaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");

        final FirebaseTaskViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");
                mFirebaseTaskListEventListener.onClickItem(viewHolder);
            }
        });

        return viewHolder;

//        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void cleanup() {
        Log.d(TAG, "cleanup");
        super.cleanup();
        setIndexInFirebase();
        mDbRef.removeEventListener(mChildEventListener);
    }

    public void setIndexInFirebase() {
        Log.d(TAG, "setIndexInFirebase");

        if (mIsNeedUpdateIndex) {
            for (TodoTask task : mTasks) {
                int index = mTasks.indexOf(task);
                task.setIndex(index);
                getRef(index).setValue(task);
                mIsNeedUpdateIndex = false;
            }
        }
    }
}