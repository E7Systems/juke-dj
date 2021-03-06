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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.net.ServerSocketFactory;

/**
 * Created by Admin on 6/25/2015.
 */
public class NetHandlerThread implements Runnable {
    private ServerSocket serverSocket;
    private ConcurrentMap<User, Socket> clientsConnected = new ConcurrentHashMap<>();
    private static NetHandlerThread instance;

    public NetHandlerThread() {
        instance = this;
    }

    /**
     * Returns a static instance of NetHandlerThread.
     * @return instance of thread
     */
    public static NetHandlerThread getInstance() {
        return instance;
    }

    /**
     * Send a packet to the given ip, assuming a socket is open.
     * @param packet The packet to send
     * @param ip The ip destination
     */
    public void writePacket(Packet packet, InetAddress ip) {
        User user = getUserByIp(ip);
        if(user == null || !clientsConnected.containsKey(user)) {
            return;
        }
        Socket socket = clientsConnected.get(user);
        if(socket == null || socket.isClosed()) {
            return; //fail silently for unknown users.
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            packet.write(writer);
        } catch (IOException e) {
            clientsConnected.remove(getUserByIp(ip));
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
//        writer.close();
    }

    /**
     * Get an instance of the User object via InetAddress.
     * @param address
     * @return user
     */
    public User getUserByIp(InetAddress address) {
        for(User user : clientsConnected.keySet()) {
            if(user.getIp().equals(address)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Send a packet to all clients
     * @param packet The packet to send
     * @throws IOException
     */
    public void broadcastPacket(Packet packet) throws IOException {
        for(User user : clientsConnected.keySet()) {
            writePacket(packet, user.getIp());
        }
    }

    /**
     * Get the number of clients connected
     * @return clients connected
     */
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
                Log.d("JukeDJDeb", "Listening for socket.");
                Socket socket = serverSocket.accept();
                Log.d("JukeDJDeb", "Received socket.");
                clientsConnected.put(new User(socket.getInetAddress(), new ArrayList<Song>(), ""), socket);
                Log.d("JukeDJDeb", "Received socket.");
                new Thread(new ClientInterfaceRunnable(socket)).start();
//                in.close();
//                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeUser(User user) {
        clientsConnected.remove(user);
    }

    public Set<User> getUsers() {
        return clientsConnected.keySet();
    }
}
