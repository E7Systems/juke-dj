package com.e7systems.jukedj_hub.util;

import com.e7systems.jukedj_hub.Callback;
import com.e7systems.jukedj_hub.entities.Song;
import com.e7systems.jukedj_hub.net.NetHandlerThread;

import java.net.InetAddress;
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
    private static List<InetAddress> skipVotes = new ArrayList<>();
    private static Callback<Boolean> skip;

    public SongQueue(Callback<Boolean> skip) {
        SongQueue.skip = skip;
    }

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
            if(songs[i] == null) {
                continue;
            }
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

    public static boolean addSkipVote(InetAddress address) {
        if(address.equals(getCurrentSong().getOwnerIp())) {
            skip.call(true);
            skipVotes.clear();
            return true;
        }
        if(!skipVotes.contains(address)) {
            skipVotes.add(address);
            return true;
        }
        if(getSkipVotes() > NetHandlerThread.getInstance().getNumClientsConnected() / 2) {
            skip.call(false);
            skipVotes.clear();
            return true;
        }
        return false;
    }

    public static Song getCurrentSong() {
        if(queue.size() > 0) {
            return queue.get(0);
        } else {
            return null;
        }
    }

    public static int getSkipVotes() {
        return skipVotes.size();
    }


}
