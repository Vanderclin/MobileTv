package com.mobiletv.app.pojo;

import androidx.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

@Keep
@IgnoreExtraProperties
public class Account {
    private boolean isAdmin = false;
    private int points = 0;
    private long timestamp = 0L;

    public Account() {
        // Constructor
    }

    public Account(boolean isAdmin, int points, long timestamp) {
        this.isAdmin = isAdmin;
        this.points = points;
        this.timestamp = timestamp;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public int getPoints() { return points; }

    public void setPoints(int points) { this.points = points; }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
