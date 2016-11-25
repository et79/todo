package com.et79.todo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;

import com.et79.todo.R;
import com.et79.todo.models.TodoTask;
import com.et79.todo.util.util;

import java.util.Date;

public class TaskEditActivity extends AppCompatActivity {

    private EditText mTaskTitleView;
    private EditText mTaskContentView;
    private int mPosition;
    TodoTask orgTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // UPナビゲーションを有効化する
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if( intent != null ) {
            orgTask = (TodoTask)intent.getSerializableExtra("task");
            mTaskTitleView = (EditText) findViewById(R.id.task_title);
            mTaskContentView = (EditText) findViewById(R.id.task_content);
            setTask(orgTask);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // いつものUPナビゲーションの処理
        switch (id) {
            case android.R.id.home:
                if (isEdit()) {
                    setEditResult();
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTask( TodoTask task ) {
        if( task != null && mTaskContentView != null && mTaskTitleView != null ) {
            mTaskTitleView.setText(task.getTitle());
            mTaskContentView.setText(task.getContent());
            mPosition = task.getPosition();
        }
    }

    private boolean isEdit() {
        if (!mTaskTitleView.getText().toString().equals(orgTask.getTitle()) ||
                !mTaskContentView.getText().toString().equals(orgTask.getContent()))
            return true;

        return false;
    }

    private void setEditResult() {

        Intent intent = new Intent();
        intent.putExtra ("task", new TodoTask(util.DateToString(new Date()),
                mTaskTitleView.getText().toString(),
                mTaskContentView.getText().toString(),
                "",
                mPosition) );
        setResult(RESULT_OK, intent);
    }

    @Override
    public void onBackPressed() {

        if (isEdit()) setEditResult();

        super.onBackPressed();
    }
}
