package com.e7systems.jukedj_hub;

import android.media.MediaPlayer;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.e7systems.jukedj_hub.entities.Song;
import com.e7systems.jukedj_hub.entities.User;
import com.e7systems.jukedj_hub.net.NetHandlerThread;
import com.e7systems.jukedj_hub.net.packets.PacketMakeNotify;
import com.e7systems.jukedj_hub.util.SongQueue;

import java.io.IOException;

/**
 * Created by Admin on 6/25/2015.
 */
public class SongQueueThread extends Thread {
    private MainActivity main;
    private static SongQueueThread instance;
    private SongQueue queue = new SongQueue(new Callback<Boolean>() {
        @Override
        public void call(Boolean obj) {
            skip(obj);
        }
    });
    private MediaPlayer mediaPlayer;

    public SongQueueThread(MainActivity main) {
        this.main = main;
        instance = this;
    }

    @Override
    public void run() {
        playMusic();
    }

    /**
     * CAREFUL: This function is infinitely recursive and runs with delay.
     * The reason for this is so we can wait for a song to finish to play the
     * next song.
     */
    public void playMusic() {
        final Song song;
        if((song = queue.pop()) == null) {
//            Log.d("JukeDJDeb", "Waiting for new songs...");

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            playMusic();
            return;
        }
//        Log.d("JukeDJDeb", "Playing new song: " + song.getName());
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView songPlaying = (TextView) main.findViewById(R.id.tb_SongPlaying);
                songPlaying.setText(Html.fromHtml("Now playing:<br><font color=#868383>" + Html.escapeHtml(song.getName()) + "</font>"));
            }
        });
        try {
            NetHandlerThread.getInstance().writePacket(new PacketMakeNotify("JukeDJ", "A song is playing based upon your preferences!", false), song.getOwnerIp());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        JSONObject songInfo = APIController.getSongInfo(main.songQueue.get(0).toString(), "437d961ac979c05ea6bae1d5cb3993ec");
//        try {
//            String title = songInfo.getString("title");
//            main.setPlayingTitle(title);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        mediaPlayer = main.stream(song, new Callback<MediaPlayer>() {
            @Override
            public void call(MediaPlayer obj) {
                playMusic();
            }
        });
    }

    public void skip(boolean songOwner) {
        //if we want to handle owner skips differently
       /* if(songOwner) {
            playMusic();
        } else {

        }*/
        playMusic();
    }

    public static SongQueueThread getInstance() {
        return instance;
    }
}
