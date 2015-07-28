package com.e7systems.jukedj_hub.entities;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dylan Katz on 7/28/2015.
 */
public class User {
    private List<Song> futureSongs = new ArrayList<>();
    private InetAddress ip;

    public User(InetAddress ip, List<Song> futureSongs) {
        this.ip = ip;
        this.futureSongs = futureSongs;
    }

    public List<Song> getFutureSongs() {
        return futureSongs;
    }

    public void addSong(Song song) {
        futureSongs.add(song);
    }

    public void clipSongs(int start) {
        futureSongs = futureSongs.subList(start, futureSongs.size());
    }

    public InetAddress getIp() {
        return ip;
    }
}
