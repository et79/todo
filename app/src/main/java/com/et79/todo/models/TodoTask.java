package com.et79.todo.models;

import com.et79.todo.Constants;

import java.io.Serializable;

/**
 * Created by eisuke on 2016/11/17.
 */

public class TodoTask implements Serializable {

    private static final String TAG = "TodoTask";

    private String mDateStr;
    private String mTitle;
    private String mContent;
    private String mPhotoUrl;
    private int mIndex = Constants.NUM_UNDEFINED;

    public TodoTask() {}

    public TodoTask(String dateStr, String title, String content, String photoUrl, int index){
        this.mDateStr = dateStr;
        this.mTitle = title;
        this.mContent = content;
        this.mPhotoUrl = photoUrl;
        this.mIndex = index;
    }

    public String getDateStr() {
        return mDateStr;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getContent() {
        return mContent;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setDateStr(String mDateStr) {
        this.mDateStr = mDateStr;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public void setPhotoUrl(String photoUrl) {
        this.mPhotoUrl = photoUrl;
    }

    public void setIndex(int index) {
        this.mIndex = index;
    }
}
