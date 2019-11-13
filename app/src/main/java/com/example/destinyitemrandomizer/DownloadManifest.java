package com.example.destinyitemrandomizer;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadManifest extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_DOWNLOAD = "com.example.destinyitemrandomizer.action.DOWNLOAD";

    // TODO: Rename parameters
    public static final String EXTRA_URL = "com.example.destinyitemrandomizer.extra.URL";
    public static final String EXTRA_MESSAGE = "com.example.destinyitemrandomizer.extra.MESSAGE";

    public DownloadManifest() {
        super("DownloadManifest");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionDownload(Context context, String param1) {
        Intent intent = new Intent(context, DownloadManifest.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_URL, param1);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_URL);
                handleActionDownload(url);
            }
        }
    }

    /**
     * Handle action Download in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDownload(String urlStr) {
        FileOutputStream fos=null;
        InputStream is=null;

        String message = "Download Failed";
        try {

            // Get InputStream from the image url
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //connection.setDoInput(true);
            //connection.connect();
            is = connection.getInputStream();

            String fileName = urlStr.substring(urlStr.lastIndexOf('/') + 1);

            //fos = new FileOutputStream(Environment.getExternalStorageDirectory()+"/"+fileName);
            fos = openFileOutput(fileName, Context.MODE_PRIVATE);

            byte[] buffer = new byte[1024];
            int count;
            while((count = is.read(buffer))>0){
                fos.write(buffer,0,count);
            }

            fos.flush();


            message="Download completed";

        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            if(fos!=null){
                try {
                    fos.close();

                }catch(IOException e){}

            }
            if(is!=null){
                try {
                    is.close();

                }catch(IOException e){}

            }

            // Send the feedback message to the MainActivity
            Intent backIntent=new Intent(DownloadManifest.ACTION_DOWNLOAD);
            backIntent.putExtra(DownloadManifest.EXTRA_MESSAGE, message);
            sendBroadcast(backIntent);
        }
    }


}
