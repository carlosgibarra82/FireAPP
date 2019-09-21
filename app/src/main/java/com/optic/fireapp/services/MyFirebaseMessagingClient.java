package com.optic.fireapp.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.optic.fireapp.R;
import com.optic.fireapp.activities.ChatActivity;
import com.optic.fireapp.channel.NotificationHelper;

import java.util.Map;


public class MyFirebaseMessagingClient extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        if(remoteMessage.getData() != null) {

            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            final String message = data.get("message");

            if (title != null) {
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                Log.d("ENTRO", "CURRENT Activity ::" + cn.getClassName());
                if(title.equals("Nuevo mensaje")) {
                    if (!cn.getClassName().equals("com.optic.fireapp.activities.ChatActivity")) {
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            showNotificationWithIntentAPI26(title, message, intent);
                        }
                        else {
                            showNotificationWithIntent(title, message, intent);
                        }
                    }
                }
                else {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showNotificationAPI26(title, message);
                    }
                    else {
                        showNotification(title, message);
                    }
                }
            }

        }

    }

    /*
     * MOSTRAR NOTIFICACION EN API 26 O SUPERIOR
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationWithIntentAPI26(String title, String body, Intent intent) {

        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotification(title, body, contentIntent, defaultSound);

        notificationHelper.getManager().notify(3, builder.build());

    }

    /*
     * CREAR NOTIFICACION PUSH
     */
    private void showNotificationWithIntent(String title, String body, Intent intent) {

        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager)
                getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(3, builder.build());

    }

    /*
     * MOSTRAR NOTIFICACION EN API 26 O SUPERIOR
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationAPI26(String title, String body) {

        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotification(title, body, contentIntent, defaultSound);

        notificationHelper.getManager().notify(1, builder.build());

    }

    /*
     * CREAR NOTIFICACION PUSH
     */
    private void showNotification(String title, String body) {

        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager)
                getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());

    }

}
