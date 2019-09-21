package com.optic.fireapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

/*
 * CLASE ESCUCHADORA QUE PERMITE SABER SI EL CONDUCTOR DESACTIVO EL GPS
 */
public class GSPCheck extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);


        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {





        }


    }


}
