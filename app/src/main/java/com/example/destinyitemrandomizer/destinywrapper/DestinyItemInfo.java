package com.example.destinyitemrandomizer.destinywrapper;


import androidx.appcompat.app.AppCompatActivity;

import com.example.destinyitemrandomizer.MainActivity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// This is a simple class that holds info about an item
// It is meant to be returned from the inventory sorter
// Then the info is consumed by the MainActivity for display
public class DestinyItemInfo {

    public final String itemName;
    public final String itemType;
    public final String itemElement;
    public String itemPower = "ITEM POWER NOT SET";    // TODO: This is so bad... shouldn't have a mix of these
    public final String instanceID;
    public final String itemImgUrl;
    public final boolean isExotic;

    public DestinyItemInfo(String name, String type, String element, String power, String instance, String imgUrl, boolean exotic)
    {
        itemName = name;
        itemType = type;
        itemElement = element;
        instanceID = instance;
        itemPower = power;
        itemImgUrl = imgUrl;
        isExotic = exotic;
    }

    public DestinyItemInfo(MainActivity activity, JsonElement itemInfo)
    {
        // TODO: This REAL flimsy, no guarantee this worked by the time we're here
        JsonObject item = itemInfo.getAsJsonObject();
        String hashVal = item.getAsJsonPrimitive("itemHash").toString();
        JsonObject manifestInfo = DestinyManifestReader.instance.findItemInfo(hashVal);

        // Get the following info from the manifest
        itemName = manifestInfo.getAsJsonObject("displayProperties").getAsJsonPrimitive("name").toString();

        itemType = manifestInfo.getAsJsonPrimitive("itemTypeAndTierDisplayName").toString();

        itemElement = getElementName(manifestInfo.getAsJsonPrimitive("defaultDamageType").toString());

        itemImgUrl = manifestInfo.getAsJsonObject("displayProperties").getAsJsonPrimitive("icon").toString();

        isExotic = itemType.toLowerCase().contains("exotic");

        // Use instance ID to get the item power
        instanceID = item.getAsJsonPrimitive("itemInstanceId").toString().replace("\"", "");

        // This won't be set until later
        DestinyAsyncTasks.DestinyTaskGetItemInstance instanceTask = new DestinyAsyncTasks.DestinyTaskGetItemInstance(this);
        String url = "https://www.bungie.net/Platform/Destiny2/" + activity.getMembershipType() + "/Profile/" + activity.getMembershipID() + "/Item/" + instanceID + "/?components=300";
        instanceTask.execute(url, activity.getToken());
    }

    // Return the string for the damage type
    private String getElementName(String type) {
        switch(type){
            case("1"):
                return "Kinetic";
            case("2"):
                return "Arc";
            case("3"):
                return "Solar";
            case("4"):
                return "Void";
            default:
                return "Damage Type Not Found";
        }
    }

    // Set the power when async task is done
    public void setPower(JsonObject instanceInfo) {
        itemPower = instanceInfo.getAsJsonObject("instance").getAsJsonObject("data").getAsJsonObject("primaryStat").getAsJsonPrimitive("value").toString();
    }


    public String toString()
    {
        String itemInfo = itemPower + " " + itemName + ": " + itemElement + " " + itemType;
        return itemInfo;
    }
}
