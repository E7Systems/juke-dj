package com.e7systems.jukedj;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.e7systems.jukedj.networking.ClientInterfaceThread;
import com.e7systems.jukedj.networking.DiscoveryManager;
import com.e7systems.jukedj.networking.packet.PacketCheckin;
import com.e7systems.jukedj.networking.packet.PacketSkipVote;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;


public class MainActivity extends Activity {
    private CallbackManager fbCallback;
    private AccessToken accessToken;
    private MainActivity instance;
    public Dialog progressDialog;
    public String fbPrefs = "";
    private boolean loggedIn = false;
    NotificationManager notificationManager;
    public Thread searchThread;
    public String fbUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        final Button skip = (Button) findViewById(R.id.btn_SkipSong);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!loggedIn) {
                    displayAlert("Login required", "You need to be logged in to do that!");
                    return;
                }
                if(DiscoveryManager.getInstance().socket != null && DiscoveryManager.getInstance().socket.isClosed()) {
                    displayAlert("Not connected", "The connection to a hub has not been established.");
                    return;
                }
                try {
                    DiscoveryManager.getInstance().sendPacket(new PacketSkipVote(), DiscoveryManager.getInstance().socket);
                    skip.setEnabled(false);
                } catch (IOException e) {
                    try {
                        DiscoveryManager.getInstance().socket.close();
                    } catch (IOException e1) {
                        e.printStackTrace();
                        e1.printStackTrace();
                    }
                }
            }
        });

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_likes");
        fbCallback = CallbackManager.Factory.create();
        accessToken = AccessToken.getCurrentAccessToken();
        instance = this;
        if(accessToken != null && !accessToken.isExpired()) {
            onLoginSuccess();
        }
        loginButton.registerCallback(fbCallback, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accessToken = loginResult.getAccessToken();
//                Toast.makeText("Welcome, " + accessToken.)
                onLoginSuccess();
            }

            @Override
            public void onCancel() {
                new AlertDialog.Builder(MainActivity.this).setTitle("Login required")
                        .setMessage("You need to log in to use this application!")
                        .setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }

            @Override
            public void onError(FacebookException e) {
                displayAlert("An unexpected error occurred!", "Please report this message:" + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void onLoginSuccess() {
        loggedIn = true;
        fetchUserInterests();
        fetchUsername();
        new DiscoveryManager(instance);
        startSearchProcess();
    }

    public void startSearchProcess() {
        progressDialog = ProgressDialog.show(this, "Searching...", "Attempting to find hub devices on the network...", true);
        final AlertDialog[] alertDialog = new AlertDialog[1];
        //Allow the user to confirm they wish to continue searching for devices. In reality this does nothing, but the illusion
        //of control is a very powerful thing.
        searchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(shouldShowSearchUI()) {
                    if(alertDialog[0] != null && alertDialog[0].isShowing()) {
                        continue;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.hide();
                            alertDialog[0] = displayAlert("Error", "Failed to find a hub device on your network. Press \"Ok\" to try again.", new Callback<Boolean>() {
                                @Override
                                public void call(Boolean obj) {
                                    if(shouldShowSearchUI()) {
                                        progressDialog = ProgressDialog.show(instance, "Searching...", "Attempting to find hub devices on the network...", true);
                                    } else {
                                    }
                                }
                            });
                        }
                    });
                    try {

                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        break;
                    }
                    while(!progressDialog.isShowing() && shouldShowSearchUI());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.hide();
                    }
                });
            }
        });
        searchThread.start();
    }

    private boolean shouldShowSearchUI() {
        return !searchThread.isInterrupted() && (DiscoveryManager.getInstance().socket == null || !DiscoveryManager.getInstance().socket.isConnected());
    }

    public void fetchUserInterests() {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                if(graphResponse.getError() != null) {
                    throw graphResponse.getError().getException();
                }
                try {
                    fbPrefs = jsonObject.getJSONObject("music").getJSONArray("data").toString();
//                    manager = new NetworkManager(getApplicationContext(), 20101, jsonObject.getJSONObject("music").getJSONArray("data").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "music");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void fetchUsername() {
        new GraphRequest(accessToken, "/" + accessToken.getUserId(), null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                try {
                    fbUsername = graphResponse.getJSONObject().getString("first_name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).executeAsync();

    }

    public AlertDialog displayAlert(String title, String content, final Callback<Boolean> callback) {
        AlertDialog alert = new AlertDialog.Builder(this).setTitle(title)
                .setMessage(content)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        callback.call(true);
                    }
                }).create();
        alert.show();
        return alert;
    }

    public void displayAlert(String title, String content) {
        displayAlert(title, content, new Callback<Boolean>() {
            @Override
            public void call(Boolean obj) {
            }
        });
    }

    public void notification(CharSequence title, CharSequence text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(title)
                .setContentText(text)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        notificationManager.notify(0, builder.build());
    }

    public void playPing() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbCallback.onActivityResult(requestCode, resultCode, data);
    }


    public void onSongFinished() {
        final Button skip = (Button) findViewById(R.id.btn_SkipSong);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                skip.setEnabled(true);
            }
        });
    }

    public void onSocketEnd() {
        onSongFinished();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startSearchProcess();
                displayAlert("Disconnected!", "We've lost our connection to the hub, trying to reconnect.");

            }
        });

        Socket old = DiscoveryManager.getInstance().socket;
        try {
            Socket newSocket =  new Socket(old.getInetAddress(), old.getPort());
            DiscoveryManager.getInstance().socket = newSocket;
            DiscoveryManager.getInstance().sendPacket(new PacketCheckin(fbPrefs, fbUsername), newSocket);
            new Thread(new ClientInterfaceThread(this, newSocket)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
