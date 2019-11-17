package com.example.destinyitemrandomizer;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.destinyitemrandomizer.destinywrapper.DestinyManifestReader;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class DownloadManifest extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_DOWNLOAD = "com.example.destinyitemrandomizer.action.DOWNLOAD";

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


            // Store the manifest name in shared prefs
            // Save to shared prefs
            SharedPreferences prefs = getSharedPreferences("MyPref", 0);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("manifest", fileName);

            editor.commit();

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
