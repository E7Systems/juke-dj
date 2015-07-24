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

    /**
     * Add a song to the beginning of the queue
     * @param song Song to add
     */
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

    /**
     * Return and remove the next song in the queue.
     * @return Removed song
     */
    public static Song pop() {
        if(queue.size() <= 0) {
            return null;
        }
        return queue.remove(0);
    }

    /**
     * Distribute songs fairly amongst the queue.
     * @param songs
     */
    public static void queueSongs(Song... songs) {
        for (int i = 0; i < songs.length; i++) {
            //If we've exceeded existing queue values, but still have songs,
            //we begin inserting songs from index 0.
            if(i > queue.size()) {
                set(i % queue.size(), songs[i]);
            } else {
                //Otherwise, we simply insert a song at the given index.
                set(i, songs[i]);
            }
        }
    }

    /**
     * Order songs by popularity. This is useful for avoiding remixes
     * and retrieving the real song, or at least a remix that many people
     * like.
     * @param songs A set of songs to sort
     * @return The sorted songs
     */
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
