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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView takePic;

    public static final String subscriptionKey = "";

    public class WebServiceRequest {
        private static final String headerKey = "ocp-apim-subscription-key";
        private HttpClient client = new DefaultHttpClient();
        private String subscriptionKey;
        private Gson gson = new Gson();

        public WebServiceRequest(String key) {
            this.subscriptionKey = key;
        }

        public Object request(String url, String method, Map<String, Object> data, String contentType, boolean responseInputStream) throws VisionServiceException {
            if (method.matches("GET")) {
                return get(url);
            } else if (method.matches("POST")) {
                return post(url, data, contentType, responseInputStream);
            } else if (method.matches("PUT")) {
                return put(url, data);
            } else if (method.matches("DELETE")) {
                return delete(url);
            } else if (method.matches("PATCH")) {
                return patch(url, data, contentType, false);
            }

            throw new VisionServiceException("Error! Incorrect method provided: " + method);
        }

        private Object get(String url) throws VisionServiceException {
            HttpGet request = new HttpGet(url);
            request.setHeader(headerKey, this.subscriptionKey);

            try {
                HttpResponse response = this.client.execute(request);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    return readInput(response.getEntity().getContent());
                } else {
                    throw new Exception("Error executing GET request! Received error code: " + response.getStatusLine().getStatusCode());
                }
            } catch (Exception e) {
                throw new VisionServiceException(e.getMessage());
            }
        }

        private Object post(String url, Map<String, Object> data, String contentType, boolean responseInputStream) throws VisionServiceException {
            return webInvoke("POST", url, data, contentType, responseInputStream);
        }

        private Object patch(String url, Map<String, Object> data, String contentType, boolean responseInputStream) throws VisionServiceException {
            return webInvoke("PATCH", url, data, contentType, responseInputStream);
        }

        private Object webInvoke(String method, String url, Map<String, Object> data, String contentType, boolean responseInputStream) throws VisionServiceException {
            HttpPost request = null;

            if (method.matches("POST")) {
                request = new HttpPost(url);
            } else if (method.matches("PATCH")) {
                //request = new HttpPatch(url);
            }

            boolean isStream = false;

        /*Set header*/
            if (contentType != null && !contentType.isEmpty()) {
                request.setHeader("Content-Type", contentType);
                if (contentType.toLowerCase().contains("octet-stream")) {
                    isStream = true;
                }
            } else {
                request.setHeader("Content-Type", "application/json");
            }

            request.setHeader(headerKey, this.subscriptionKey);

            try {
                if (!isStream) {
                    String json = this.gson.toJson(data).toString();
                    StringEntity entity = new StringEntity(json);
                    request.setEntity(entity);
                } else {
                    request.setEntity(new ByteArrayEntity((byte[]) data.get("data")));
                }

                HttpResponse response = this.client.execute(request);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    if(!responseInputStream) {
                        return readInput(response.getEntity().getContent());
                    }else {
                        return response.getEntity().getContent();
                    }
                }else if(statusCode==202)
                {
                    return response.getFirstHeader("Operation-Location").getValue();
                }
                else {
                    throw new Exception("Error executing POST request! Received error code: " + response.getStatusLine().getStatusCode());
                }
            } catch (Exception e) {
                throw new VisionServiceException(e.getMessage());
            }
        }

        private Object put(String url, Map<String, Object> data) throws VisionServiceException {
            HttpPut request = new HttpPut(url);
            request.setHeader(headerKey, this.subscriptionKey);

            try {
                String json = this.gson.toJson(data).toString();
                StringEntity entity = new StringEntity(json);
                request.setEntity(entity);
                request.setHeader("Content-Type", "application/json");
                HttpResponse response = this.client.execute(request);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200 || statusCode == 201) {
                    return readInput(response.getEntity().getContent());
                } else {
                    throw new Exception("Error executing PUT request! Received error code: " + response.getStatusLine().getStatusCode());
                }
            } catch (Exception e) {
                throw new VisionServiceException(e.getMessage());
            }
        }

        private Object delete(String url) throws VisionServiceException {
            HttpDelete request = new HttpDelete(url);
            request.setHeader(headerKey, this.subscriptionKey);

            try {
                HttpResponse response = this.client.execute(request);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new Exception("Error executing DELETE request! Received error code: " + response.getStatusLine().getStatusCode());
                }

                return readInput(response.getEntity().getContent());
            } catch (Exception e) {
                throw new VisionServiceException(e.getMessage());
            }
        }

        public String getUrl(String path, Map<String, Object> params) {
            StringBuffer url = new StringBuffer(path);

            boolean start = true;
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (start) {
                    url.append("?");
                    start = false;
                } else {
                    url.append("&");
                }

                try {
                    url.append(param.getKey() + "=" + URLEncoder.encode(param.getValue().toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            return url.toString();
        }

        private String readInput(InputStream is) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuffer json = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                json.append(line);
            }

            return json.toString();
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
            Bitmap photo = (Bitmap) extra.get("data");
            WebServiceRequest webs = new WebServiceRequest("c265a7549014410daffebda580bb22f1");
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("pic1", photo);

            String builtURL = webs.getUrl("/", map1); //unsure what the path should be
            String jsonData = null;
            try { // android studio literally made me use a try catch. This is by necessity, not choice.
                jsonData = (String) webs.request(builtURL, "PUT", map1, "bitmap", true);
            } catch (VisionServiceException e) {
                e.printStackTrace();
            }

            takePic.setImageBitmap(photo);

            String[] compostTags = {"banana", "bowl", "cake", "chocolate", "different", "eaten", "filled",
                    "food", "fruit", "holding", "indoor", "orange", "piece", "plate", "salad", "sitting", "slice", "sliced"};
            String[] recycleTags = {"aluminum", "batteries", "bottle", "computer", "electronic", "glass", "mixed paper",
                    "plastic", "shredded"};

            // the following code assumes we get json data in a certain desired format. If we do not
            // have the json file in our specific format, this will not work as intended.
            Scanner input = new Scanner(jsonData);
            String check = input.nextLine();
            while (!input.hasNextLine() && !check.contains("Description")) {
                check = input.nextLine();
            }
            input.nextLine(); //guarunteed to exist. Discards "tags" line
            check = input.nextLine(); //assumed to exist because there is a description, therefore there are tags
            String decision = makeDecision(check, input, compostTags, recycleTags);
            Toast present = Toast.makeText(getApplicationContext(), decision, Toast.LENGTH_LONG);
            present.show();
        }
    }


    // goes through the tags on the image and decides whether it should be put in the recycling bin,
    // compost bin, or trash bin.
    private String makeDecision(String check, Scanner input, String[] compostTags, String[] recycleTags) {
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
            return makeDecision(check, input, compostTags, recycleTags);
        }
    }

}
