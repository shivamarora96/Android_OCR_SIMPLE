package com.example.shivamarora.android_ocr_simple;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class LowStorageReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == Intent.ACTION_DEVICE_STORAGE_LOW){
            MainActivity.mHasLowStorage = true;
            Toast.makeText(context , "Device has Low Storage" , Toast.LENGTH_LONG).show();
        }
    }
}
