package com.mobiletv.app.pojo;

import androidx.annotation.Keep;

@Keep
public class Carousel {

    private String address = "Unknown";
    private String description = "Unknown";
    private String image = "Unknown";
    private String key = "Unknown";
    private String title = "Unknown";

    public Carousel() {
        // Need Constructor
    }

    public Carousel(String address, String description, String image, String key, String title) {

        this.address = address;
        this.description = description;
        this.image = image;
        this.key = key;
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}