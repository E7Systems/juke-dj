package com.e7systems.jukedj_hub.net;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;

import com.e7systems.jukedj_hub.MainActivity;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Created by Dylan on 6/24/2015.
 * This class advertises the service information over LAN so clients can locate the hub.
 */
public class MDNSBroadcaster extends AsyncTask<MainActivity, Void, Void> {
    private static final String SERVICE_NAME = "jdjmp";
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
            InetAddress address = InetAddress.getByName(Formatter.formatIpAddress(((WifiManager) main.getSystemService(main.WIFI_SERVICE)).getConnectionInfo().getIpAddress()));
            jmdns = JmDNS.create(address,
                    "JDJMP");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        ServiceInfo info = ServiceInfo.create("_jdjmp._tcp.local.", SERVICE_NAME, MainActivity.PORT, "");
        try {
            jmdns.registerService(info);
        } catch (IOException e) {
            Log.e("MDNS", "ERROR: " + e.getMessage());
        }
        return null;
    }

    public void acquireMulticastLock() {
        multicastLock = ((WifiManager)main.getSystemService(main.WIFI_SERVICE)).createMulticastLock("MultiCastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();
    }

}
