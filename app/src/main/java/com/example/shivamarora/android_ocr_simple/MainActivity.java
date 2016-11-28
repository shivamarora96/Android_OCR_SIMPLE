package com.example.shivamarora.android_ocr_simple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.text.TextRecognizer;

public class MainActivity extends AppCompatActivity {

    TextRecognizer mTextRecogniser ;
    CameraSource mCameraSource ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextRecogniser = new TextRecognizer.Builder(getApplicationContext()).build() ;

       if(!mTextRecogniser.isOperational()){
           Toast.makeText(getApplicationContext() , "Text Recogniser is not Operational !! " , Toast.LENGTH_LONG).show();
       }
        else{

           mCameraSource = new CameraSource.Builder(getApplicationContext() , mTextRecogniser)
                   .setAutoFocusEnabled(true)
                   .setFacing(CameraSource.CAMERA_FACING_BACK)
//                 .setRequestedPreviewSize(1280 , 1024)
                    .build() ;
           
       }



    }





}
