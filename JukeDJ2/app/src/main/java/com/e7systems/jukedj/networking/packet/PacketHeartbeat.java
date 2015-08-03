package com.e7systems.jukedj.networking.packet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Dylan Katz on 8/3/2015.
 */
public class PacketHeartbeat implements Packet {

    @Override
    public int getId() {
        return 4;
    }

    public PacketHeartbeat read(BufferedReader in) {
        return new PacketHeartbeat();
    }

    @Override
    public void write(BufferedWriter out) throws IOException {
        out.write(getId());
//        out.write(System.currentTimeMillis() + "");
//        out.newLine();
        out.flush();
    }

}
