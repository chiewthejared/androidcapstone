package com.example.test_v2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.test_v2.medsEquipment.MedsAndEquipmentTrackerPage;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "Medication_Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String eventId = intent.getStringExtra("eventId");

        createNotificationChannel(context);
        sendNotification(context, eventId);
    }

    private void createNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID, "Medication Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sendNotification(Context context, String eventId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MedsAndEquipmentTrackerPage.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_medication)
                .setContentTitle("Medication Reminder")
                .setContentText("It's time to take your medication!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setContentIntent(pendingIntent);

        notificationManager.notify(eventId.hashCode(), builder.build());
    }
}