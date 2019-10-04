package com.example.destinyitemrandomizer.destinywrapper;

import com.google.gson.JsonObject;

// This class is meant to hold a character and its currently equipped items
public class DestinyCharacterInfo {
    public String characterID;
    public DestinyItemInfo curKinetic;
    public DestinyItemInfo curEnergy;
    public DestinyItemInfo curPower;
    public String charDescription;

    // Pass in the character info and the character equipment, store need pieces
    public DestinyCharacterInfo(JsonObject charInfo, JsonObject charEquipped) {
        //charDescription = description;
        //characterID = id;
        //curKinetic = kinetic;
        //curEnergy = energy;
       // curPower = power;
    }
}
