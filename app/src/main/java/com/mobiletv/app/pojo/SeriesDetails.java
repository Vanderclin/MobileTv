package com.mobiletv.app.pojo;

import com.google.firebase.database.PropertyName;

import java.util.Map;

public class SeriesDetails {

    private String cover;
    private Map<String, EpisodeDetails> episodes;
    private String description;
    private String title;
    private int views;


    public SeriesDetails() {
        // Construtor
    }

    @PropertyName("cover")
    public String getCover() {
        return cover;
    }

    @PropertyName("cover")
    public void setCover(String cover) {
        this.cover = cover;
    }

    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("title")
    public String getTitle() {
        return title;
    }

    @PropertyName("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @PropertyName("episodes")
    public Map<String, EpisodeDetails> getEpisodes() {
        return episodes;
    }

    @PropertyName("episodes")
    public void setEpisodes(Map<String, EpisodeDetails> episodes) {
        this.episodes = episodes;
    }

    @PropertyName("views")
    public int getViews() { return views; }

    @PropertyName("views")
    public void setViews(int views) { this.views = views; }
}
