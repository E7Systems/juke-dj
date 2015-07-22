package com.e7systems.jukedj_hub.entities;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dylan Katz on 7/22/2015.
 */
public class Song {
    private int id;
    private String name;
    public Song(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Song fromJSON(JSONObject object) throws JSONException {
        int id = object.getInt("id");
        String name = object.getString("title");
        return new Song(id, name);
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
}
