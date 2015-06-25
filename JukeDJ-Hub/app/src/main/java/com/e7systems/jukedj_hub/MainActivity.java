package com.e7systems.jukedj_hub;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    public static final int PORT = 20101;
    private static final String CLIENT_ID = "437d961ac979c05ea6bae1d5cb3993ec";
    private boolean accepting = false;
    private NetHandlerThread networkThread;
    public List<Integer> songQueue = new ArrayList<>();
    MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Switch onOff = (Switch) findViewById(R.id.enabled);
        onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                accepting = isChecked;
            }
        });

        new MDNSBroadcaster(this);
        Log.d("JukeDJDeb", "Started broadcaster.");
        networkThread = new NetHandlerThread(this);
        networkThread.start();
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
    public void stream(String id, final Callback<MediaPlayer> finished) {
        String url = String.format("http://api.soundcloud.com/tracks/%s/stream?client_id=%s", id, CLIENT_ID);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(url));
            mediaPlayer.prepare();
//            AudioAttributes attr = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).
//                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
//            mediaPlayer.setAudioAttributes(attr);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.reset();
                    finished.call(mp);
//                    mediaPlayer.stop();
//                    mediaPlayer.release();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
//        view.
    }

    public boolean isAccepting() {
        return accepting;
    }
}
