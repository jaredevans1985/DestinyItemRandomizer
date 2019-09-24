package com.example.destinyitemrandomizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.destinyitemrandomizer.destinywrapper.DestinyAsyncTasks.*;
import com.google.gson.JsonObject;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;



// This activity is launched by the intent from the oauth in LoginActivity
public class MainActivity extends AppCompatActivity {

    // This will eventually hold the OAuth access token
    private String token = "NO_TOKEN";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    // This is the user's membership info
    // TODO: Put a bunch of these in a class
    private String membershipID = "";
    private String membershipType = "";

    // This is the api key
    private String apiKey = "7f2b4c1bfb4c4816a3f57cff6b3f8c53";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roll);

        // We should have info from the intent that launched this part of the app
        // Namely, Oauth
        Intent intent = getIntent();
        Uri uri = intent.getData();

        if (uri != null && uri.toString().startsWith("myapp://oauthresponse"))
        {
            // Get the response code
            String code = uri.getQueryParameter("code");

            // Execute the OAuth task to get the token
            DestinyTaskOAuth newTask = new DestinyTaskOAuth(this);
            newTask.execute(code);

        }
        else
        {
            // Error reporting for us not getting a proper response
        }

    }

    // This is probably a bad idea, but this method will handle parsing any json response
    public void parseAPIResponse(JsonObject response)
    {
        if(response != null) {
            // If we're getting user member info on initial connection
            if (response.has("destinyMemberships")) {
                // ID and Type of membership retrieved
                JsonObject memberInfo = response.getAsJsonArray("destinyMemberships").get(0).getAsJsonObject();

                this.membershipType = memberInfo.getAsJsonPrimitive("membershipType").toString();
                this.membershipID = memberInfo.getAsJsonPrimitive("membershipId").toString().replace("\"", "");

                Log.d("RESPONSE_MEMBER_INFO", "ID: " + this.membershipID + ", TYPE: " + this.membershipType);

                // TODO: Get user profile here, not in random roll
            }
            // Get all character inventories through the profile
            else if (response.has("profileInventory")) {
                JsonObject profileInventory = response.getAsJsonObject("profileInventory");
                JsonObject characters = response.getAsJsonObject("characters");
                JsonObject characterInventories = response.getAsJsonObject("characterInventories");
                JsonObject characterEquipment = response.getAsJsonObject("characterEquipment");

                Log.d("RESPONSE_PROFILE", "Got the user profile: " + profileInventory.toString());
            }
            else {
                Log.d("RESPONSE_NONE", "Response does not parse to a known response type");
            }

        }
        else
        {
            Log.d("PARSE_ERROR", "Response from get postExecute passed as null");
        }
    }

    // Reroll button callback, go back to roll screen
    public void onClickToRollScreen(View v)
    {
        setContentView(R.layout.activity_roll);
    }

    // Reroll in individual item
    public void onClickSingleReroll(View v)
    {
        // Do some rerolling here
    }

    // Equip button callback
    public void onClickEquipItems(View v)
    {
        // Try to equip all items
        // Need some good error reporting
    }

    // Random roll
    public void onClickRandomRoll(View v)
    {
        // Find all new items

        // Go to results screen
        //setContentView(R.layout.activity_result);

        // TODO: Finalize this code
        // Do a test request for now
        if(isTokenValid())
        {
            // This gets inventories and character info
            String endpoint = "https://www.bungie.net/Platform/Destiny2/" + this.membershipType + "/Profile/" + this.membershipID
                    + "/?components=102"    // Profile inventories (vault)
                    + "&components=200"    // Characters
                    + "&components=201"    // Character inventories (unequipped)
                    + "&components=205";   // Character equipment (equipped)

            DestinyTaskGet getInventory = new DestinyTaskGet(this);
            getInventory.execute(endpoint, token);
        }
        else
        {
            Log.d("NO_VALID_TOKEN", "Cannot process this action, no valid token yet");
        }

    }

    // Test if the token is valid
    private boolean isTokenValid()
    {
        return !token.equalsIgnoreCase("NO_TOKEN");
    }


}


