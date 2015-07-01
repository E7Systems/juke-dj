package com.e7systems.jukedj;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.e7systems.jukedj.networking.DiscoveryManager;
import com.e7systems.jukedj.networking.NetworkManager;
import com.e7systems.jukedj.networking.PacketLike;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class MainActivity extends Activity {
    private LoginButton loginButton;
    private CallbackManager fbCallback;
    private AccessToken accessToken;
    private MainActivity instance;
    public String fbPrefs = "";
    private boolean loggedIn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ToggleButton likeUnlike = (ToggleButton) findViewById(R.id.tb_Like);
        likeUnlike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!loggedIn) {
                    displayAlert("Login required", "You need to be logged in to do that!");
                    return;
                }
                try {
                    DiscoveryManager.getInstance().sendPacket(new PacketLike(isChecked), DiscoveryManager.getInstance().socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_likes");
        fbCallback = CallbackManager.Factory.create();
        accessToken = AccessToken.getCurrentAccessToken();
        instance = this;
        if(accessToken != null && !accessToken.isExpired()) {
            getUserInterests();
        }
        loginButton.registerCallback(fbCallback, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accessToken = loginResult.getAccessToken();
//                Toast.makeText("Welcome, " + accessToken.)
                getUserInterests();
            }

            @Override
            public void onCancel() {
                Log.d("JukeDJDeb", "Cancelled");
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

    public void getUserInterests() {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                try {
                    Log.d("JukeDJDeb", jsonObject.getJSONObject("music").getJSONArray("data").toString());
                    fbPrefs = jsonObject.getJSONObject("music").getJSONArray("data").toString();
                    new DiscoveryManager(instance);
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

    public void displayAlert(String title, String content) {
        new AlertDialog.Builder(getApplicationContext()).setTitle(title)
                .setMessage(content)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbCallback.onActivityResult(requestCode, resultCode, data);
    }


}
