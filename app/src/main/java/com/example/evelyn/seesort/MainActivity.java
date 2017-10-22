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
import java.util.Scanner;

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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView takePic;
    String[] compostTags = {"banana", "bowl", "cake", "chocolate", "different", "eaten", "filled", "food", "fruit", "holding", "indoor", "orange",
                        "piece", "plate", "salad", "sitting", "slice", "sliced"};
    String[] recycleTags = {"aluminum", "batteries", "bottle", "computer", "electronic", "glass", "mixed paper", "plastic", "shredded"};

    public static final String subscriptionKey = "c265a7549014410daffebda580bb22f1";

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
        private String readableJSON;

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
                    readableJSON = json.toString(2);
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
            new RetrieveFeedTask().execute();

            String jsonData = "something"; //something will be replaced with a string full of json data

            Bitmap photo = (Bitmap) extra.get("data");
            takePic.setImageBitmap(photo);

            Scanner input = new Scanner(jsonData);
            String check = input.nextLine();
            while (!input.hasNextLine() && !check.contains("Description")) {
                check = input.nextLine();
            }
            input.nextLine(); //guarunteed to exist. Discards "tags" line
            check = input.nextLine(); //assumed to exist because there is a description, therefore there are tags
            String decision = makeDecision(check, input);
            Toast present = Toast.makeText(getApplicationContext(), decision, Toast.LENGTH_LONG);
            present.show();
        }
    }

    public String makeDecision(String check, Scanner input) {
        if (!input.hasNextLine() || check.contains("]")) {
            return "trash";
        } else {
            check = check.substring(1, check.length() - 1);

            // check to see if the tag is in the list of designated recycle tags
            for (int i = 0; i < recycleTags.length; i++) {
                if (check.equals(recycleTags[i])) {
                    return "recycle";
                }
            }
            // check to see if the tag is in the list of designated compost tags
            for (int i = 0; i < compostTags.length; i++) {
                if (check.equals(compostTags[i])) {
                    return "compost";
                }
            }
            check = input.nextLine();
            return makeDecision(check, input);
        }
    }

}
