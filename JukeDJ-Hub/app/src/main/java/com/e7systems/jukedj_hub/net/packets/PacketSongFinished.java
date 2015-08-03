package com.e7systems.jukedj_hub.net.packets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Dylan Katz on 8/3/2015.
 */
public class PacketSongFinished implements Packet {
    @Override
    public int getId() {
        return 3;
    }

    public PacketSongFinished read(BufferedReader in){
        return new PacketSongFinished();
    }

    @Override
    public void write(BufferedWriter out) throws IOException {
        out.write(getId());
        out.newLine();
        out.flush();
    }
}
