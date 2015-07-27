package com.e7systems.jukedj_hub.net;

import android.util.Log;

import com.e7systems.jukedj_hub.MainActivity;
import com.e7systems.jukedj_hub.entities.Song;
import com.e7systems.jukedj_hub.net.packets.Packet;
import com.e7systems.jukedj_hub.net.packets.PacketCheckin;
import com.e7systems.jukedj_hub.net.packets.PacketMakeNotify;
import com.e7systems.jukedj_hub.util.APIDataHandler;
import com.e7systems.jukedj_hub.util.SongQueue;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ServerSocketFactory;

/**
 * Created by Admin on 6/25/2015.
 */
public class NetHandlerThread extends Thread {
    private ServerSocket serverSocket;
    private Map<InetAddress, Socket> clientsConnected = new HashMap<>();
    private static NetHandlerThread instance;

    public NetHandlerThread() {
        instance = this;
    }

    public static NetHandlerThread getInstance() {
        return instance;
    }

    public void writePacket(Packet packet, InetAddress ip) throws IOException {
        Socket socket = clientsConnected.get(ip);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        packet.write(writer);
//        writer.close();
    }

    @Override
    public void run() {
        try {
            serverSocket = ServerSocketFactory.getDefault().createServerSocket(MainActivity.PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            try {
                Log.d("JukeDJDeb", "Listening for socket.");
                Socket socket = serverSocket.accept();
                clientsConnected.put(socket.getInetAddress(), socket);
                Log.d("JukeDJDeb", "Received socket.");
                new Thread(new ClientInterfaceRunnable(socket)).start();
//                in.close();
//                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
