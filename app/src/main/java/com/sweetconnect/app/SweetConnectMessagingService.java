package com.sweetconnect.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class SweetConnectMessagingService
        extends FirebaseMessagingService {

    private static final String CHANNEL_ID =
            "sweetconnect_messages";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String title = "SweetConnect 💗";
        String body = "คุณมีข้อความใหม่";

        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null) {
                title = message.getNotification().getTitle();
            }

            if (message.getNotification().getBody() != null) {
                body = message.getNotification().getBody();
            }
        }

        if (message.getData().containsKey("title")) {
            title = message.getData().get("title");
        }

        if (message.getData().containsKey("body")) {
            body = message.getData().get("body");
        }

        createNotificationChannel();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        )
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(body)
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(this).notify(
                (int) System.currentTimeMillis(),
                notification.build()
        );
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        // Token พร้อมใช้งานสำหรับนำไปตั้งค่าระบบส่งแจ้งเตือน
        // ยังไม่ได้ส่ง token ไปยังเซิร์ฟเวอร์
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "ข้อความ SweetConnect",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "แจ้งเตือนเมื่อมีข้อความใหม่"
            );

            NotificationManager manager =
                    (NotificationManager) getSystemService(
                            Context.NOTIFICATION_SERVICE
                    );

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
