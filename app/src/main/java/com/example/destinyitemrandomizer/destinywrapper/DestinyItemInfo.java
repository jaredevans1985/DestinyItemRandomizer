package com.example.destinyitemrandomizer.destinywrapper;



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

    public String toString()
    {
        String itemInfo = itemPower + " " + itemName + ": " + itemElement + " " + itemType;
        return itemInfo;
    }
}
