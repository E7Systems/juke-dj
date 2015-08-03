package com.e7systems.jukedj_hub.net;

import android.util.Log;

import com.e7systems.jukedj_hub.net.packets.Packet;
import com.e7systems.jukedj_hub.net.packets.PacketCheckin;
import com.e7systems.jukedj_hub.net.packets.PacketHeartbeat;
import com.e7systems.jukedj_hub.net.packets.PacketSkipVote;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dylan Katz on 7/27/2015.
 */
public class PacketSerializer {
    private Map<Integer, Class<? extends Packet>> packetTypes = new HashMap<>();
    public PacketSerializer() {
        packetTypes.put(0, PacketCheckin.class);
        packetTypes.put(1, PacketSkipVote.class);
        packetTypes.put(4, PacketHeartbeat.class);
    }

    public Class getPacketType(int id) {
        return packetTypes.get(id);
    }

    /**
     * Read a packet from a given input stream
     * @param inputStream
     * @return null if packet cannot be read, else packet instance.
     * @throws IOException
     */
    public Packet readPacket(BufferedReader inputStream) throws IOException {
        int id = inputStream.read();
        Class packetCs = getPacketType(id);
        if(packetCs == null) {
            throw new IOException("Invalid packet id: " + id);
        }
        try {
            Log.d("JukeDJDeb", packetCs.getName());
            Method readMethod = packetCs.getDeclaredMethod("read", BufferedReader.class);
            if(readMethod == null) {
                return null;
            }
            return (Packet) readMethod.invoke(packetCs.newInstance(), inputStream);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
