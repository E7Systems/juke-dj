package com.e7systems.jukedj.networking;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Dylan on 6/15/2015.
 */
public class PacketCheckin implements Packet {
    private String fbMusicPrefs;

    public PacketCheckin(String fbMusicPrefs) {
        this.fbMusicPrefs = fbMusicPrefs;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public void stream(BufferedWriter out) throws IOException {
        out.write(0);
        out.write(fbMusicPrefs);
        out.flush();
    }
}
