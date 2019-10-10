package com.example.destinyitemrandomizer.destinywrapper;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.destinyitemrandomizer.MainActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

    // Our current activity
    MainActivity activity;

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
    public DestinyInventoryManager(MainActivity activity, JsonObject profileInv, JsonObject chars, JsonObject charsInv, JsonObject charsEquip)
    {
        // Set the activity
        this.activity = activity;

        // Create our instance of the manifest reader
        manifest = DestinyManifestReader.createDestinyManifestReader(activity);

        // Just a test of the manifest
        //JsonObject test = manifest.findItemInfo("3211806999");

        // TODO: Actually do something with the JsonObjects

        // Step 1 - get bucket ids from manifest
        buckets = manifest.getBucketHashes();

        // Step 2 - Store character info for later use, including full equipped weapon info, id and char description
        Set<String> charKeys = chars.getAsJsonObject("data").keySet();
        for(String charId :charKeys ) {
            characters.add(new DestinyCharacterInfo(activity, chars.getAsJsonObject("data").getAsJsonObject(charId), charsEquip.getAsJsonObject("data").getAsJsonObject(charId).getAsJsonArray("items")));
        }

        // Step 3 - Compare character inventory to bucket ids and store in appropriate lists with full info
        for(String charId :charKeys ) {
            // Get character inventory
            JsonArray charInvArray = charsInv.getAsJsonObject("data").getAsJsonObject(charId).getAsJsonArray("items");

            // Iterate over inventory and place in buckets
            for(JsonElement element : charInvArray) {
                JsonObject elAsObj = element.getAsJsonObject();

                // Only try and place it in the bucket if it's in a weapon bucket
                String bucketHash = elAsObj.getAsJsonPrimitive("bucketHash").toString();
                if(buckets.containsKey(bucketHash)) {
                    createItemAndPlaceInBucket(elAsObj);
                }
            }

        }

        // Step 4 - Create a list of all items in the general bucket (must have instance id)
        // Step 5 - Go through all objects in general bucket, get their info
        // Step 5b - If it's a weapon, get its full info and store it in the appropriate list

        // Temporary list using during sorting
        JsonArray unsortedGeneral = new JsonArray();

        Log.d("INVENTORY_COMPLETE", "Inventory object created!");
    }

    // This method takes destiny item info and sorts it into the correct array
    public void createItemAndPlaceInBucket(JsonElement itemInfo) {

        // Make an item info object
        DestinyItemInfo item = new DestinyItemInfo(itemInfo);

        String itemBucket = buckets.get(item.itemBucket);

        if(itemBucket == null) {
            Log.d("SORTING_ERROR", "Null exception trying to get item bucket");
        }

        switch(itemBucket) {
            case "kinetic":
                kineticWeapons.add(item);
                break;
            case "energy":
                energyWeapons.add(item);
                break;
            case "power":
                powerWeapons.add(item);
                break;
            default:
                Log.d("ITEM_SORTING_ERROR", "No bucket found for this item");
                break;

        }
    }

}
