package com.example.destinyitemrandomizer.destinywrapper;


import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.destinyitemrandomizer.MainActivity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.w3c.dom.Text;

// This is a simple class that holds info about an item
// It is meant to be returned from the inventory sorter
// Then the info is consumed by the MainActivity for display
public class DestinyItemInfo {

    public String itemName = "ITEM NAME NOT SET";
    public String itemType = "ITEM TYPE NOT SET";
    public String itemBucket = "ITEM BUCKET NOT SET";
    public String itemElement = "ITEM ELEMENT NOT SET";
    public String itemPower = "ITEM POWER NOT SET";

    public void setInstanceID(String instanceID) {
        this.instanceID = instanceID;
    }

    public String instanceID = "ITEM INSTANCE ID NOT SET";
    public String itemImgUrl = "ITEM IMG URL NOT SET";
    public boolean isExotic = false;

    public DestinyItemInfo(){}

    public DestinyItemInfo(String instanceID){
        instanceID = instanceID;
    }

    public DestinyItemInfo(String name, String type, String element, String instance, String imgUrl, boolean exotic, String bucketHash)
    {
        itemName = name;
        itemType = type;
        itemBucket = bucketHash;
        itemElement = element;
        instanceID = instance;
        itemImgUrl = imgUrl;
        isExotic = exotic;
    }

    // This is the primary constructor used as we're reading items from the manifest in DestinyManifestReader
    public DestinyItemInfo(JsonObject itemInfo, String instanceID)
    {
        setFromJsonObject(itemInfo);
        this.instanceID = instanceID;
    }

    public void setFromJsonObject(JsonObject manifestInfo) {

        // Get the following info from the manifest
        itemName = manifestInfo.getAsJsonObject("displayProperties").getAsJsonPrimitive("name").toString().replace("\"", "");

        itemType = manifestInfo.getAsJsonPrimitive("itemTypeAndTierDisplayName").toString().replace("\"", "");

        itemBucket = manifestInfo.getAsJsonObject("inventory").getAsJsonPrimitive("bucketTypeHash").toString().replace("\"", "");

        itemElement = getElementName(manifestInfo.getAsJsonPrimitive("defaultDamageType").toString().replace("\"", ""));

        itemImgUrl = manifestInfo.getAsJsonObject("displayProperties").getAsJsonPrimitive("icon").toString().replace("\"", "");

        isExotic = itemType.toLowerCase().contains("exotic");
    }


    // Return the string for the damage type
    public static String getElementName(String type) {
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

    // Kick of the async task to get the item power
    public void requestItemPower(MainActivity activity, TextView pane) {
        DestinyAsyncTasks.DestinyTaskGetItemInstance instanceTask = new DestinyAsyncTasks.DestinyTaskGetItemInstance(activity, pane, this);
        String url = "https://www.bungie.net/Platform/Destiny2/" + activity.getMembershipType() + "/Profile/" + activity.getMembershipID() + "/Item/" + instanceID + "/?components=300";
        instanceTask.execute(url, activity.getToken());
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
