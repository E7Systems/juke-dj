package com.e7systems.jukedj_hub.net.packets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Admin on 6/29/2015.
 */
public class PacketSkipVote implements Packet {

    public PacketSkipVote() {}

    @Override
    public int getId() {
        return 1;
    }

    public PacketSkipVote read(BufferedReader in){
        return new PacketSkipVote();
    }

    @Override
    public void write(BufferedWriter out) throws IOException {}
}
