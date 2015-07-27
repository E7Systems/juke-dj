package com.e7systems.jukedj.networking.packet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Admin on 6/29/2015.
 */
public class PacketSkipVote implements Packet {

    @Override
    public int getId() {
        return 1;
    }

    public Packet read(BufferedReader in) throws IOException {
        return null;
    }

    @Override
    public void write(BufferedWriter out) throws IOException {
        out.write(getId());
        out.flush();
    }
}
