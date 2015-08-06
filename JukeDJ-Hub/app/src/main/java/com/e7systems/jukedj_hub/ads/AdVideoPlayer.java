package com.e7systems.jukedj_hub.ads;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.MediaController;
import android.widget.VideoView;

import com.e7systems.jukedj_hub.Callback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dylan Katz on 8/5/2015.
 */
public class AdVideoPlayer extends VideoView {
    private AdPlayState state = AdPlayState.FINISHED;
    private MediaController mediaController;
    private List<Callback<AdCallbackResponseType>> callbacks = new ArrayList<>();
    public AdVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AdVideoPlayer(Context context) {
        super(context);
        init();
    }

    public AdVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        mediaController = new MediaController(getContext());
        mediaController.setAnchorView(this);
        setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                state = AdPlayState.FINISHED;
                mp.reset();
                mp.setDisplay(getHolder());
                for (Callback<AdCallbackResponseType> callback : callbacks) {
                    callback.call(AdCallbackResponseType.COMPLETED);
                }
            }
        });

        setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                state = AdPlayState.FAILURE;
                for (Callback<AdCallbackResponseType> callback : callbacks) {
                    callback.call(AdCallbackResponseType.FAILURE);
                }

                return true;
            }
        });
    }

    public void play() {
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
        for(Callback<AdCallbackResponseType> callback : callbacks) {
            callback.call(responseType);
        }
    }

    public void pause() {

    }

    public void registerCallback(Callback<AdCallbackResponseType> callback) {
        callbacks.add(callback);
    }
}
