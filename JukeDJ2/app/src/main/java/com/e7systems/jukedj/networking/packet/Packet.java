package com.e7systems.jukedj.networking.packet;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Dylan on 6/11/2015.
 */
public interface Packet {
    public int getId();

    public void write(BufferedWriter out) throws IOException;

}
