package com.e7systems.jukedj.networking;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Admin on 6/29/2015.
 */
public class PacketLike implements Packet {

    private boolean like;

    public PacketLike(boolean like) {
        this.like = like;
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public void stream(BufferedWriter out) throws IOException {
        out.write(getId());
        out.write(like ? 1 : 0);
        out.newLine();
        out.flush();
    }
}
