package com.e7systems.jukedj_hub;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ServerSocketFactory;

/**
 * Created by Admin on 6/25/2015.
 */
public class NetHandlerThread extends Thread {
    private static final int SONGS_PER_USER = 5;
    private final MainActivity activity;
    private ServerSocket serverSocket;
    private long MAX_DURATION = 60000 * 4;
    public NetHandlerThread(MainActivity activity) {
        this.activity = activity;
    }
    @Override
    public void run() {
        try {
            serverSocket = ServerSocketFactory.getDefault().createServerSocket(MainActivity.PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("JukeDJDeb", "Started listener.");
        while(true) {
            try {
                Log.d("JukeDJDeb", "Waiting for Socket...");
                Socket socket = serverSocket.accept();
                Log.d("JukeDJDeb", "Accepted Socket.");
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final int id = in.read();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity.getApplicationContext(), "ID " + id, Toast.LENGTH_SHORT).show();
                    }
                });
                String result = "";
                String val;
                while((val = in.readLine()) != null) {
                    Log.d("JukeDJDeb", val);
                    result += val;
                }
                int totalSongs = 0;
                for(String artist : getArtists(result)) {
                    JSONArray songs = APIInterface.search(URLEncoder.encode(artist, "UTF-8"));
                    for (int i = 0; i < songs.length() && totalSongs < SONGS_PER_USER; i++) {
                        JSONObject songObj = songs.getJSONObject(i);
                        if(songObj.getBoolean("streamable") && songObj.getLong("duration") <= MAX_DURATION) {
                            if(!activity.songQueue.contains(songObj.getInt("id"))) {
                                activity.songQueue.add(songObj.getInt("id"));
                                totalSongs++;
                                Log.d("JukeDJDeb", "Added " + songObj.getInt("id"));
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> getArtists(String parse) throws JSONException {
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

}
