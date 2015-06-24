package com.e7systems.jukedj_hub;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Created by Admin on 6/24/2015.
 */
public class MDNSBroadcaster extends AsyncTask<MainActivity, Void, Void> {
    private static final String SERVICE_NAME = "JSBNMP";
    private WifiManager.MulticastLock multicastLock;
    private JmDNS jmdns;
    private MainActivity main;

    public MDNSBroadcaster(MainActivity main) {
        this.main = main;
        execute(main);
    }

    protected Void doInBackground(MainActivity... params) {

        acquireMulticastLock();
        try {
            jmdns = JmDNS.create(Formatter.formatIpAddress(((WifiManager) main.getSystemService(main.WIFI_SERVICE)).getConnectionInfo().getIpAddress()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ServiceInfo info = ServiceInfo.create("_jdjmp._tcp.local.", SERVICE_NAME, MainActivity.PORT, "");
        try {
            jmdns.registerService(info);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void acquireMulticastLock() {
        multicastLock = ((WifiManager)main.getSystemService(main.WIFI_SERVICE)).createMulticastLock("MultiCastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();
    }

}
