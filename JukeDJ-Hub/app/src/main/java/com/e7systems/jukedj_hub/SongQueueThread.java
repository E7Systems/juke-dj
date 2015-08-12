package com.e7systems.jukedj_hub;

import android.media.MediaPlayer;
import android.text.Html;
import android.widget.TextView;

import com.e7systems.jukedj_hub.entities.Song;
import com.e7systems.jukedj_hub.net.NetHandlerThread;
import com.e7systems.jukedj_hub.net.packets.PacketMakeNotify;
import com.e7systems.jukedj_hub.net.packets.PacketSongFinished;
import com.e7systems.jukedj_hub.util.SongQueue;

import java.io.IOException;

/**
 * Created by Admin on 6/25/2015.
 */
public class SongQueueThread extends Thread {
    private MainActivity main;
    private static SongQueueThread instance;
    private int songsPlayed = 0;
    private SongQueue queue = new SongQueue(new Callback<Boolean>() {
        @Override
        public void call(Boolean obj) {
            skip(obj);
        }
    });

    public SongQueueThread(MainActivity main) {
        this.main = main;
        instance = this;
    }

    @Override
    public void run() {
        playMusic();
    }

    /**
     * Starts music loop, this is a recursive method so that we can continue the loop once the song has finished.
     */
    public void playMusic() {
        //Wait for new song
        final Song song = waitForSong();

        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView songPlaying = (TextView) main.findViewById(R.id.tb_SongPlaying);
                songPlaying.setText(Html.fromHtml("Now playing:<br><font color=#868383>"
                        + Html.escapeHtml(song.getName()) + "</font>"));
            }
        });

        //Alert client their song is playing.
        NetHandlerThread.getInstance().writePacket(new PacketMakeNotify("JukeDJ", "The song, '"
                + song.getName() + "' is playing based upon your preferences!", false), song.getOwnerIp());

        //If we've played 5 songs, play an ad. Also displays on the first song.
        if(songsPlayed % 5 == 0 && !main.hasRemovedAds() && main.isAdCompleted()) {
            main.setAdContentVisible(true);
        }
        while(!main.isAdCompleted()); //wait for advert to finish
        main.setAdContentVisible(false);
        main.stream(song, new Callback<MediaPlayer>() {
            @Override
            public void call(MediaPlayer obj) {
                try {
                    NetHandlerThread.getInstance().broadcastPacket(new PacketSongFinished());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                songsPlayed++;
                playMusic();
            }
        });
    }

    //If there's no songs in the queue, wait in a loop for new songs to appear.
    public Song waitForSong() {
        Song song;
        while (((song = queue.pop()) == null || main.mediaPlayer.isPlaying())) {

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return song;
    }

    public void skip(boolean songOwner) {
        //if we want to handle owner skips differently
       /* if(songOwner) {
            playMusic();
        } else {

        }*/
        try {
            NetHandlerThread.getInstance().broadcastPacket(new PacketSongFinished());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //kill the current song
        if(main.mediaPlayer.isPlaying()) {
            main.mediaPlayer.stop();
        }

        //play the next one
        playMusic();
    }

    public static SongQueueThread getInstance() {
        return instance;
    }
}
