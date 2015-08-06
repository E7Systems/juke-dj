package com.e7systems.jukedj_hub.ads;

import android.util.Log;

import com.e7systems.jukedj_hub.MainActivity;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;

/**
 * Created by Dylan Katz on 8/5/2015.
 */
public class AdResponseListener implements AdErrorEvent.AdErrorListener, AdsLoader.AdsLoadedListener, AdEvent.AdEventListener {
    private MainActivity main;
    private AdsManager adsManager;

    public AdResponseListener(MainActivity main) {
        this.main = main;
    }

    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        Log.e("jukedj-ads", adErrorEvent.getError().toString());
    }

    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
        adsManager = adsManagerLoadedEvent.getAdsManager();
        adsManager.addAdErrorListener(this);
        adsManager.addAdEventListener(this);
        adsManager.init();
    }

    @Override
    public void onAdEvent(AdEvent adEvent) {
        switch(adEvent.getType()) {
            case LOADED:
                main.onAdLoaded(adEvent);
                break;
            case CONTENT_PAUSE_REQUESTED:
                main.onContentPauseRequested(adEvent);
                break;
            case CONTENT_RESUME_REQUESTED:
                main.onContentResumeRequested(adEvent);
                break;
            case CLICKED:
                main.onAdClicked(adEvent);
                break;
            case AD_BREAK_READY:
                getAdsManager().start();
                break;
            case COMPLETED:
                main.onAdCompleted(adEvent);
                break;
        }
    }

    public void playAd() {
        AdsRequest request = main.getSdkFactory().createAdsRequest();
        request.setAdTagUrl("http://pubads.g.doubleclick.net/gampad/ads?sz=640x360&iu=/6062/iab_vast_samples/skippable&ciu_szs=300x250,728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast3&unviewed_position_start=1&url=[referrer_url]&correlator=[timestamp]]]");
        AdDisplayContainer container = main.getSdkFactory().createAdDisplayContainer();
//        container.setPlayer();
        request.setAdDisplayContainer(container);
    }

    public AdsManager getAdsManager() {
        return adsManager;
    }
}
