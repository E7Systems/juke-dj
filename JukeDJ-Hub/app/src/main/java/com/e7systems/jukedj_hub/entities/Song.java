package com.e7systems.jukedj_hub.entities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by Dylan Katz on 7/22/2015.
 */
public class Song {
    private int id;
    private UUID ownerUUID;
    private String name;
    private int playbacks;

    public Song(int id, UUID ownerUUID, String name, int playbacks) {
        this.id = id;
        this.name = name;
        this.playbacks = playbacks;
    }

    public static Song fromJSON(JSONObject object) throws JSONException {
        int id = object.getInt("id");
        String name = object.getString("title");
        int playbacks = object.getInt("playback_count");
        return new Song(id, UUID.randomUUID(), name, playbacks);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlaybacks() {
        return playbacks;
    }
}
