package com.example.destinyitemrandomizer.destinywrapper;

import android.os.AsyncTask;
import android.util.Log;

import com.example.destinyitemrandomizer.LoginActivity;
import com.example.destinyitemrandomizer.MainActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.smartam.leeloo.client.OAuthClient;
import net.smartam.leeloo.client.URLConnectionClient;
import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.client.response.OAuthAccessTokenResponse;
import net.smartam.leeloo.client.response.OAuthJSONAccessTokenResponse;
import net.smartam.leeloo.common.exception.OAuthProblemException;
import net.smartam.leeloo.common.exception.OAuthSystemException;
import net.smartam.leeloo.common.message.types.GrantType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// This class holds all of the potential async tasks needed for the app
public class DestinyAsyncTasks {

    // This task gets and returns the Oauth token
    public static class DestinyTaskOAuth extends AsyncTask<String, Void, OAuthAccessTokenResponse>
    {
        // Pass in the parent activity
        // TODO: Parent class that all tasks inherit from that has this parent activity
        private MainActivity activity;

        public DestinyTaskOAuth(MainActivity a)
        {
            this.activity = a;
        }

        protected OAuthAccessTokenResponse doInBackground(String... params) {

            // Build a request to get the OAuth token
            OAuthClientRequest request = null;
            try {
                request = OAuthClientRequest.tokenLocation("https://www.bungie.net/Platform/App/oauth/token/")
                        .setGrantType(GrantType.AUTHORIZATION_CODE)
                        .setClientId("29602")
                        .setClientSecret("BIavGLh-ZPr9YKlyx2wPhnXtMbVMsSkloTOotk-X2CQ")
                        .setCode(params[0])
                        .buildBodyMessage();
                        //.buildBodyMessage();

                // Add headers to prevent errors
                //request.addHeader("Accept", "application/json");
                //request.addHeader("Content-Type", "application/x-www-form-urlencoded");
            } catch (OAuthSystemException e) {
                Log.d("OAUTH_REQUEST", e.getMessage());
            }

            // We're just trying some oauth here
            // See if we can get back a token
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

            //OAuthJSONAccessTokenResponse response = null;
            OAuthAccessTokenResponse response = null;
            try {
                response = oAuthClient.accessToken(request);
            } catch (OAuthSystemException e) {
                Log.d("OAUTH_SYSTEM_EXC", e.getMessage());
            } catch (OAuthProblemException e) {
                Log.d("OAUTH_PROBLEM_EXC", e.getMessage());
            }

            return response;
        }

        protected void onPostExecute(OAuthAccessTokenResponse response)
        {
            try{

                // Get the token
                String token = response.getAccessToken();

                // Set the token
                this.activity.setToken(token);

                // Get additional user details
                // See if we can get the current user with our valid token
                DestinyTaskGet memberInfo = new DestinyTaskGet(this.activity);
                memberInfo.execute("https://www.bungie.net/Platform/User/GetMembershipsForCurrentUser/", token);
            }
            catch (NullPointerException e)
            {
                Log.d("NO_OAUTH_RESPONSE", "ERROR: No valid Oauth response, try again");
            }
        }

    }

    // This is is a generic task for Get request from the Destiny API
    // Params[0] is the url, params[1] is the OAuth token
    public static class DestinyTaskGet extends AsyncTask<String, Void, JsonObject> {
        // Pass in the parent activity
        // TODO: Parent class that all tasks inherit from that has this parent activity
        private MainActivity activity;

        public DestinyTaskGet(MainActivity a) {
            this.activity = a;
        }

        protected JsonObject doInBackground(String... params) {

            // Create a request with the provided url and token
            try {
                URL obj = new URL(params[0]);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("GET");

                // Set header
                con.setRequestProperty("X-API-KEY", "7f2b4c1bfb4c4816a3f57cff6b3f8c53");
                con.setRequestProperty("Authorization", "Bearer " + params[1]);

                int responseCode = con.getResponseCode();
                Log.d("API_GET_1", "\nSending 'GET' request to Bungie.Net : " + params[0]);
                Log.d("API_GET_2", "Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                String response = "";

                while ((inputLine = in.readLine()) != null) {
                    response += inputLine;
                }

                in.close();

                // Uses Gson - https://github.com/google/gson
                JsonParser parser = new JsonParser();
                JsonObject json = (JsonObject) parser.parse(response);

                //Log.d("API_TOKEN_TEST", "\n" + json.getAsJsonObject("Response"));

                return json.getAsJsonObject("Response");
            } catch (java.io.IOException e) {
                Log.d("API_GET_ERROR", "Get request failed with error " + e.getMessage());
            }

            return null;
        }

        public void onPostExecute(JsonObject response) {
            this.activity.parseAPIResponse(response);
        }
    }

    // This is a get request for the URL of the json manifest
    public static class DestinyGetManifestURL extends AsyncTask<Void, Void, JsonObject> {
        // Pass in the parent activity
        // TODO: Parent class that all tasks inherit from that has this parent activity
        private LoginActivity activity;

        public DestinyGetManifestURL(LoginActivity a) {
            this.activity = a;
        }

        protected JsonObject doInBackground(Void... params) {

            // Create a request with the provided url and token
            try {
                URL obj = new URL("https://www.bungie.net/Platform/Destiny2/Manifest/");
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("GET");


                int responseCode = con.getResponseCode();
                Log.d("MANIFEST_GET_1", "\nSending request for manifest URL");
                Log.d("MANIFEST_GET_2", "Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                String response = "";

                while ((inputLine = in.readLine()) != null) {
                    response += inputLine;
                }

                in.close();

                // Uses Gson - https://github.com/google/gson
                JsonParser parser = new JsonParser();
                JsonObject json = (JsonObject) parser.parse(response);

                //Log.d("API_TOKEN_TEST", "\n" + json.getAsJsonObject("Response"));

                return json.getAsJsonObject("Response");
            } catch (java.io.IOException e) {
                Log.d("MANIFEST_GET_ERROR", "Get request failed with error " + e.getMessage());
            }

            return null;
        }

        public void onPostExecute(JsonObject response) {
            String url = response.getAsJsonObject("jsonWorldContentPaths").getAsJsonPrimitive("en").toString().replace("\"", "");
            this.activity.onManifestURLFound(url);
        }
    }

    // Create the Inventory Manager
    // Do it in another thread in case sorting takes a while
    public static class DestinyCreateInventoryManagerAsync extends AsyncTask<JsonObject, Void, DestinyInventoryManager> {

        // Pass in the parent activity
        // TODO: Parent class that all tasks inherit from that has this parent activity
        private MainActivity activity;

        public DestinyCreateInventoryManagerAsync(MainActivity a) {
            this.activity = a;
        }

        protected DestinyInventoryManager doInBackground(JsonObject... jsonObjects) {

            // Kick off creating an inventory manager with the provided info
            DestinyInventoryManager invMan = new DestinyInventoryManager(this.activity, jsonObjects[0], jsonObjects[1], jsonObjects[2], jsonObjects[3]);

            return invMan;
        }

        public void onPostExecute(DestinyInventoryManager invMan)
        {
            // Set the activity's inventory manager to the newly created instance
            this.activity.setInventoryManager(invMan);
        }
    }

    // This is used by item info to get item power
    // Params[0] is the url, params[1] is the OAuth token
    public static class DestinyTaskGetItemInstance extends AsyncTask<String, Void, JsonObject> {
        // Pass in the parent activity
        // TODO: Parent class that all tasks inherit from that has this parent activity
        private DestinyItemInfo item;

        public DestinyTaskGetItemInstance(DestinyItemInfo i) {
            this.item = i;
        }

        protected JsonObject doInBackground(String... params) {

            // Create a request with the provided url and token
            try {
                URL obj = new URL(params[0]);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("GET");

                // Set header
                con.setRequestProperty("X-API-KEY", "7f2b4c1bfb4c4816a3f57cff6b3f8c53");
                con.setRequestProperty("Authorization", "Bearer " + params[1]);

                int responseCode = con.getResponseCode();
                Log.d("API_GET_1", "\nSending 'GET' request to Bungie.Net : " + params[0]);
                Log.d("API_GET_2", "Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                String response = "";

                while ((inputLine = in.readLine()) != null) {
                    response += inputLine;
                }

                in.close();

                // Uses Gson - https://github.com/google/gson
                JsonParser parser = new JsonParser();
                JsonObject json = (JsonObject) parser.parse(response);

                //Log.d("API_TOKEN_TEST", "\n" + json.getAsJsonObject("Response"));

                return json.getAsJsonObject("Response");
            } catch (java.io.IOException e) {
                Log.d("API_GET_ERROR", "Get request failed with error " + e.getMessage());
            }

            return null;
        }

        public void onPostExecute(JsonObject response) {
            this.item.setPower(response);
        }
    }


}
