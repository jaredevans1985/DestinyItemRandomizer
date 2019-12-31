package com.example.destinyitemrandomizer.destinywrapper;

import android.content.ClipData;
import android.os.Debug;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.destinyitemrandomizer.MainActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
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
    List<DestinyCharacterInfo> characters = new ArrayList<>();

    // Sorted item lists
    List<DestinyItemInfo> kineticWeapons = new ArrayList<>();
    List<DestinyItemInfo> energyWeapons = new ArrayList<>();
    List<DestinyItemInfo> powerWeapons = new ArrayList<>();

    // Bucket Hashes
    Map<String, String> buckets;

    // Pass in the manifest file and start the sorting tasks
    public DestinyInventoryManager(MainActivity activity, JsonObject profileInv, JsonObject chars, JsonObject charsInv, JsonObject charsEquip)
    {
        // Item Map
        // Used before sorting, this item map has keys of item hash values, followed by an instance id
        // If this turns out to belong to a weapon, it will be used to crete a DestinyItemInfo object
        List<ItemLookupInfo> unsortedItems = new ArrayList<>();

        // Set the activity
        this.activity = activity;

        // Create our instance of the manifest reader
        manifest = DestinyManifestReader.createDestinyManifestReader(activity);

        // Step 1 - get bucket ids from manifest
        buckets = manifest.getBucketHashes();

        // Step 2 - Store character info for later use
        // TODO: We are no longer using the equipped item info when setting a character, should probably do that at some point
        Set<String> charKeys = chars.getAsJsonObject("data").keySet();
        for(String charId : charKeys ) {
            characters.add(new DestinyCharacterInfo(chars.getAsJsonObject("data").getAsJsonObject(charId), charsEquip.getAsJsonObject("data").getAsJsonObject(charId).getAsJsonArray("items")));
        }

        // STEP 3 - Populate the unsorted items list by adding key/value pairs of hash val and a default item info
        for(String charId : charKeys ) {
            // Get character inventory
            JsonArray charInvArray = charsInv.getAsJsonObject("data").getAsJsonObject(charId).getAsJsonArray("items");

            // Iterate over inventory and place in unsorted list
            for(JsonElement element : charInvArray) {
                JsonObject elAsObj = element.getAsJsonObject();

                // Only try and place it in the bucket if it's in a weapon bucket
                String itemHash = elAsObj.getAsJsonPrimitive("itemHash").toString();

                String instanceID = null;
                if(elAsObj.has("itemInstanceId")) {
                    instanceID = elAsObj.getAsJsonPrimitive("itemInstanceId").toString();

                    unsortedItems.add(new ItemLookupInfo(itemHash, instanceID));
                }
            }

            // Iterate over equipped items and place in unsorted list
            JsonArray charEqpArray = charsEquip.getAsJsonObject("data").getAsJsonObject(charId).getAsJsonArray("items");

            for(JsonElement element : charEqpArray) {
                JsonObject elAsObj = element.getAsJsonObject();

                // Only try and place it in the bucket if it's in a weapon bucket
                String itemHash = elAsObj.getAsJsonPrimitive("itemHash").toString();

                String instanceID = null;
                if(elAsObj.has("itemInstanceId")) {
                    instanceID = elAsObj.getAsJsonPrimitive("itemInstanceId").toString();

                    unsortedItems.add(new ItemLookupInfo(itemHash, instanceID));
                }
            }

        }


        // Step 4 - Create a list of all items in the general bucket (must have instance id)
        // Step 4b - Go through all objects in general bucket, get their info
        // Step 4c - If it's a weapon, get its full info and store it in the appropriate list
        JsonArray inventoryItems = profileInv.getAsJsonObject("data").getAsJsonArray("items");


        // Iterate over inventory and place in buckets
        for(JsonElement element : inventoryItems) {

            // Only try and place it in the array if it's in a bucket we care about (in this case, general)
            String bucketHash = element.getAsJsonObject().getAsJsonPrimitive("bucketHash").toString();
            if(buckets.containsKey(bucketHash)) {


                // NEW THING: We're just going to put it all in the map, sort it out later
                JsonObject item = element.getAsJsonObject();

                // Test to see if the object is null
                if(item != null) {
                    String hashVal = item.getAsJsonPrimitive("itemHash").toString();
                    JsonPrimitive instanceIDPrimitive = item.getAsJsonPrimitive("itemInstanceId");
                    if(instanceIDPrimitive != null) {
                        String instanceID = instanceIDPrimitive.toString().replace("\"", "");
                        unsortedItems.add(new ItemLookupInfo(hashVal, instanceID));
                    }
                    else {
                        // If we're here, it's because it's not a weapon/armor
                        Log.d("ITEM_SORT", "instanceID didn't work " + item.toString());
                    }

                }
                else {
                    Log.d("ITEM_SORT", "element didn't work " + element.toString());
                }


            }
        }


        // Now that we have just a list of weapons, we need to sort them out
        List<DestinyItemInfo> unsortedWeapons = manifest.getAllWeaponData(unsortedItems);

        //Log.d("MANIFEST_READ", "We should have all of the manifest info for weapons now, there are this many weapons: " + unsortedWeapons.size());

        sortUnsortedWeapons(unsortedWeapons);

        // THIS IS FOR TESTING ONLY
        Map<String, DestinyItemInfo> randomItemAssortment = getRandomLoadout();

        Log.d("RANDOM_LOADOUT_TEST", "Here's the random loadout: "
                + randomItemAssortment.get("kinetic").itemName + ", "
                + randomItemAssortment.get("energy").itemName + ", "
                + randomItemAssortment.get("power").itemName);
    }

    // This sorts the unsorted weapon list returned from the reader
    public void sortUnsortedWeapons(List<DestinyItemInfo> unsortedWeapons) {
        for(DestinyItemInfo item : unsortedWeapons) {
            placeItemInBucket(item);
        }
    }

    // This method takes destiny item info (returned from the manifest) and sorts it into the correct array
    public void placeItemInBucket(DestinyItemInfo item) {

        String itemBucket = buckets.get(item.itemBucket);

        if (itemBucket == null) {
            Log.d("SORTING_ERROR", "Null exception trying to get item bucket");
        }

        switch (itemBucket) {
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


    // This method returns a map that contains an item for each slot
    // The keys are "kinetic", "energy", and "power"
    // It will prevent two exotics from being suggested
    public Map<String, DestinyItemInfo> getRandomLoadout() {

        // Create the map that will be returned
        Map<String, DestinyItemInfo> itemMap = new HashMap<>();

        // Track if we have space for an exotic weapon
        boolean exoticAllowed = true;

        // Get the kinetic weapon
        exoticAllowed = !addRandomWeaponToMap(itemMap, "kinetic", exoticAllowed);

        // Get the energy weapon
        exoticAllowed = !addRandomWeaponToMap(itemMap, "energy", exoticAllowed);

        // Get the power weapon
        addRandomWeaponToMap(itemMap, "power", exoticAllowed);

        return itemMap;
    }

    // Adds a random weapon to a map and returns if this weapon is exotic
    public boolean addRandomWeaponToMap(Map<String, DestinyItemInfo> itemMap, String weaponType, boolean exoticAllowed) {
        DestinyItemInfo weapon = null;

        // A bit inelegant, but find the right kind of weapon
        switch(weaponType) {
            case "kinetic":
                weapon = getRandomWeapon(kineticWeapons, exoticAllowed);
                break;
            case "energy":
                weapon = getRandomWeapon(energyWeapons, exoticAllowed);
                break;
            case "power":
                weapon = getRandomWeapon(powerWeapons, exoticAllowed);
                break;
            default:
                Log.d("RANDOM_WEAPON_ERROR", "No valid weapon type, must be kinetic, energy or power");
                break;
        }

        // Add it to the map
        itemMap.put(weaponType, weapon);

        // Return if this weapon is exotic
        return weapon.isExotic;
    }

    // This method returns a DestinyItemInfo object for the specified bucket
    public DestinyItemInfo getRandomWeapon(List<DestinyItemInfo> weaponList, boolean exoticAllowed) {

        // Value we'll be returning
        DestinyItemInfo weapon = null;

        // Random generator
        Random rand = new Random();

        // This breaks us out of while loops
        boolean itemFound = false;

        // Get random kinetic weapon
        while(!itemFound) {
            int rIndex = rand.nextInt(weaponList.size());

            weapon = weaponList.get(rIndex);

            // We have only found an item if exotics are allowed (thus all weapons are good)
            // OR this weapon is not exotic
            itemFound = exoticAllowed || weapon.isExotic == false;
        }

        return weapon;
    }



}
