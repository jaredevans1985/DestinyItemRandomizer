package com.example.destinyitemrandomizer.destinywrapper;

import android.os.Message;
import android.text.style.TabStopSpan;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

// This class is in charge of reading the destiny manifest and returning results from it.
public class DestinyManifestReader {

    // The manifest file
    private File manifest;


    public DestinyManifestReader(File file)
    {
        manifest = file;
    }

    // Find an item in the database
    public JsonObject findItemInfo(String hash)
    {

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


}
