package com.example.destinyitemrandomizer.destinywrapper;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// This is a simple class that holds info about an item
// It is meant to be returned from the inventory sorter
// Then the info is consumed by the MainActivity for display
public class DestinyItemInfo {

    public final String itemName;
    public final String itemType;
    public final String itemElement;
    public final String itemPower;
    public final String instanceID;
    public final String itemImgUrl;

    public DestinyItemInfo(String name, String type, String element, String power, String instance, String imgUrl)
    {
        itemName = name;
        itemType = type;
        itemElement = element;
        itemPower = power;
        instanceID = instance;
        itemImgUrl = imgUrl;
    }

    public DestinyItemInfo(JsonElement itemInfo)
    {
        // TODO: This REAL flimsy, no guarantee this worked by the time we're here

        String hashVal = itemInfo.getAsJsonObject().getAsJsonPrimitive("itemHash").toString();
        JsonObject manifestInfo = DestinyManifestReader.instance.findItemInfo(hashVal);

        itemName = manifestInfo.getAsJsonObject("displayProperties").getAsJsonPrimitive("name").toString();
        itemType = manifestInfo.getAsJsonPrimitive("itemTypeDisplayName").toString();
        itemElement = hashVal;
        itemPower = hashVal;
        instanceID = hashVal;
        itemImgUrl = hashVal;
    }

    public String toString()
    {
        String itemInfo = itemPower + " " + itemName + ": " + itemElement + " " + itemType;
        return itemInfo;
    }
}
