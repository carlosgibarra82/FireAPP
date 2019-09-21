package com.optic.fireapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class StartedService extends Service {
    private static final String TAG = "MyService";

    /**
     * starts the AlarmManager.
     */

    @Override
    public void onCreate() {
        super.onCreate();
        //TODO: Start ongoing notification here to make service foreground
        //Toast.makeText(this, "Ejecuta", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onStart(Intent intent, int startid) {

        //TODO: Put your AlarmManager code here
        //TODO: you also need to add some logic to check if some previous work is pending in case of a device reboot
        Toast.makeText(this, "Entro aqui", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onStart");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        //TODO: cancel the notification
        Log.d(TAG, "onDestroy");
    }
}
