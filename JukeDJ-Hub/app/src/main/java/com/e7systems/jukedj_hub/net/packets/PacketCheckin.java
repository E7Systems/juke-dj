package com.e7systems.jukedj_hub.net.packets;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Dylan on 6/15/2015.
 */
public class PacketCheckin implements Packet {
    private String fbMusicPrefs;

    public PacketCheckin() {}

    public PacketCheckin(String fbMusicPrefs) {
        this.fbMusicPrefs = fbMusicPrefs;
    }

    @Override
    public int getId() {
        return 0;
    }

    public static PacketCheckin read(BufferedReader in) throws IOException {
        return new PacketCheckin(in.readLine());
    }

    @Override
    public void write(BufferedWriter out) throws IOException {
        out.write(getId());
        out.write(fbMusicPrefs);
        out.newLine();
        out.flush();
    }

    public String getBlob() {
        return fbMusicPrefs;
    }
}
