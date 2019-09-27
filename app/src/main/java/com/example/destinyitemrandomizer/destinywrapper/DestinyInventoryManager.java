package com.example.destinyitemrandomizer.destinywrapper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import java.io.File;

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


    // Pass in the manifest file and
    public DestinyInventoryManager(AppCompatActivity activity, JsonObject profileInv, JsonObject chars, JsonObject charsInv, JsonObject charsEquip)
    {
        // Create our instance of the manifest reader
        manifest = new DestinyManifestReader(activity);

        JsonObject test = manifest.findItemInfo("3211806999");

        // TODO: Actually do something with the JsonObjects
    }


}
