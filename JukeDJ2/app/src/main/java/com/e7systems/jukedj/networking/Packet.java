package com.e7systems.jukedj.networking;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Dylan on 6/11/2015.
 */
public interface Packet {
    public int getId();
    public void stream(BufferedWriter out) throws IOException;

}
