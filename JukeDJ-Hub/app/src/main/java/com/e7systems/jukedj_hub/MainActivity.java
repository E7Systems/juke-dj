package com.e7systems.jukedj_hub;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import com.e7systems.jukedj_hub.ads.AdResponseListener;
import com.e7systems.jukedj_hub.entities.Song;
import com.e7systems.jukedj_hub.entities.User;
import com.e7systems.jukedj_hub.net.MDNSBroadcaster;
import com.e7systems.jukedj_hub.net.NetHandlerThread;
import com.e7systems.jukedj_hub.util.SongQueue;
import com.e7systems.jukedj_hub.util.payments.IabException;
import com.e7systems.jukedj_hub.util.payments.IabHelper;
import com.e7systems.jukedj_hub.util.payments.IabResult;
import com.e7systems.jukedj_hub.util.payments.Inventory;
import com.e7systems.jukedj_hub.util.payments.Purchase;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;

import java.io.IOException;


public class MainActivity extends Activity {
    public static final int PORT = 20101;
    public static final int SONGS_PER_USER = 5;
    public static final String CLIENT_ID = "437d961ac979c05ea6bae1d5cb3993ec";
    public static final String AD_PURCHASE_ID = "android.test.purchased";
    public static final String PUB_KEY = "";
    private NetHandlerThread networkThread;
    private boolean hasRemovedAds = true;
    MediaPlayer mediaPlayer = new MediaPlayer();
    private AdResponseListener listener;
    private ImaSdkFactory sdkFactory;
    private IabHelper iabHelper;
    private MainActivity mainActivity;
    private AdsLoader adsLoader;
    private boolean adCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        Button buyButton = (Button) findViewById(R.id.btn_Buy);
        iabHelper = new IabHelper(this, PUB_KEY);
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (result.isFailure()) {
                    Log.e("Main", "Failed to initialize iab helper, Error code " + result.getMessage());
                } else {
                    Log.d("Main", "Successfully initialized iab helper.");
                }
            }
        });


        //Networking broadcaster for LAN discovery
        new MDNSBroadcaster(this);

        //Advertisment code
        doAdsStuff(buyButton);
        sdkFactory = ImaSdkFactory.getInstance();
        ImaSdkSettings settings = sdkFactory.createImaSdkSettings();
        settings.setAutoPlayAdBreaks(false);

        adsLoader = sdkFactory.createAdsLoader(this);
        listener = new AdResponseListener(this);
        adsLoader.addAdErrorListener(listener);
        adsLoader.addAdsLoadedListener(listener);
        setAdContentVisible(true);
        //End ad code

        //Networking/processing new clients
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

        //Song cycling/queue
        new SongQueueThread(this).start();
    }

    /**
     * Initialize advertisment setup/button control/etc
     * @param buyButton
     */
    private void doAdsStuff(Button buyButton) {
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                try {
                    Inventory inventory = iabHelper.queryInventory(false, null, null);
                    if(inventory.getPurchase(AD_PURCHASE_ID) != null) {
                        hasRemovedAds = true;
                    }
                } catch (IabException e) {
                    Log.e("Main", e.getMessage());
                }

            }
        });
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iabHelper.launchPurchaseFlow(mainActivity, AD_PURCHASE_ID, 1337, new IabHelper.OnIabPurchaseFinishedListener() {
                    @Override
                    public void onIabPurchaseFinished(IabResult result, Purchase info) {
                        if(result.isFailure()) {
                            if(result.getResponse() == 7) {
//                                Log.d("Main", info.toString());
                                hasRemovedAds = true;
                                setAdContentVisible(false);
                            }
                            Log.e("Main", "Error: " + result.getMessage());
                        } else {
                            hasRemovedAds = true;
                            setAdContentVisible(false);
                        }
                    }
                }, "removeAds");
            }
        });
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
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    finished.call(mediaPlayer);
                }
            });
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

    public void onContentPauseRequested(AdEvent adEvent) {
    }

    public void onAdLoaded(AdEvent event) {
        adCompleted = false;
        listener.getAdsManager().start();
    }

    public void onContentResumeRequested(AdEvent adEvent) {
    }

    public void onAdClicked(AdEvent adEvent) {
    }

    public void onAdCompleted(AdEvent adEvent) {
        this.adCompleted = true;
    }

    public void setAdContentVisible(boolean visible) {
        TextView promptBuyView = (TextView) findViewById(R.id.tv_PromptBuy);
        VideoView adDisplay = (VideoView) findViewById(R.id.vv_Ad);
        Button buyButton = (Button) findViewById(R.id.btn_Buy);
        if(visible) {
            promptBuyView.setVisibility(View.VISIBLE);
            adDisplay.setVisibility(View.VISIBLE);
            buyButton.setVisibility(View.VISIBLE);
            getAdsListener().playAd();
//            getAdsListener().getAdsManager().start();
//            Linkify.addLinks(promptBuyView, Linkify.ALL);
        } else {
            promptBuyView.setVisibility(View.INVISIBLE);
            adDisplay.setVisibility(View.INVISIBLE);
            buyButton.setVisibility(View.INVISIBLE);
            adDisplay.stopPlayback();
        }
    }

    public AdResponseListener getAdsListener() {
        return listener;
    }

    public ImaSdkFactory getSdkFactory() {
        return sdkFactory;
    }

    public AdsLoader getAdsLoader() {
        return adsLoader;
    }

    public boolean hasRemovedAds() {
        return hasRemovedAds;
    }

    public boolean isAdCompleted() {
        return adCompleted;
    }
}
