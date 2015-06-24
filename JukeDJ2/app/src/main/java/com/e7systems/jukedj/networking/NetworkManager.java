package com.e7systems.jukedj.networking;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dylan on 6/11/2015.
 */
public class NetworkManager {
    private static String SERVICE_NAME = "JDJMP";
    private static NetworkManager instance;
    private NsdManager nsdMngr;
    private List<NsdServiceInfo> services = new ArrayList<>();
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.ResolveListener resolveListener;

    public NetworkManager(Context ctx, int port, final String fbPrefs) {

        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(port);
        NsdManager.RegistrationListener listener = new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                SERVICE_NAME = NsdServiceInfo.getServiceName();
                Log.d("JukDJDeb", "Registered with SID: " + SERVICE_NAME);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d("JukeDJDeb", "Failed to register: " + errorCode);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {}
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {}
        };
        initResListener();
        nsdMngr = (NsdManager) ctx.getSystemService(Context.NSD_SERVICE);

        //nsdMngr.registerService(
         //       serviceInfo, NsdManager.PROTOCOL_DNS_SD, listener);
        instance = this;
        runDeviceDiscovery();
        new Thread(new Runnable() {
            public void run() {
                for (NsdServiceInfo info : services) {
                    try {
                        Socket socket = new Socket(info.getHost(), info.getPort());
                        sendPacket(new PacketCheckin(fbPrefs), socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).run();
    }

    public void sendPacket(Packet packet, Socket socket) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        packet.stream(writer);
        writer.close();
    }

    public static NetworkManager getInstance() {
        return instance;
    }

    public void runDeviceDiscovery() {
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("JukeDJ", "Failed to start discovery: " + errorCode);
                nsdMngr.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("JukeDJ", "Failed to stop discovery: " + errorCode);
                nsdMngr.stopServiceDiscovery(this);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d("JukeDJ", "Discovery started");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.e("JukeDJ", "DISCOVERY STOPPED");
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d("JukeDJDeb", "Service found!");
                if(serviceInfo.getServiceType().equals(SERVICE_NAME)) {
                    nsdMngr.resolveService(serviceInfo, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.d("JukeDJDeb", "Service lost!");
            }
        };
        nsdMngr.discoverServices(SERVICE_NAME, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void initResListener() {
        resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e("JukeDJ", "Resolve failed on device " + serviceInfo.getServiceName() + ": " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                services.add(serviceInfo);
                Log.d("JukeDJDeb", "Found device: " + serviceInfo.getHost().getHostAddress());
            }
        };
    }

}
