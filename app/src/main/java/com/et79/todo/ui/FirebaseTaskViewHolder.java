package com.et79.todo.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.et79.todo.R;
import com.et79.todo.models.TodoTask;

/**
 * Created by eisuke on 2016/11/22.
 */

public class FirebaseTaskViewHolder extends RecyclerView.ViewHolder {
    public TextView taskTitleView;
    public TextView taskContentView;
    private TodoTask mTodoTask;

    public FirebaseTaskViewHolder(View v) {
        super(v);
        taskTitleView = (TextView) itemView.findViewById(R.id.task_title);
        taskContentView = (TextView) itemView.findViewById(R.id.task_content);
    }

    public void bindTodoTask(TodoTask task) {

        mTodoTask = task;
        mTodoTask.setPosition(getAdapterPosition());

        taskTitleView = (TextView) itemView.findViewById(R.id.task_title);
        taskContentView = (TextView) itemView.findViewById(R.id.task_content);

        taskTitleView.setText(task.getTitle());
        taskContentView.setText(task.getContent());
    }

    public TodoTask getTodoTask() {
        return mTodoTask;
    }
}