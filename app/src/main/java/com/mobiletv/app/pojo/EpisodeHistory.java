package com.mobiletv.app.pojo;

public class EpisodeHistory {
    private String episodeTitle;
    private String episodeAddress;
    private int episodePosition;

    public EpisodeHistory() {
        // Construtor vazio necessário para o Firebase
    }

    public EpisodeHistory(String episodeTitle, String episodeAddress, int episodePosition) {
        this.episodeTitle = episodeTitle;
        this.episodeAddress = episodeAddress;
        this.episodePosition = episodePosition;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    public String getEpisodeAddress() {
        return episodeAddress;
    }

    public void setEpisodeAddress(String episodeAddress) {
        this.episodeAddress = episodeAddress;
    }

    public int getEpisodePosition() {
        return episodePosition;
    }

    public void setEpisodePosition(int episodePosition) {
        this.episodePosition = episodePosition;
    }
}
