package com.e7systems.jukedj_hub.util;

import android.util.Log;

import com.e7systems.jukedj_hub.APIController;
import com.e7systems.jukedj_hub.MainActivity;
import com.e7systems.jukedj_hub.entities.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Dylan Katz on 7/24/2015.
 */
public class APIDataHandler {
    private static final int SONGS_PER_USER = 5;
    private static long MAX_DURATION = 60000 * 4;
    private static Random random = new Random();

    /**
     * Retrieve a lists of artists from a user's musical preferences.
     * @param parse The raw JSON from facebook's API sent by the client
     * @return A list of artists
     * @throws JSONException
     */
    public static List<String> getArtists(String parse) throws JSONException {
        List<String> artists = new ArrayList<>();
        JSONArray music = new JSONArray(parse);
        for (int i = 0; i < music.length(); i++) {
            JSONObject musicItem = music.getJSONObject(i);
            String artist = musicItem.getString("name");
            Log.d("JukeDJDeb", artist);
            artists.add(artist);
        }
        return artists;
    }

    /**
     * Get a list of songs from multiple given artists
     * @param artists Artists of various songs and/or search queries
     * @return A list of songs that match the given queries.
     * @throws UnsupportedEncodingException
     * @throws JSONException
     */
    public static List<Song> fetchSongs(List<String> artists) throws UnsupportedEncodingException, JSONException {
        int totalSongs = 0;
        int startIdx = random.nextInt(SONGS_PER_USER);
        List<Song> songsToAdd = new ArrayList<>();

        for(String artist : artists) {

            JSONArray songs = APIController.search(URLEncoder.encode(artist, "UTF-8"));
            for (int i = startIdx; i < songs.length() && totalSongs < SONGS_PER_USER; i++) {
                JSONObject songObj = songs.getJSONObject(i);

                if(songObj.getBoolean("streamable") && songObj.getLong("duration") <= MAX_DURATION) {
                    int songId = songObj.getInt("id");
                    Song song = APIController.getSongInfo(songId, MainActivity.CLIENT_ID);
                    songsToAdd.add(song);
                    totalSongs++;
                }
            }
        }
        return songsToAdd;
    }

}
