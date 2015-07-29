package com.e7systems.jukedj_hub.net;

import android.util.Log;

import com.e7systems.jukedj_hub.MainActivity;
import com.e7systems.jukedj_hub.entities.Song;
import com.e7systems.jukedj_hub.entities.User;
import com.e7systems.jukedj_hub.net.packets.Packet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.net.ServerSocketFactory;

/**
 * Created by Admin on 6/25/2015.
 */
public class NetHandlerThread extends Thread {
    private ServerSocket serverSocket;
    private Map<User, Socket> clientsConnected = new HashMap<>();
    private static NetHandlerThread instance;

    public NetHandlerThread() {
        instance = this;
    }

    public static NetHandlerThread getInstance() {
        return instance;
    }

    public void writePacket(Packet packet, InetAddress ip) throws IOException {
        Socket socket = clientsConnected.get(getUserByIp(ip));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        packet.write(writer);
//        writer.close();
    }

    public User getUserByIp(InetAddress address) {
        for(User user : clientsConnected.keySet()) {
            if(user.getIp().equals(address)) {
                return user;
            }
        }
        return null;
    }

    public void broadcastPacket(Packet packet) throws IOException {
        for(User user : clientsConnected.keySet()) {
            writePacket(packet, user.getIp());
        }
    }

    public int getNumClientsConnected() {
        return clientsConnected.size();
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
//                Log.d("JukeDJDeb", "Listening for socket.");
                Socket socket = serverSocket.accept();
                clientsConnected.put(new User(socket.getInetAddress(), new ArrayList<Song>(), ""), socket);
//                Log.d("JukeDJDeb", "Received socket.");
                new Thread(new ClientInterfaceRunnable(socket)).start();
//                in.close();
//                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public Set<User> getUsers() {
        return clientsConnected.keySet();
    }
}
