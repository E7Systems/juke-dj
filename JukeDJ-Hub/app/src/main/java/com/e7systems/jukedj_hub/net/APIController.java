package com.e7systems.jukedj_hub.net;

import com.e7systems.jukedj_hub.entities.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Admin on 6/25/2015.
 */
public class APIController {
    /**
     * Search for all songs matching a specific query
     * @param query The query string to search for
     * @return Resulting search data as JSON Array
     */
    public static JSONArray search(String query) {
        try {
            return new JSONArray(sendGetRequest("http://api.soundcloud.com/tracks?q=" + query));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    /**
     * Get information regarding a specific song
     * @param id The ID of a song
     * @param cid The client ID of api account
     * @return an instance of the 'Song' object
     */
    public static Song getSongInfo(int id, String cid) {
        try {
            return Song.fromJSON(new JSONObject(sendGetRequest("http://api.soundcloud.com/tracks/%s.json?client_id=%s", id + "", cid)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Send a get request to the specified uri formatted with the specified parameters.
     * @param uri The destination uri
     * @param params These will be used in String.format().
     * @return
     */
    private static String sendGetRequest(String uri, String... params) {
        try {
            if(params != null && params.length > 0) {
                uri = String.format(uri, params);
            }
            URL url = new URL(uri);
            URLConnection connection = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = "";
            String line;
            while((line = reader.readLine()) != null) {
                result += line;
            }
            reader.close();
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
