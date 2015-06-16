package com.e7systems.jukedj;

/**
 * Created by Dylan on 6/9/2015.
 */
public class MusicSelection {
    private String videoLink;
    private String embedLink;
    private String playerId;
    public MusicSelection(String videoLink) {
        this.videoLink = videoLink;
        embedLink = getEmbedLink(videoLink);
    }

    private String getEmbedLink(String link) {
        String extension = link.substring(link.indexOf("=") + 1);
        return extension;
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }

    public String getPlayerId() throws Exception {
        throw new Exception("Not implemented.");
    }

    public String getEmbedLink() {
        return embedLink;
    }
}
