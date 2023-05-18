package com.mobiletv.app.pojo;

import androidx.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@Keep
@IgnoreExtraProperties
public class Series {
    private String cover = "";
    private String description = "";
    private String title = "";

    public Series() {
        // Constructor
    }

    public Series(String cover, String description, String title) {
        this.cover = cover;
        this.description = description;
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

