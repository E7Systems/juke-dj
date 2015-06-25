package com.e7systems.jukedj_hub;
import java.io.IOException;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.webkit.JavascriptInterface;

public class AudioInterface {
    Context ctx;

    AudioInterface(Context ctx) {
        this.ctx = ctx;
    }
    public void playSound(String url) {

        final MediaPlayer mp;

        try {
            AssetFileDescriptor fileDescriptor =
                    ctx.getAssets().openFd(url);
            mp = new MediaPlayer();
            mp.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
            fileDescriptor.close();
            mp.prepare();
            mp.start();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}