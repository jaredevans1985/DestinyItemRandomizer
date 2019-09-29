package com.example.destinyitemrandomizer.destinywrapper;

import com.google.gson.JsonObject;

// This class is meant to hold a character and its currently equipped items
public class DestinyCharacterInfo {
    public String characterID;
    public DestinyItemInfo curKinetic;
    public DestinyItemInfo curEnergy;
    public DestinyItemInfo curPower;
    public String charDescription;

    public DestinyCharacterInfo(JsonObject charInfo, DestinyItemInfo kinetic, DestinyItemInfo energy, DestinyItemInfo power) {
        //charDescription = description;
        //characterID = id;
        curKinetic = kinetic;
        curEnergy = energy;
        curPower = power;
    }
}
