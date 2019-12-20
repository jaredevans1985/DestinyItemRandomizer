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

    // Pass in the character info and the character equipment
    public DestinyCharacterInfo(JsonObject charInfo, JsonArray charEquipped) {
        charDescription = buildCharacterDescription(charInfo);
        characterID = charInfo.getAsJsonPrimitive("characterId").toString();
    }

    public static String buildCharacterDescription(JsonObject charInfo) {
        String race = getCharacterRace(charInfo.getAsJsonPrimitive("raceType").getAsString());
        String charClass = getCharacterClass(charInfo.getAsJsonPrimitive("classType").getAsString());
        String power = charInfo.getAsJsonPrimitive("light").getAsString();

        return power + " " + race + " " + charClass;
    }

    public static String getCharacterRace(String raceType) {
        switch(raceType) {
            case "0":
                return "Human";
            case "1":
                return "Awoken";
            case "2":
                return "Exo";
            default:
                return "NO RACE FOUND";
        }
    }

    public static String getCharacterClass(String classType) {
        switch(classType) {
            case "0":
                return "Titan";
            case "1":
                return "Hunter";
            case "2":
                return "Warlock";
            default:
                return "NO CLASS FOUND";
        }
    }
}
