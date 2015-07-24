package com.e7systems.jukedj_hub.util;

import com.e7systems.jukedj_hub.entities.Song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Dylan Katz on 7/22/2015.
 */
public class SongQueue {
    private static List<Song> queue = new ArrayList<>();

    public static void push(Song song) {
        set(0, song);
    }

    public static void set(int idx, Song song) {
        for(Song s : queue) {
            if(s.getId() == song.getId()) {
                return;
            }
        }
        queue.add(idx, song);
    }

    public static Song pop() {
        if(queue.size() <= 0) {
            return null;
        }
        return queue.remove(0);
    }

    public static void queueSongs(Song... songs) {
        for (int i = 0; i < songs.length; i++) {
            if(i > queue.size()) {
                set(i % queue.size(), songs[i]);
            } else {
                set(i, songs[i]);
            }
        }
    }

    public static Song[] orderSongsByWeight(Song... songs) {
        Comparator<Song> comparator = new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                return lhs.getPlaybacks() - rhs.getPlaybacks();
            }
        };
        List<Song> songList = Arrays.asList(songs);
        Collections.sort(songList, comparator);
        return songList.toArray(new Song[0]);
    }

}
