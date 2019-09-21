package com.optic.fireapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class Autostart extends BroadcastReceiver {

    /**
     * Listens for Android's BOOT_COMPLETED broadcast and then executes
     * the onReceive() method.
     */
    @Override
    public void onReceive(Context context, Intent arg1) {
        Toast.makeText(context, "La ejecucion ha empezado", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(context, StartedService.class);
        context.startService(intent);
    }
}