package com.et79.todo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import com.et79.todo.Constants;
import com.et79.todo.R;
import com.et79.todo.models.TodoTask;
import com.et79.todo.util.util;

import java.util.Date;


public class TaskEditActivity extends AppCompatActivity {

    private static final String TAG = "TaskEditActivity";

    private EditText mTaskTitleView;
    private EditText mTaskContentView;
    private int mPosition = Constants.NUM_UNDEFINED;
    private TodoTask mOrgTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // UPナビゲーションを有効化する
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if( intent != null ) {
            mOrgTask = (TodoTask)intent.getSerializableExtra(Constants.STR_TASK);
            mPosition = intent.getIntExtra(Constants.STR_POSITION, Constants.NUM_UNDEFINED);
        }

        setUpTask();
    }

    private void setUpTask() {
        Log.d(TAG, "setUpTask");

        mTaskTitleView = (EditText) findViewById(R.id.task_title);
        mTaskTitleView.setText(mOrgTask.getTitle());

        mTaskContentView = (EditText) findViewById(R.id.task_content);
        mTaskContentView.setText(mOrgTask.getContent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        switch (item.getItemId()) {
            case android.R.id.home:
                setEditResult();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        setEditResult();
        super.onBackPressed();
    }

    private void setEditResult() {
        Log.d(TAG, "setEditResult");

        if( !isEdit() )
            return;

        Intent intent = new Intent();
        intent.putExtra (Constants.STR_TASK, new TodoTask(util.DateToString(new Date()),
                mTaskTitleView.getText().toString(),
                mTaskContentView.getText().toString(),
                "",
                mOrgTask.getIndex()) );
        intent.putExtra(Constants.STR_POSITION, mPosition);

        setResult(RESULT_OK, intent);
    }

    private boolean isEdit() {
        Log.d(TAG, "isEdit");

        if (!mTaskTitleView.getText().toString().equals(mOrgTask.getTitle()) ||
                !mTaskContentView.getText().toString().equals(mOrgTask.getContent()))
            return true;

        return false;
    }
}
