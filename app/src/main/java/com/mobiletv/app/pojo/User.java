package com.mobiletv.app.pojo;

public class User {
    private boolean isAdmin = false;

    public User() {
        // Constructor
    }

    public User(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
