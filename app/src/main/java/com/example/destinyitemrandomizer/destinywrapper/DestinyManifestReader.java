package com.example.destinyitemrandomizer.destinywrapper;

import android.os.Message;
import android.text.style.TabStopSpan;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// This class is in charge of reading the destiny manifest and returning results from it.
public class DestinyManifestReader {

    // A singleton
    public static DestinyManifestReader instance;

    // A static variable that holds the filename
    public static String manifestFileName = "";

    // These are the bucket hashes used for sorting items
    private String kineticBucketHash;
    private String energyBucketHash;
    private String powerBucketHash;
    private String vaultBucketHash;

    // The manifest file
    private File manifest;

    // Get the file ourselves
    private DestinyManifestReader(AppCompatActivity activity)
    {
        if(!manifestFileName.equals(""))
        {
            manifest = activity.getBaseContext().getFileStreamPath(manifestFileName);
        }
    }

    // Create the singleton instance of this class
    public static DestinyManifestReader createDestinyManifestReader(AppCompatActivity activity)
    {
        instance = new DestinyManifestReader(activity);
        return instance;
    }

    // Use a passed in file
    public DestinyManifestReader(File file)
    {
        manifest = file;
    }

    // Returns the matching hash value for given bucket
    public Map<String, String> getBucketHashes()
    {
        Map<String, String> buckets = new HashMap<>();

        // Destiny Inventory Bucket Definition
        // displayProperties.name

            // Kinetic Weapons - 1498876634
            // Energy Weapons - 2465295065
            // Power Weapons - 953998645
            // Vault (General ?) - 138197802

        try{

            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(manifest), StandardCharsets.UTF_8));
            Gson gson = new Gson();

            // Begin the initial object
            reader.beginObject();

            // Boolean that marks if we're inside of the bucket definitions
            boolean insideBucketDefs = false;

            // Look for bucket defs
            while(reader.hasNext() && !insideBucketDefs)
            {
                // Look for the next BEGIN_OBJECT, and check the path
                if(reader.peek().toString().equals("BEGIN_OBJECT")) {
                    // Get the path to determine where we are
                    String path = reader.getPath();

                    // If this is the start of the buckets, begin another object
                    if (path.equals("$.DestinyInventoryBucketDefinition")) {
                        //reader.beginObject();
                        insideBucketDefs = true;
                    }
                    // If this wasn't an object that we care about, move ahead
                    else
                    {
                        reader.skipValue();
                    }
                }
                // If this wasn't an object that we care about, move ahead
                else
                {
                    reader.nextName();
                }
            }

            // Get it as a JsonObject
            JsonObject bucketObj = new Gson().fromJson(reader, JsonObject.class);

            // Close the reader, we're done with it
            reader.close();

            // Once we're there, look for all four bucket hashes
            while(buckets.size() < 4)
            {

                Set<String> keys = bucketObj.keySet();

                // Check what its display property is
                // Add it and the hash value to the map, or move on
                for(String key : keys)
                {
                    JsonObject displayProperties = bucketObj.getAsJsonObject(key).getAsJsonObject("displayProperties");
                    if(displayProperties.has("name")) {
                        String bucketName = displayProperties.getAsJsonPrimitive("name").toString();

                        bucketName = bucketName.toLowerCase();

                        if (bucketName.contains("kinetic")) {
                            buckets.put(bucketObj.getAsJsonObject(key).getAsJsonPrimitive("hash").toString(), "kinetic");
                        } else if (bucketName.contains("energy")) {
                            buckets.put(bucketObj.getAsJsonObject(key).getAsJsonPrimitive("hash").toString(), "energy");
                        } else if (bucketName.contains("power")) {
                            buckets.put(bucketObj.getAsJsonObject(key).getAsJsonPrimitive("hash").toString(), "power");
                        } else if (bucketName.contains("general")) {
                            buckets.put(bucketObj.getAsJsonObject(key).getAsJsonPrimitive("hash").toString(), "general");
                        }
                    }

                }

            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return buckets;
    }

    // Set all bucket hashes for comparison

    // Find an item in the database
    public JsonObject findItemInfo(String hash)
    {

        // Do some Json parsing here


        try{
            JsonObject itemInfo = null;

            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(manifest), StandardCharsets.UTF_8));
            Gson gson = new Gson();

            // Begin the initial object
            reader.beginObject();

            // Look for item database
            while(reader.hasNext())
            {
                //if(next.equals("DestinyInventoryItemDefinition"))
                // Look for the next BEGIN_OBJECT, and check the path
                if(reader.peek().toString().equals("BEGIN_OBJECT")) {
                    // Get the path to determine where we are
                    String path = reader.getPath();

                    // If this is the start of the inventory items, begin another object
                    if (path.equals("$.DestinyInventoryItemDefinition")) {
                        reader.beginObject();
                    }
                    // If this is our hash, return the json object and break the loop
                    else if (path.contains(hash)) {
                        itemInfo = new Gson().fromJson(reader, JsonObject.class);

                        break;
                    }
                    // If this wasn't an object that we care about, move ahead
                    else
                    {
                        reader.skipValue();
                    }
                }
                // If this wasn't an object that we care about, move ahead
                else
                {
                    reader.nextName();
                }
            }

            reader.close();
            return itemInfo;

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // We ended up with nothing
        return null;
    }

    // Move through the map of unsorted items and get their info
    // The idea is that, by doing it all at once now, we only have to go through the file once
    public List<DestinyItemInfo> sortAllUnsortedItems(Map<String, String> unsortedItems) {

        List<DestinyItemInfo> unsortedWeapons = new ArrayList<>();

        try{
            JsonObject itemInfo = null;

            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(manifest), StandardCharsets.UTF_8));
            Gson gson = new Gson();

            // Begin the initial object
            reader.beginObject();

            // Look for item database
            while(reader.hasNext())
            {
                //if(next.equals("DestinyInventoryItemDefinition"))
                // Look for the next BEGIN_OBJECT, and check the path
                if(reader.peek().toString().equals("BEGIN_OBJECT")) {
                    // Get the path to determine where we are
                    String path = reader.getPath();

                    // Split the path apart to use for hash checking
                    String hash = "NOT_SET";
                    String[] splitPath = path.split("\\.");

                    if(splitPath.length >= 3) {
                        hash = splitPath[2];
                    }

                    // If this is the start of the inventory items, begin another object
                    if (path.equals("$.DestinyInventoryItemDefinition")) {
                        reader.beginObject();
                    }
                    // If this is our hash, return the json object and break the loop
                    else if (hash.equals("NOT_SET") == false && unsortedItems.containsKey(hash)) {
                        JsonObject itemObject = new Gson().fromJson(reader, JsonObject.class);

                        // Only continue if this has a damage type
                        if(itemObject.has("itemType") && itemObject.getAsJsonPrimitive("itemType").getAsInt() == 3) {
                            DestinyItemInfo itemInfoObject = new DestinyItemInfo(itemObject, unsortedItems.get(hash));
                            itemInfoObject.setFromJsonObject(itemObject);
                            unsortedWeapons.add(itemInfoObject);
                        }
                    }
                    // If this wasn't an object that we care about, move ahead
                    else
                    {
                        reader.skipValue();
                    }
                }
                // If this wasn't an object that we care about, move ahead
                else
                {
                    reader.nextName();
                }
            }

            reader.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return unsortedWeapons;
    }

    public boolean doesPathContainHash(String path, HashMap<String, DestinyItemInfo> unsortedItems) {



        return false;
    }

}
