package com.e7systems.jukedj_hub;

import android.media.MediaPlayer;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Admin on 6/25/2015.
 */
public class SongQueueThread extends Thread {
    private MainActivity main;
    public SongQueueThread(MainActivity main) {
        this.main = main;
    }

    @Override
    public void run() {
        playMusic();
    }
    public void playMusic() {
        while(main.songQueue.size() <= 0) {
            Log.d("JukeDJDeb", "No music :(");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Log.d("JukeDJDeb", "Playing new song: " + main.songQueue.get(0).toString());
//        JSONObject songInfo = APIInterface.getSongInfo(main.songQueue.get(0).toString(), "437d961ac979c05ea6bae1d5cb3993ec");
//        try {
//            String title = songInfo.getString("title");
//            main.setPlayingTitle(title);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        main.stream(main.songQueue.get(0).toString(), new Callback<MediaPlayer>() {
            @Override
            public void call(MediaPlayer obj) {
                main.songQueue.remove(0);
                if(main.songQueue.size() > 0) {

                }
                playMusic();
            }
        });
    }
}
