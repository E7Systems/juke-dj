package com.e7systems.jukedj_hub;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.e7systems.jukedj_hub.entities.Song;
import com.e7systems.jukedj_hub.entities.User;
import com.e7systems.jukedj_hub.net.MDNSBroadcaster;
import com.e7systems.jukedj_hub.net.NetHandlerThread;
import com.e7systems.jukedj_hub.util.SongQueue;

import java.io.IOException;


public class MainActivity extends Activity {
    public static final int PORT = 20101;
    public static final int SONGS_PER_USER = 5;
    public static final String CLIENT_ID = "437d961ac979c05ea6bae1d5cb3993ec";
    private NetHandlerThread networkThread;
    MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new MDNSBroadcaster(this);
        Log.d("JukeDJDeb", "Started broadcaster.");
        networkThread = new NetHandlerThread();
        networkThread.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pollNetThread();
                        }
                    });
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        new SongQueueThread(this).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MediaPlayer stream(Song song, final Callback<MediaPlayer> finished) {
        String url = String.format("http://api.soundcloud.com/tracks/%s/stream?client_id=%s", song.getId(), CLIENT_ID);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(url));
            mediaPlayer.prepare();
//            AudioAttributes attr = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).
//                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
//            mediaPlayer.setAudioAttributes(attr);
            mediaPlayer.start();
            //TODO: Remove thread, this is for demonstration purposes only.
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(30000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    finished.call(mediaPlayer);
////                    mediaPlayer.stop();
////                    mediaPlayer.release();
//                }
//            }).start();
            return mediaPlayer;
//            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    finished.call(mp);
////                    mediaPlayer.stop();
////                    mediaPlayer.release();
//                }
//            });
        } catch (IOException e) {
            e.printStackTrace();
        }
//        view.
        return null;
    }

    public void pollNetThread() {
        TextView list = (TextView) findViewById(R.id.tv_Users);
        list.setText("Users online:");
        for(User user : NetHandlerThread.getInstance().getUsers()) {
            if(!user.getUsername().isEmpty()) {
                list.setText(list.getText()
                        + Html.fromHtml("<br><font color=#868383>" + Html.escapeHtml(user.getUsername()) + "</font>").toString());
            }
        }
        TextView votes = (TextView) findViewById(R.id.tv_SkipVotes);
        votes.setText("Skip Votes:" + SongQueue.getSkipVotes() + "/" + (int) Math.ceil((double)NetHandlerThread.getInstance().getUsers().size() / 2d));
    }
}
