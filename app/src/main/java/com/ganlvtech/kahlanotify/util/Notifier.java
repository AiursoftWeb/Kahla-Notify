package com.ganlvtech.kahlanotify.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.provider.Settings;

import com.ganlvtech.kahlanotify.ConversationActivity;
import com.ganlvtech.kahlanotify.R;

import java.util.concurrent.atomic.AtomicInteger;

public class Notifier {
    public static final String CHANNEL_ID = "KAHLA_NOTIFY_CHANNEL";
    public static final String CHANNEL_NAME = "Kahla Notify";
    public static final long[] VIBRATION_PATTERN = {0, 250, 250, 250};
    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private Context mContext;
    private NotificationManager mNotificationManager;

    public Notifier(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.CYAN);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(VIBRATION_PATTERN);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI,
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .build());
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
    }

    public void notify(String title, String text, String server, String email, int conversationId) {
        Notification notification;
        Intent intent = new Intent(mContext, ConversationActivity.class);
        intent.putExtra(ConversationActivity.INTENT_EXTRA_NAME_SERVER, server);
        intent.putExtra(ConversationActivity.INTENT_EXTRA_NAME_EMAIL, email);
        intent.putExtra(ConversationActivity.INTENT_EXTRA_NAME_CONVERSATION_ID, conversationId);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(mContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .setStyle(new Notification.BigTextStyle().bigText(text))
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setChannelId(CHANNEL_ID)
                    .setAutoCancel(true)
                    .build();
        } else {
            notification = new Notification.Builder(mContext)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .setStyle(new Notification.BigTextStyle().bigText(text))
                    .setLights(Color.CYAN, 1000, 1000)
                    .setVibrate(VIBRATION_PATTERN)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setAutoCancel(true)
                    .build();
        }
        mNotificationManager.notify(COUNTER.getAndIncrement(), notification);
    }
}
