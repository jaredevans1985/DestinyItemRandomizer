package com.example.destinyitemrandomizer;

import android.app.PendingIntent;
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
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.destinyitemrandomizer.destinywrapper.DestinyAsyncTasks.DestinyGetManifestURL;
import com.example.destinyitemrandomizer.destinywrapper.DestinyManifestReader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.common.exception.OAuthSystemException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

// This activity handles
public class LoginActivity extends AppCompatActivity {

    // Broadcast receiver for download completion
    private BroadcastReceiver DownloadReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent){

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
        String filename = manifestUrl.substring(manifestUrl.lastIndexOf("/") + 1);

        // Set the static variable in the reader class
        DestinyManifestReader.manifestFileName = filename;

        // Check internal storage for the manifest
        File file = getBaseContext().getFileStreamPath(filename);

        // If the file doesn't exist, download it
        if(!file.exists())
        {
            // TODO: If the file doesn't exist, clear any old json
            //
            // Use IntentService to download manifest
            Intent newIntent=new Intent(this, DownloadManifest.class);
            newIntent.setAction(DownloadManifest.ACTION_DOWNLOAD);
            newIntent.putExtra(DownloadManifest.EXTRA_URL, "https://www.bungie.net" + manifestUrl);
            // Start Download Service

            this.startService(newIntent);
        }

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

            if(curTime >= infoAsObject.getAsJsonPrimitive("expires_in").getAsLong())
            {
                // NEW OAUTH WITH APPAUTH
                // Declare the authorization and token endpoints
                AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                        Uri.parse("https://www.bungie.net/en/OAuth/Authorize") /* auth endpoint*/,
                        Uri.parse("https://www.bungie.net/Platform/App/Oauth/Token/") /* token endpoint */
                );

                // Create the Authorization Request
                String clientID = "29602";
                Uri redirectUri = Uri.parse("myapp://oauthresponse");
                AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                        serviceConfiguration,
                        clientID,
                        ResponseTypeValues.CODE,
                        redirectUri
                );
                builder.setScopes("profile"); // Not sure if this is right
                AuthorizationRequest request = builder.build();

                // Perform the authorization request
                AuthorizationService authorizationService = new AuthorizationService(this); // This maybe should be base context

                Intent authIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, request.hashCode(), authIntent, 0);

                authorizationService.performAuthorizationRequest(request, pendingIntent);

                // END NEW OAUTH

                /*OAuthClientRequest request = null;
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
                startActivity(intent);*/
            }
            else
            {
                // If we don't need this, just move on
                setContentView(R.layout.activity_login);
            }
        }



    }

    // Used for reading manifest json file from internal storage
    private String readFromFile() {

        String ret = "";
        InputStream inputStream = null;
        try {
            inputStream = openFileInput("names.json");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }


}



