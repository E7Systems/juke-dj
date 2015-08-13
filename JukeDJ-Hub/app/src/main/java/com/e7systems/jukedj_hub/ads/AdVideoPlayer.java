package com.e7systems.jukedj_hub.ads;

import android.net.Uri;
import android.util.Log;
import android.widget.VideoView;

import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dylan Katz on 8/5/2015.
 */
public class AdVideoPlayer implements VideoAdPlayer {
    private final VideoView view;
    private AdPlayState state = AdPlayState.FINISHED;
    private List<VideoAdPlayer.VideoAdPlayerCallback> callbacks = new ArrayList<>();
    public AdVideoPlayer(VideoView view) {
        this.view = view;
    }

    public void play() {
        view.start();
        AdCallbackResponseType responseType = AdCallbackResponseType.COMPLETED;
        switch(state) {
            case PAUSED:
                responseType = AdCallbackResponseType.PAUSED;
                break;
            case FINISHED:
                responseType = AdCallbackResponseType.COMPLETED;
                break;
            case FAILURE:
                responseType = AdCallbackResponseType.FAILURE;
                break;
            default:
                break;
        }
        for(VideoAdPlayer.VideoAdPlayerCallback callback : callbacks) {
            switch (responseType) {
                case PAUSED:
                    callback.onPause();
                    break;
                case COMPLETED:
                    callback.onEnded();
                    break;
                case FAILURE:
                    callback.onError();
            }
        }
    }

    public void pause() {
        view.pause();
        for (VideoAdPlayer.VideoAdPlayerCallback callback : callbacks) {
            callback.onPause();
        }

    }

    @Override
    public void playAd() {
        play();
    }

    @Override
    public void loadAd(String s) {
        view.setVideoURI(Uri.parse(s));
    }

    @Override
    public void stopAd() {
        view.stopPlayback();
    }

    @Override
    public void pauseAd() {
        pause();
    }

    @Override
    public void resumeAd() {
        view.resume();
        for (VideoAdPlayer.VideoAdPlayerCallback callback : callbacks) {
            callback.onResume();
        }
    }

    @Override
    public void addCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
        callbacks.add(videoAdPlayerCallback);
    }

    @Override
    public void removeCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
        callbacks.remove(videoAdPlayerCallback);
    }

    @Override
    public VideoProgressUpdate getAdProgress() {
        return new VideoProgressUpdate(view.getCurrentPosition(), view.getDuration());
    }
}
