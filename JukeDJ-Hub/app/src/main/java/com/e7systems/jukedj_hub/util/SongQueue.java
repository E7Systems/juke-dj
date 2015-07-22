package com.e7systems.jukedj_hub.util;

import com.e7systems.jukedj_hub.entities.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dylan Katz on 7/22/2015.
 */
public class SongQueue {
    private static List<Song> queue = new ArrayList<>();
    public static void push(Song song) {
        for(Song s : queue) {
            if(s.getId() == song.getId()) {
                return;
            }
        }
        queue.add(song);
    }

    public static Song pop() {
        if(queue.size() <= 0) {
            return null;
        }
        return queue.remove(0);
    }

    public static void queueSongs(Song... songs) {
        for(int i = queue.size(); i > 0 && queue.size() - i < songs.length; i--) {
            if(i % 2 == 0) {
                queue.add(i, songs[i]);
            }
        }
    }

}
