package com.optic.fireapp.common;

import android.location.Location;

import com.optic.fireapp.FCM.FCMClient;
import com.optic.fireapp.FCM.IFCMService;
import com.optic.fireapp.retrofit.IGoogleApi;
import com.optic.fireapp.retrofit.RetrofitClient;


public class Common {

    public static String currentToken = "";
    public static Location mLastLocation = null;

    // API QUE DEVUELVE EL CALCULO DEL TIEMPO DEL RECORRIDO DE LA CARRERA
    public static final String baseUrl = "http://maps.googleapis.com";
    public static IGoogleApi getGoogleAPI(){
      return RetrofitClient.getClient(baseUrl).create(IGoogleApi.class);
    }

    // API PARA REALIZAR NOTIFICACIONES FIREBASE
    public static final String fcmUrl = "https://fcm.googleapis.com/";
    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmUrl).create(IFCMService.class);
    }
}
