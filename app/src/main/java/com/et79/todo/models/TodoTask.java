package com.et79.todo.models;

import com.et79.todo.ui.TaskEditActivity;

import java.io.Serializable;

/**
 * Created by eisuke on 2016/11/17.
 */

public class TodoTask implements Serializable {

    private String dateStr = "";
    private String title = "";
    private String content = "";
    private String photoUrl = "";
    private int position = -1;

    public TodoTask() {}

    public TodoTask(String dateStr, String title, String content, String photoUrl, int position){
        this.dateStr = dateStr;
        this.title = title;
        this.content = content;
        this.photoUrl = photoUrl;
        this.position = position;
    }

    public String getDateStr() {
        return dateStr;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public int getPosition() {
        return position;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
