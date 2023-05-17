package com.mobiletv.app.pojo;

import androidx.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

@Keep
@IgnoreExtraProperties
public class EpisodeDetails {
    private String address;
    private String cover;
    private String title;

    public EpisodeDetails() {
        // Construtor vazio necessário para o Firebase
    }

    public EpisodeDetails(String address, String cover, String title) {
        this.address = address;
        this.cover = cover;
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCover() { return cover; }

    public void setCover(String cover) { this.cover = cover; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
