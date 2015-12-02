package com.e7systems.jukedj.networking;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;

import com.e7systems.jukedj.MainActivity;
import com.e7systems.jukedj.networking.packet.Packet;
import com.e7systems.jukedj.networking.packet.PacketCheckin;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;


/**
 * Created by Admin on 6/24/2015.
 */
public class DiscoveryManager extends AsyncTask<MainActivity, Void, Void> {
    private static String SERVICE_NAME = "jdjmp";
    private MainActivity main;
    private WifiManager.MulticastLock multicastLock;
    private JmDNS jmdns;
    public Socket socket;
    private static DiscoveryManager instance;

    public DiscoveryManager(MainActivity main) {
        this.main = main;
        execute(main);
        instance = this;

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
        addServiceListener();

        return null;
    }

    public void addServiceListener() {

        jmdns.addServiceListener("_jdjmp._tcp.local.", new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent event) {
                Log.d("JukeDJDeb", "Found service: " + event.getInfo());
                jmdns.requestServiceInfo(event.getType(), event.getName(), 500);
            }

            @Override
            public void serviceRemoved(ServiceEvent event) {
                main.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        main.progressDialog.dismiss();
                    }
                });
                main.searchThread.start();
            }

            @Override
            public void serviceResolved(ServiceEvent event) {
                InetAddress inetAddr = null;
                try {
                    inetAddr = event.getInfo().getInet4Addresses().length == 0 ? InetAddress.getByName(event.getInfo().getDomain()) : event.getInfo().getInet4Addresses()[0];
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                if (inetAddr == null) {
                    Log.e("JukeDJ", "Null ip!");
                } else {
                    Log.d("JukeDJDeb", "Found service at " + inetAddr.getHostAddress());
                    try {
                        main.searchThread.interrupt();
                        socket = new Socket(inetAddr, event.getInfo().getPort());
                        sendPacket(new PacketCheckin(main.fbPrefs, main.fbUsername), socket);
                        new Thread(new ClientInterfaceThread(main, socket)).start();
                        main.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                main.progressDialog.dismiss();
                            }
                        });
//                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

   /* public void refresh() {
        jmdns.unregisterAllServices();
        addServiceListener();
    }
*/
    public void sendPacket(Packet packet, Socket socket) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        packet.write(writer);
//        writer.close();
    }

    public static DiscoveryManager getInstance() {
        return instance;
    }
}
