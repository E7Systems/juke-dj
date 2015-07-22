package com.e7systems.jukedj_hub;

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
public class APIInterface {
    public static JSONArray search(String query) {
        try {
            return new JSONArray(sendGetRequest("http://api.soundcloud.com/tracks?q=" + query));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public static Song getSongInfo(int id, String cid) {
        try {
            return Song.fromJSON(new JSONObject(sendGetRequest("http://api.soundcloud.com/tracks/%s.json?client_id=%s", id + "", cid)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

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
