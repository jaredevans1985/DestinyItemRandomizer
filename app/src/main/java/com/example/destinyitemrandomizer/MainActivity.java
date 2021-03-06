package com.example.destinyitemrandomizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.example.destinyitemrandomizer.destinywrapper.DestinyAsyncTasks.*;
import com.example.destinyitemrandomizer.destinywrapper.DestinyCharacterInfo;
import com.example.destinyitemrandomizer.destinywrapper.DestinyInventoryManager;
import com.example.destinyitemrandomizer.destinywrapper.DestinyItemInfo;
import com.example.destinyitemrandomizer.destinywrapper.WeaponType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

        boolean mustRefresh = false;
        intent.getBooleanExtra("NEED_TO_REFRESH", mustRefresh);

        if (uri != null && uri.toString().startsWith("myapp://oauthresponse"))
        {
            // Get the response code
            //
            String code = uri.getQueryParameter("code");

            // Execute the OAuth task to get the token
            DestinyTaskSimpleOAuth newTask = new DestinyTaskSimpleOAuth(this);
            //DestinyTaskOAuth newTask = new DestinyTaskOAuth(this);
            newTask.execute(code);
        }
        // Do we need to refresh?
        else if (mustRefresh) {

            // Get the refresh token
            SharedPreferences prefs = getSharedPreferences("MyPref", 0);
            String oauthInfo = prefs.getString("oauth", null);
            JsonObject infoAsObject = new JsonParser().parse(oauthInfo).getAsJsonObject();
            String refreshToken = infoAsObject.getAsJsonPrimitive("refresh_token").getAsString();

            // Execute the refresh OAuth task to refresh the token
            DestinyTaskOAuthRefresh newTask = new DestinyTaskOAuthRefresh(this);
            newTask.execute(refreshToken);
        }
        else
        {
            // If we get here...it probably means we have a valid token? So set it?
            if(token.equalsIgnoreCase("NO_TOKEN")) {

                // Check for token in shared prefs if it's not here
                // By now we should have a valid token in there
                // Check to see if we have token info stored already
                SharedPreferences prefs = getSharedPreferences("MyPref", 0);
                String oauthInfo = prefs.getString("oauth", null);

                if (oauthInfo != null) {
                    JsonObject infoAsObject = new JsonParser().parse(oauthInfo).getAsJsonObject();
                    String accessToken = infoAsObject.getAsJsonPrimitive("access_token").getAsString();

                    setToken(accessToken);

                    // Get membership info
                    // See if we can get the current user with our valid token
                    DestinyTaskGet memberInfo = new DestinyTaskGet(this);
                    memberInfo.execute("https://www.bungie.net/Platform/User/GetMembershipsForCurrentUser/", token);
                }

            }
        }

    }

    // Store the oauth response
    public void storeOauthResponse(String response) {
        // Turn this into a json object so it's easier to edit
        JsonObject oauthObj = new JsonParser().parse(response).getAsJsonObject();

        // Get current time
        long curTime = (new Date().getTime())/1000;

        // TODO: Make a method for this
        // Update the expiry time in the json object
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
        // Create a new random loadout
        inventory.getRandomLoadout();

        updateResultScreen();

    }


    // Equip button callback
    public void onClickEquipItems(View v)
    {
        // Try to equip all items
        // Need some good error reporting
        inventory.equipCurrentRoll();
    }

    // This does roll, but importantly it also creates the item database
    public void onClickRandomRoll(View v)
    {
        // Find all new items

        // Go to results screen
        //setContentView(R.layout.activity_result);

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

        // Switch to loadout view
        switchToResult();

    }

    // Switch to the result screen and populate it with info
    private void switchToResult() {
        setContentView(R.layout.activity_result);

        updateResultScreen();

    }

    private void updateResultScreen() {
        // Get the kinetic view
        View kinView = findViewById(R.id.kineticView);

        // Get the kinetic weapon
        DestinyItemInfo kinWeapon = inventory.getCurrentWeapon(WeaponType.KINETIC);

        // Set the kinetic values
        setWeaponInfoFromInventory(kinView, kinWeapon);


        // Get the energy view
        View enView = findViewById(R.id.energyView);

        // Get the energy weapon
        DestinyItemInfo enWeapon = inventory.getCurrentWeapon(WeaponType.ENERGY);

        // Set the energy values
        setWeaponInfoFromInventory(enView, enWeapon);


        // Get the power view
        View powView = findViewById(R.id.powerView);

        // Get the power weapon
        DestinyItemInfo powWeapon = inventory.getCurrentWeapon(WeaponType.POWER);

        // Set the power values
        setWeaponInfoFromInventory(powView, powWeapon);

        // Update character buttons
        Button char1 = findViewById(R.id.btnChar1);
        Button char2 = findViewById(R.id.btnChar2);
        Button char3 = findViewById(R.id.btnChar3);

        char1.setText(inventory.characters.get(0).charDescription);
        char2.setText(inventory.characters.get(1).charDescription);
        char3.setText(inventory.characters.get(2).charDescription);

    }

    private void setWeaponInfoFromInventory(View infoPane, DestinyItemInfo weaponInfo) {
        TextView name = infoPane.findViewById(R.id.weaponTitle);
        name.setText(weaponInfo.itemName);

        TextView weapType = infoPane.findViewById(R.id.weaponType);
        weapType.setText(weaponInfo.itemType);

        TextView element = infoPane.findViewById(R.id.weaponElement);
        element.setText(weaponInfo.itemElement);

        TextView power = infoPane.findViewById(R.id.weaponPower);
        setItemPower(power, weaponInfo);

        ImageView icon = infoPane.findViewById(R.id.weaponImage);
        Picasso.get().load("http://www.bungie.net" + weaponInfo.itemImgUrl).into(icon);
    }

    public void setItemPower(TextView pane, DestinyItemInfo weapon) {

        if(weapon.itemPower.equals("ITEM POWER NOT SET")) {
            weapon.requestItemPower(this, pane);
        }
        else {
            pane.setText(weapon.itemPower);
        }
    }

    // Char select buttons
    public void onClickCharOne(View v) {
        setCharacter(0);
    }

    public void onClickCharTwo(View v) {
        setCharacter(1);
    }

    public void onClickCharThree(View v) {
        setCharacter(2);
    }

    private void setCharacter(int index) {
        changeButtonColors(index);

        inventory.setSelectedCharacter(index);
    }

    private void changeButtonColors(int index) {
        Button char1 = findViewById(R.id.btnChar1);
        Button char2 = findViewById(R.id.btnChar2);
        Button char3 = findViewById(R.id.btnChar3);

        int highlight = Color.argb(1, 255, 155, 155);
        int reset = Color.argb(1, 200, 200, 200);


        switch(index) {
            case 0:
                char1.setBackgroundColor(highlight);
                char2.setBackgroundColor(reset);
                char3.setBackgroundColor(reset);
                break;
            case 1:
                char2.setBackgroundColor(highlight);
                char1.setBackgroundColor(reset);
                char3.setBackgroundColor(reset);
                break;
            case 2:
                char3.setBackgroundColor(highlight);
                char1.setBackgroundColor(reset);
                char2.setBackgroundColor(reset);
                break;
        }
    }
}


