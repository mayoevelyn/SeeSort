package com.example.evelyn.seesort;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import static com.example.evelyn.seesort.R.id.takePic;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView takePic;

    public static final String subscriptionKey = "00ca1b7a85b64189ad1bc3abd7b19eea";

    // Replace or verify the region.
    //
    // You must use the same region in your REST API call as you used to obtain your subscription keys.
    // For example, if you obtained your subscription keys from the westus region, replace
    // "westcentralus" in the URI below with "westus".
    //
    // NOTE: Free trial subscription keys are generated in the westcentralus region, so if you are using
    // a free trial subscription key, you should not need to change this region.
    public static final String uriBase = "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0/analyze";


    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... params) {
            //Don't do this (massive Try/catch)
            try {
                HttpClient httpclient = new DefaultHttpClient();
                URIBuilder builder = new URIBuilder(uriBase);

                // Request parameters. All of them are optional.
                builder.setParameter("visualFeatures", "Categories,Description,Color");
                builder.setParameter("language", "en");

                // Prepare the URI for the REST API call.
                URI uri = builder.build();
                HttpPost request = new HttpPost(uri);

                // Request headers.
                request.setHeader("Content-Type", "application/json");
                request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

                // Request body.
                StringEntity reqEntity = new StringEntity("{\"url\":\"https://ih1.redbubble.net/image.92345144.8711/flat,800x800,075,t.u1.jpg\"}");
                request.setEntity(reqEntity);

                // Execute the REST API call and get the response entity.
                HttpResponse response = httpclient.execute(request);
                HttpEntity entity = response.getEntity();

                if (entity != null)
                {
                    // Format and display the JSON response.
                    String jsonString = EntityUtils.toString(entity);
                    JSONObject json = new JSONObject(jsonString);
                    System.out.println("REST Response:\n");
                    System.out.println(json.toString(2));
                }
                return null;

            }
            catch(URISyntaxException ex) {

            }
            catch (IOException ex) {

            }
            catch(JSONException ex)
            {

            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        new RetrieveFeedTask().execute();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Button picButton = (Button) findViewById(R.id.picButton);
        takePic = (ImageView) findViewById(R.id.takePic);

        // check to see if the android device has a camera
        if (!hasCamera()) {
            picButton.setEnabled(false);
        }
    }


    // makes sure that the android device has a camera. If it doesn't, return false,
    // otherwise it returns true.
    public boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public void cameraLaunch(View view) {
        Intent cap = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // take a picture and pass it to onActivityResult
        startActivityForResult(cap, REQUEST_IMAGE_CAPTURE);
    }

    // returns the image for analysis through Google Vision API
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extra = data.getExtras();
            Bitmap photo = (Bitmap) extra.get("data");
            takePic.setImageBitmap(photo);
        }
    }

}
