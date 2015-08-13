package com.e7systems.jukedj.networking.packet;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Dylan on 6/15/2015.
 */
public class PacketCheckin implements Packet {
    private String fbMusicPrefs;
    private String username;

    public PacketCheckin(String fbMusicPrefs, String username) {
        this.fbMusicPrefs = fbMusicPrefs;
        this.username = username;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public void write(BufferedWriter out) throws IOException {
        out.write(getId());
        out.write(username);
        out.newLine();
        out.write(fbMusicPrefs);
        out.newLine();
        out.flush();
    }

    public String getUsername() {
        return username;
    }
}
