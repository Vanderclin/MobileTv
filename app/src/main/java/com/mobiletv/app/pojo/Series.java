package com.mobiletv.app.pojo;

import androidx.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@Keep
@IgnoreExtraProperties
public class Series {
    private String cover;

    public Series() {
        // Constructor
    }

    public Series(String cover) {
        this.cover = cover;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

}

