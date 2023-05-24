package com.mobiletv.app.pojo;

public class AccountData {

    private long access;
    private boolean admin;
    private String device;
    private String email;
    private String name;
    private int points;
    private long timestamp;
    private String uid;

    public AccountData() {
        // Need Constructor
    }

    public AccountData(long access, boolean admin, String device, String email, String name, int points, long timestamp, String uid) {
        this.access = access;
        this.admin = admin;
        this.device = device;
        this.email = email;
        this.name = name;
        this.points = points;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public long getAccess() {
        return access;
    }

    public boolean getAdmin() {
        return admin;
    }

    public String getDevice() {
        return device;
    }

    public String getEmail() {
        return email;
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
