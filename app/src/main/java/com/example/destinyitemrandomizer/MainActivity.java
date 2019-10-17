package com.example.destinyitemrandomizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.example.destinyitemrandomizer.destinywrapper.DestinyAsyncTasks.*;
import com.example.destinyitemrandomizer.destinywrapper.DestinyInventoryManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.Date;


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

    public String getMembershipID() {
        return membershipID;
    }

    public void setMembershipID(String membershipID) {
        this.membershipID = membershipID;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }

    // This is the api key
    private String apiKey = "7f2b4c1bfb4c4816a3f57cff6b3f8c53";

    // This is the inventory manager
    private DestinyInventoryManager inventory = null;

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
            //
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

    // Store the oauth response
    public void storeOauthResponse(String response) {
        // Turn this into a json object so it's easier to edit
        JsonObject oauthObj = new JsonParser().parse(response).getAsJsonObject();

        // Get current time
        long curTime = (new Date().getTime())/1000;

        // TODO: Make a method for this
        // Update the expiry time in the jso object
        long expTime = curTime + oauthObj.getAsJsonPrimitive("expires_in").getAsLong();
        oauthObj.remove("expires_in");
        oauthObj.addProperty("expires_in", ("" + expTime));

        // Update the refresh expiry time
        long refExpTime = curTime + oauthObj.getAsJsonPrimitive("refresh_expires_in").getAsLong();
        oauthObj.remove("refresh_expires_in");
        oauthObj.addProperty("refresh_expires_in", ("" + refExpTime));

        // Turn response back into a string
        response = oauthObj.toString();

        // Save to shared prefs
        SharedPreferences prefs = getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("oauth", response);

        editor.commit();
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

                // Create our instance of the inventory manager, passing in the data from bungie.net
                DestinyCreateInventoryManagerAsync managerCreator = new DestinyCreateInventoryManagerAsync(this);
                managerCreator.execute(profileInventory, characters, characterInventories, characterEquipment);

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
                    // Item instances is 300, ItemStats is 304
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

    // Set the inventory manager when it is done being created asynchronously
    public void setInventoryManager(DestinyInventoryManager invMan)
    {
        // Set the manager
        inventory = invMan;

        // Put the app in a usable mode
    }



}


