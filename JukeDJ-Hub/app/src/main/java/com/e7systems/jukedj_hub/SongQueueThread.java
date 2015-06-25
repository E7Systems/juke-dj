package com.e7systems.jukedj_hub;

import android.media.MediaPlayer;
import android.util.Log;

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
        if(main.songQueue.size() <= 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            playMusic();
            return;
        }
        main.stream(main.songQueue.get(0).toString(), new Callback<MediaPlayer>() {
            @Override
            public void call(MediaPlayer obj) {
                main.songQueue.remove(0);
                if(main.songQueue.size() > 0) {
                    Log.d("JukeDJDeb", "Playing new song: " + main.songQueue.get(0).toString());
                }
                playMusic();
            }
        });
    }
}
