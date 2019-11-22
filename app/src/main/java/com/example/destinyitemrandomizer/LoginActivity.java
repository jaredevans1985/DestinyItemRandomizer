package com.example.destinyitemrandomizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.destinyitemrandomizer.destinywrapper.DestinyAsyncTasks.DestinyGetManifestURL;
import com.example.destinyitemrandomizer.destinywrapper.DestinyManifestReader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.common.exception.OAuthSystemException;


import java.io.File;
import java.util.Date;

// This activity handles
public class LoginActivity extends AppCompatActivity {

    // Broadcast receiver for download completion
    private BroadcastReceiver DownloadReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent) {

            // Display message from DownloadService
            Bundle b = intent.getExtras();
            String manifest = b.getString(DownloadManifest.EXTRA_MESSAGE);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // This gets the manifest path
        DestinyGetManifestURL getManifestPath = new DestinyGetManifestURL(this);
        getManifestPath.execute();
    }

    // Get the manifest data to look up item info
    public void onManifestURLFound(String manifestUrl)
    {
        // Get filename
        String foundFilename = manifestUrl.substring(manifestUrl.lastIndexOf("/") + 1);

        // Get the currently stored filename
        SharedPreferences prefs = getSharedPreferences("MyPref", 0);
        String storedFilename = prefs.getString("manifest", null);

        // If we have a file that doesn't match, download the new one
        if(!foundFilename.equals(storedFilename))
        {
            // Clear any old json by recovering the stored file and deleting it
            File file = null;
            if(storedFilename != null) {
                file = getBaseContext().getFileStreamPath(storedFilename);
            }

            // Actually delete the old file
            if(file != null && file.exists()) {
                boolean success = file.delete();
            }

            // Use IntentService to download manifest
            Intent newIntent=new Intent(this, DownloadManifest.class);
            newIntent.setAction(DownloadManifest.ACTION_DOWNLOAD);
            newIntent.putExtra(DownloadManifest.EXTRA_URL, "https://www.bungie.net" + manifestUrl);
            // Start Download Service


            this.startService(newIntent);
        }
        else {
            makeAuthenticateClickable();
        }

        // Set the static variable in the reader class
        DestinyManifestReader.manifestFileName = foundFilename;

    }

    // Make authenticate button visible
    public void makeAuthenticateClickable() {
        // Once the file is loaded, make the authenticate button clickable
        Button button = findViewById(R.id.authButton);
        button.setClickable(true);
        button.setVisibility(View.VISIBLE);
    }

    // Here for use with download intent
    protected void onResume(){
        super.onResume();
        // Register receiver to get message from DownloadService
        registerReceiver(DownloadReceiver, new IntentFilter(DownloadManifest.ACTION_DOWNLOAD));

    }

    // Here for use with download intent
    protected void onPause(){
        super.onPause();
        // Unregister the receiver
        unregisterReceiver(DownloadReceiver);

    }

    // Login button callback
    public void onLoginClick(View v) {


        // Call into the API wrapper for login
        // On success, move ahead
        // On failure, give some error messaging

        // Check to see if we have token info stored already
        SharedPreferences prefs = getSharedPreferences("MyPref", 0);
        String oauthInfo = prefs.getString("oauth", null);

        if (oauthInfo != null) {
            JsonObject infoAsObject = new JsonParser().parse(oauthInfo).getAsJsonObject();
            Log.d("PREF_OAUTH_TEST", "Oauth info stored, exp time of token is: " + infoAsObject.getAsJsonPrimitive("expires_in").toString());

            // Check expiry date, if past refresh
            long curTime = (new Date().getTime())/1000;
            long expiryTime = infoAsObject.getAsJsonPrimitive("expires_in").getAsLong();

            // If our token is entirely expired, then we need to start the process again
            if(curTime >= expiryTime)
            {
                startAuthentication();
            }
            else
            {
                // Other, our token is at least valid, though we may need to refresh
                Intent startMain = new Intent(this, MainActivity.class);

                long refreshExpiryTime = infoAsObject.getAsJsonPrimitive("refresh_expires_in").getAsLong();
                if(curTime >= refreshExpiryTime) {
                    // Add an extra value telling us we need to refresh
                    startMain.putExtra("NEED_TO_REFRESH", true);
                }

                // Start our main intent
                startActivity(startMain);

            }
        }
        else {
            startAuthentication();
        }



    }

    // Get a brand new oauth token, or rather, request the code
    public void startAuthentication() {
        OAuthClientRequest request = null;
        try {
            request = OAuthClientRequest
                    .authorizationLocation("https://www.bungie.net/en/OAuth/Authorize")
                    .setClientId("29602")
                    .setRedirectURI("myapp://oauthresponse")
                    .buildQueryMessage();
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }

        // If there, send refresh token

        // Create an intent to get the code and launch the authentication process
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getLocationUri() + "&response_type=code"));
        // Start the intent
        startActivity(intent);
    }




}



