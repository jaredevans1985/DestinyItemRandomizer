package com.example.destinyitemrandomizer.destinywrapper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.destinyitemrandomizer.MainActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// This class is meant to hold a character and its currently equipped items
public class DestinyCharacterInfo {
    public String characterID;
    public DestinyItemInfo curKinetic;
    public DestinyItemInfo curEnergy;
    public DestinyItemInfo curPower;
    public String charDescription;

    // Pass in the character info and the character equipment, store need pieces
    // TODO: Finish setting character info
    public DestinyCharacterInfo(MainActivity activity, JsonObject charInfo, JsonArray charEquipped) {
        charDescription = "Desc";
        characterID = "Breakpoint";
        curKinetic = new DestinyItemInfo(charEquipped.get(0));
        curEnergy = new DestinyItemInfo(charEquipped.get(1));
        curPower = new DestinyItemInfo(charEquipped.get(2));
    }
}
