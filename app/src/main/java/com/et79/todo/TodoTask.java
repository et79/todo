package com.et79.todo;

/**
 * Created by eisuke on 2016/11/17.
 */

public class TodoTask {

    private String dateStr;
    private String title;
    private String content;
    private String photoUrl;

    public TodoTask() {}

    public TodoTask(String dateStr, String title, String content, String photoUrl){
        this.dateStr = dateStr;
        this.title = title;
        this.content = content;
        this.photoUrl = photoUrl;
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
}
