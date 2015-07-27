package com.e7systems.jukedj.networking.packet;

import java.io.BufferedReader;
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

    public PacketMakeNotify() {}

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public void write(BufferedWriter out) throws IOException {}

    public void read(BufferedReader in) throws IOException {
        this.toast = in.read() == 1;
        this.title = in.readLine();
        this.text = in.readLine();
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public boolean isToast() {
        return toast;
    }
}
