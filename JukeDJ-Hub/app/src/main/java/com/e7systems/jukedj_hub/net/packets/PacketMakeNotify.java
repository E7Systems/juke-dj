package com.e7systems.jukedj_hub.net.packets;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Dylan Katz on 7/27/2015.
 */
public class PacketMakeNotify implements Packet {
    private boolean toast;
    private String title, text;

    public PacketMakeNotify(String title, String text, boolean toast) {
        this.title = title;
        this.text = text;
        this.toast = toast;
    }

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public void write(BufferedWriter out) throws IOException {
        out.write(getId());
        out.write(toast ? 1 : 0);
        out.write(title);
        out.newLine();
        out.write(text);
        out.newLine();
        out.flush();
    }
}
