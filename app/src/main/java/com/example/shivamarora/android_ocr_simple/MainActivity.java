package com.example.shivamarora.android_ocr_simple;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.example.shivamarora.android_ocr_simple.Camera.CameraSource;
import com.example.shivamarora.android_ocr_simple.Camera.CameraSourcePreview;
import com.example.shivamarora.android_ocr_simple.Camera.GraphicOverlay;


import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextRecognizer mTextRecogniser ;

    GraphicOverlay<OcrGraphic> mGraphicOverlay ;
    CameraSourcePreview mPreview ;
    static boolean mHasLowStorage = false;
    CameraSource mCameraSource ;

    public static final String AutoFocus = "FOcus";
    public static final String UseFlash = "Flash";
    private static final int RC_HANDLE_GMS = 2;
    private static final int RC_HANDLE_CAMERA_PERM = 0;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    boolean mAutoFocus = true ;
    boolean mFlash  = true ;
    TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.Graphicoverlay);
        mPreview = (CameraSourcePreview) findViewById(R.id.CameraSourcePreview);

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        // TODO: Set up the Text To Speech engine.
        tts = new TextToSpeech(this.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(final int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d("TTS", "Text to speech engine started successfully.");
                    tts.setLanguage(Locale.US);
//                        Log.i("LOCALE" ,tts.get)
                } else {
                    Log.d("TTS", "Error starting the text to speech engine.");
                }
            }
        });


//        Set<Locale> language = tts.getAvailableLanguages()


    }


    @Override
    protected void onStart() {
        super.onStart();
        int checkPermission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        if (checkPermission == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(mFlash,mAutoFocus);
        } else {
            requestCameraPermission();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }


    @Override
    protected void onPause() {
        super.onPause();

        if(mPreview!=null)
        mPreview.stop();
    }


    @Override
    protected void onStop() {
        super.onStop();

        if(mPreview!=null)
        mPreview.release();
    }

    void createCameraSource(boolean useFlash , boolean autoFocus){

        mTextRecogniser = new TextRecognizer
                .Builder(getApplicationContext())
                .build() ;

        if(!mTextRecogniser.isOperational()){
            Toast.makeText(getApplicationContext() , "TextRecogniser is not Operational !! " , Toast.LENGTH_SHORT).show();
            //Checking For Low Storage ....
            sendBroadcast(new Intent(MainActivity.this , LowStorageReciever.class));
        }
        else{

            mTextRecogniser.setProcessor(new DynamicCameraDetector(mGraphicOverlay  ));

            mCameraSource = new CameraSource.Builder(getApplicationContext() , mTextRecogniser)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280 , 1024)
                    .setRequestedFps(15.0f)
                    .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                    .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE:null)
                    .build() ;

        }
    }


    private void requestCameraPermission() {

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
        }

        else {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(MainActivity.this , permissions,
                            RC_HANDLE_CAMERA_PERM);
                }
            };
        }
    }


    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());

        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }




        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e("Tag", "Unable to start camera source.", e);
//                mCameraSource.release();
//                mCameraSource = null;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != 0) {
            Log.d("TAG", "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Camera permission granted - initialize the camera source");
            // We have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus,true);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(useFlash , autoFocus);
            return;
        }

        Log.e("TAG", "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage("YOU DIDN'T PROVIDE CAMERA PERMISSION !! ")
                .setPositiveButton(" OK " , listener)
                .show();
    }



    // * @param rawX - the raw position of the tap
    // * @param rawY - the raw position of the tap.
    // * @return true if the tap was on a TextBlock


    private boolean onTap(float rawX, float rawY) {
        // TODO: Speak the text when the user taps on screen.
        OcrGraphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            text = graphic.getTextBlock();
            Log.i("lang :: " , text.getLanguage() ) ;
            if (text != null && text.getValue() != null) {
                Log.d("TAG", "text data is being spoken! --> " + text.getValue());
                // Speak the string.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts.speak(text.getValue(), TextToSpeech.QUEUE_ADD, null, "DEFAULT");
                }
            }
            else {
                Log.d("TAG", "text data is null");
            }
        }
        else {
            Log.d("TAG","no text detected");
        }
        return text != null;
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (mCameraSource != null) {
                mCameraSource.doZoom(detector.getScaleFactor());
            }
        }
    }


}
