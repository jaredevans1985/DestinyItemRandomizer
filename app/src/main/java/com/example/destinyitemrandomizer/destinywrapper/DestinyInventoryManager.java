package com.example.destinyitemrandomizer.destinywrapper;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
This class is in charge of managing the user's inventory
The inventory info from Bungie.net is stored here
It's processed and categorized for easy use later
This class provides easy methods for getting random items within provided parameters
This class has an instance of the manifest reader and talks directly to it
*/
public class DestinyInventoryManager {

    // The manifest manager
    DestinyManifestReader manifest;

    // The currently equipped items for each character
    List<DestinyCharacterInfo> characters = new ArrayList<DestinyCharacterInfo>();

    // Sorted item lists
    List<DestinyItemInfo> kineticWeapons = new ArrayList<DestinyItemInfo>();
    List<DestinyItemInfo> energyWeapons = new ArrayList<DestinyItemInfo>();
    List<DestinyItemInfo> powerWeapons = new ArrayList<DestinyItemInfo>();

    // Bucket Hashes
    Map<String, String> buckets;

    // Pass in the manifest file and start the sorting tasks
    public DestinyInventoryManager(AppCompatActivity activity, JsonObject profileInv, JsonObject chars, JsonObject charsInv, JsonObject charsEquip)
    {
        // Create our instance of the manifest reader
        manifest = new DestinyManifestReader(activity);

        // Just a test of the manifest
        //JsonObject test = manifest.findItemInfo("3211806999");

        // TODO: Actually do something with the JsonObjects

        // Step 1 - get bucket ids from manifest
        buckets = manifest.getBucketHashes();

        // Step 2 - Store character info for later use, including full equipped weapon info, id and char description
        Set<String> charKeys = chars.getAsJsonObject("data").keySet();
        for(String charId :charKeys ) {
            characters.add(new DestinyCharacterInfo(chars.getAsJsonObject("data").getAsJsonObject(charId), charsInv.getAsJsonObject("data").getAsJsonObject(charId)));
        }

        Log.d("Noo", "Too");
        // Step 3 - Compare character inventory to bucket ids and store in appropriate lists with full info
        // Step 4 - Create a list of all items in the general bucket (must have instance id)
        // Step 5 - Go through all objects in general bucket, get their info
        // Step 5b - If it's a weapon, get its full info and store it in the appropriate list

        // Temporary list using during sorting
        JsonArray unsortedGeneral = new JsonArray();
    }


}
