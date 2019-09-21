package com.optic.fireapp.channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.optic.fireapp.R;


public class NotificationHelper extends ContextWrapper{

    private static final String SUBETE_AMIGO_CHANNEL_ID = "com.optic.fireapp";
    private static final String SUBETE_AMIGO_CHANNEL_NAME = "FireAPP";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {

        NotificationChannel notificationChannel = new
                NotificationChannel(
                        SUBETE_AMIGO_CHANNEL_ID,
                        SUBETE_AMIGO_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(Color.GRAY);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager() {

        if(manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        }

        return  manager;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotification(String title, String content, PendingIntent contentIntent, Uri soundUri) {
        return new Notification.Builder(getApplicationContext(), SUBETE_AMIGO_CHANNEL_ID)
                    .setContentText(content)
                    .setContentTitle(title)
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.ic_notification);

    }

}
