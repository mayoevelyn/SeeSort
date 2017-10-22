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

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView takePic;

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
            takePic.setImageBitmap(photo);
        }
    }

}
