package br.com.roadmaps.mapapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by sidd on 18/05/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification().getBody() != null) {
            sendNotification(remoteMessage.getNotification().getBody());
        }
    }
    private void sendNotification(String msg) {
        this.mNotificationManager = (NotificationManager) getSystemService("notification");
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
//        MediaPlayer.create(this, R.raw.air_horn).start();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(getNotificationIcon(mBuilder))
                .setTicker("Hearty365")
                .setContentTitle("Menssagem")
                .setContentText(msg)
//                  .setStyle(new NotificationCompat.BigTextStyle().bigText("Menssagem do: " +authMessage))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_STATUS)
                .setContentInfo("Chat")
                .setContentIntent(contentIntent)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .setVibrate(new long[]{400, 400});
        mBuilder.setContentIntent(contentIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "channel";
            CharSequence NAME = "sao joao";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            android.app.NotificationChannel channel = new android.app.NotificationChannel(CHANNEL_ID,NAME,importance);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(CHANNEL_ID);
        }
        this.mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return R.drawable.ic_dialog_close_dark;
        } else {
            return R.drawable.ic_dialog_close_dark;
        }
    }
}
