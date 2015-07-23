package com.e7systems.jukedj_hub;

import android.media.MediaPlayer;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.e7systems.jukedj_hub.entities.Song;
import com.e7systems.jukedj_hub.util.SongQueue;

/**
 * Created by Admin on 6/25/2015.
 */
public class SongQueueThread extends Thread {
    private MainActivity main;
    private SongQueue queue = new SongQueue();
    public SongQueueThread(MainActivity main) {
        this.main = main;
    }

    @Override
    public void run() {
        playMusic();
    }
    public void playMusic() {
        final Song song;
        if((song = queue.pop()) == null) {
            Log.d("JukeDJDeb", "No music :(");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            playMusic();
            return;
        }
        Log.d("JukeDJDeb", "Playing new song: " + song.getName());
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView songPlaying = (TextView) main.findViewById(R.id.tb_SongPlaying);
                songPlaying.setText(Html.fromHtml("Now playing:<br><font color=#868383>"+ Html.escapeHtml(song.getName()) + "</font>"));
            }
        });
//        JSONObject songInfo = APIInterface.getSongInfo(main.songQueue.get(0).toString(), "437d961ac979c05ea6bae1d5cb3993ec");
//        try {
//            String title = songInfo.getString("title");
//            main.setPlayingTitle(title);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        main.stream(song, new Callback<MediaPlayer>() {
            @Override
            public void call(MediaPlayer obj) {
                playMusic();
            }
        });
    }
}
