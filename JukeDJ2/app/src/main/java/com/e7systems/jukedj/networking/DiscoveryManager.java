package com.e7systems.jukedj.networking;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;

import com.e7systems.jukedj.MainActivity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;


/**
 * Created by Admin on 6/24/2015.
 */
public class DiscoveryManager extends AsyncTask<MainActivity, Void, Void> {
    private static String SERVICE_NAME = "JDJMP";
    private MainActivity main;
    private WifiManager.MulticastLock multicastLock;
    private JmDNS jmdns;
    public DiscoveryManager(MainActivity main) {
        this.main = main;
        execute(main);

    }

    public void acquireMulticastLock() {
        multicastLock = ((WifiManager)main.getSystemService(main.WIFI_SERVICE)).createMulticastLock("MultiCastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();
    }

    @Override
    protected Void doInBackground(MainActivity... params) {
        acquireMulticastLock();
        try {
            jmdns = JmDNS.create(Formatter.formatIpAddress(((WifiManager) main.getSystemService(main.WIFI_SERVICE)).getConnectionInfo().getIpAddress()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        jmdns.addServiceListener("_jdjmp._tcp.local.", new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent event) {
                Log.d("JukeDJDeb", "Found service: " + event.getInfo());
                InetAddress inetAddr = event.getInfo().getInet4Addresses().length == 0 ? null : event.getInfo().getInet4Addresses()[0];
                if (inetAddr == null) {
                    Log.d("JukeDJDeb", "Null ip!");
                } else {
                    Log.d("JukeDJDeb", "Found service at " + inetAddr.getHostAddress());
                    Socket socket = null;
                    try {
                        socket = new Socket(inetAddr, event.getInfo().getPort());
                        sendPacket(new PacketCheckin(main.fbPrefs), socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void serviceRemoved(ServiceEvent event) {
                Log.d("JukeDJDeb", "Lost service: " + event.getInfo());
            }

            @Override
            public void serviceResolved(ServiceEvent event) {
                jmdns.requestServiceInfo(event.getType(), event.getName(), 500);
            }
        });
        return null;
    }

    public void sendPacket(Packet packet, Socket socket) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        packet.stream(writer);
        writer.close();
    }
}
