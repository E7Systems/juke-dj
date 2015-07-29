package com.e7systems.jukedj.networking;

import android.util.Log;
import android.widget.Toast;

import com.e7systems.jukedj.MainActivity;
import com.e7systems.jukedj.networking.packet.PacketCheckin;
import com.e7systems.jukedj.networking.packet.PacketMakeNotify;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.Buffer;

/**
 * Created by Dylan Katz on 7/27/2015.
 */
public class ClientInterfaceThread implements Runnable {

    private MainActivity activity;
    private Socket client;
    private BufferedReader in;
    private BufferedWriter out;

    public ClientInterfaceThread(MainActivity activity, Socket client) {
        this.activity = activity;
        this.client = client;
        try {
            this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(!client.isClosed()) {
            try {
                while(!in.ready());
                int id = in.read();
                switch(id) {
                    case 2:
                        final PacketMakeNotify notify = new PacketMakeNotify();
                        notify.read(in);
                        if(notify.isToast()) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity.getApplicationContext(), notify.getText(), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            activity.notification(notify.getTitle(), notify.getText());
                        }
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
