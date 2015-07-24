package com.e7systems.jukedj_hub;

import android.util.Log;

import com.e7systems.jukedj_hub.entities.Song;
import com.e7systems.jukedj_hub.util.APIDataHandler;
import com.e7systems.jukedj_hub.util.SongQueue;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;

import javax.net.ServerSocketFactory;

/**
 * Created by Admin on 6/25/2015.
 */
public class NetHandlerThread extends Thread {
    private ServerSocket serverSocket;

    @Override
    public void run() {
        try {
            serverSocket = ServerSocketFactory.getDefault().createServerSocket(MainActivity.PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            try {
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final int id = in.read(); //Unused
                String result = "";
                String val;
                while((val = in.readLine()) != null) {
                    result += val;
                }
//                int maxSongs = random.nextInt(SONGS_PER_USER / 2);
//                int startIdx = 0;
                List<Song> songsToAdd = APIDataHandler.fetchSongs(APIDataHandler.getArtists(result));

                Song[] sortedSongs = SongQueue.orderSongsByWeight(songsToAdd.toArray(new Song[0]));
                SongQueue.queueSongs(sortedSongs);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
