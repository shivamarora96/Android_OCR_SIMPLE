package com.example.shivamarora.android_ocr_simple;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class MainActivity extends AppCompatActivity {

    TextRecognizer mTextRecogniser ;
    CameraSource mCameraSource ;
    GraphicOverlay<OcrGraphic> mGraphicOverlay ;
    CameraSourcePreview mPreview ;
    static boolean mHasLowStorage = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextRecogniser = new TextRecognizer.Builder(getApplicationContext()).build() ;
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>)findViewById(R.id.Graphicoverlay);
        mPreview = (CameraSourcePreview)findViewById(R.id.CameraSourcePreview);


    }


    @Override
    protected void onStart() {
        super.onStart();

        if(!mTextRecogniser.isOperational()){
            Toast.makeText(getApplicationContext() , "TextRecogniser is not Operational !! " , Toast.LENGTH_SHORT).show();
            //Checking For Low Storage ....
          sendBroadcast(new Intent(MainActivity.this , LowStorageReciever.class));
        }
        else{

            mTextRecogniser.setProcessor(new DynamicCameraDetector(mGraphicOverlay  ));

            mCameraSource = new CameraSource.Builder(getApplicationContext() , mTextRecogniser)
                    .setAutoFocusEnabled(true)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280 , 1024)
                    .setRequestedFps(15.0f)
                    .setAutoFocusEnabled(true)
                    .build() ;
        }






    }
}
