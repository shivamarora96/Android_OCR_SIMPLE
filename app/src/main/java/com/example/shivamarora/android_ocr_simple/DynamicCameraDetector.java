package com.example.shivamarora.android_ocr_simple;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;


public class DynamicCameraDetector implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay ;

    public DynamicCameraDetector(GraphicOverlay<OcrGraphic> mGraphicOverlay) {
        this.mGraphicOverlay = mGraphicOverlay;
    }


    @Override
    public void release() {
            mGraphicOverlay.clear();
    }

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        SparseArray<TextBlock> textBlockSparseArray =   detections.getDetectedItems() ;

        for(int i = 0 ;  i <textBlockSparseArray.size() ; i++){
            if(textBlockSparseArray.valueAt(i)!=null && textBlockSparseArray.valueAt(i).getValue()!=null){
                Log.i("TextBlocksValue :: " , textBlockSparseArray.valueAt(i).getValue()) ;
            }
            OcrGraphic currentOcrGraphic = new OcrGraphic( mGraphicOverlay , textBlockSparseArray.valueAt(i)) ;
            mGraphicOverlay.add(currentOcrGraphic);
        }

    }
}

