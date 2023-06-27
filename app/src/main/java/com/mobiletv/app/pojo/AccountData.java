package com.mobiletv.app.pojo;

import androidx.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

@Keep
@IgnoreExtraProperties
public class AccountData {

    private long access;
    private boolean admin;
    private String device;
    private String email;
    private boolean member;
    private String name;
    private int points;
    private long timestamp;
    private String uid;

    public AccountData() {
        // Need Constructor
    }

    public AccountData(long access, boolean admin, String device, String email, boolean member, String name, int points, long timestamp, String uid) {
        this.access = access;
        this.admin = admin;
        this.device = device;
        this.email = email;
        this.member = member;
        this.name = name;
        this.points = points;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public long getAccess() {
        return access;
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getDevice() {
        return device;
    }

    public String getEmail() {
        return email;
    }

    public boolean isMember() {
        return member;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUid() {
        return uid;
    }

}
