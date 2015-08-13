package com.e7systems.jukedj_hub.net;

import android.util.Log;
import android.widget.Toast;

import com.e7systems.jukedj_hub.MainActivity;
import com.e7systems.jukedj_hub.SongQueueThread;
import com.e7systems.jukedj_hub.entities.Song;
import com.e7systems.jukedj_hub.entities.User;
import com.e7systems.jukedj_hub.net.packets.Packet;
import com.e7systems.jukedj_hub.net.packets.PacketCheckin;
import com.e7systems.jukedj_hub.net.packets.PacketHeartbeat;
import com.e7systems.jukedj_hub.net.packets.PacketMakeNotify;
import com.e7systems.jukedj_hub.util.APIDataHandler;
import com.e7systems.jukedj_hub.util.SongQueue;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dylan Katz on 7/27/2015.
 */
public class ClientInterfaceRunnable implements Runnable {

    private Socket client;
    private PacketSerializer packetSerializer;
    private BufferedReader in;
    private BufferedWriter out;
    private long lastPacket = 0;

    public ClientInterfaceRunnable(Socket client) {
        packetSerializer = new PacketSerializer();
        this.client = client;
        try {
            this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO: Break packet parsing away from case statements and into utility class.
    @Override
    public void run() {
        while(!client.isClosed() && System.currentTimeMillis() - lastPacket <= 40000 || lastPacket == 0) {
            try {
//                int id = in.read();
                Packet packet = packetSerializer.readPacket(in);
                lastPacket = System.currentTimeMillis();
                switch(packet.getId()) {
                    case 0:
                        NetHandlerThread.getInstance().writePacket(new PacketHeartbeat(), client.getInetAddress());
//                int maxSongs = random.nextInt(SONGS_PER_USER / 2);
//                int startIdx = 0;
                        List<Song> songsToAdd = new ArrayList<>();
                        try {
                            songsToAdd = APIDataHandler.fetchSongs(APIDataHandler.getArtists(((PacketCheckin) packet).getBlob()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        User user = NetHandlerThread.getInstance().getUserByIp(client.getInetAddress());
                        user.setUsername(((PacketCheckin)packet).getFbUsername());

                        for(Song song : songsToAdd) {
//                            Log.d("JukeDJDeb", song.getName());
                            song.setOwnerIp(client.getInetAddress());
                            user.addSong(song);
                        }

                        Song[] sortedSongs = SongQueue.orderSongsByWeight(songsToAdd.toArray(new Song[0]));
                        SongQueue.queueSongs(sortedSongs);
                        break;
                    case 1:
                        Song song = SongQueue.getCurrentSong();
                        if(song != null && song.getOwnerIp() != null) {
                            if (!SongQueue.addSkipVote(client.getInetAddress())) {
                                NetHandlerThread.getInstance().writePacket(new PacketMakeNotify("", "You've already voted to skip!", true),
                                       song.getOwnerIp());
                            } else {
                                NetHandlerThread.getInstance().writePacket(new PacketMakeNotify("", "Voted to skip the current song.", true),
                                        song.getOwnerIp());
                            }
                        } else {
                            SongQueueThread.getInstance().skip(true);
                        }
                        break;
                    case 4:
                        NetHandlerThread.getInstance().writePacket(new PacketHeartbeat(), client.getInetAddress());
                        break;
                }
            } catch (IOException e) {
                //You ditched us :(
                NetHandlerThread.getInstance().removeUser(NetHandlerThread.getInstance().getUserByIp(client.getInetAddress()));
                try {
                    client.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
//                e.printStackTrace();
            }
        }
    }
}
