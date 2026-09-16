package com.pafez.flashnote;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class StudyReminderWorker extends Worker {

    private static final String CHANNEL_ID = "study_reminders_channel";
    private static final int NOTIFICATION_ID = 1001;

    public StudyReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        FlashNoteDatabase database = FlashNoteDatabase.getInstance(context);

        long now = System.currentTimeMillis();
        int dueCount = database.cardDao().getTotalDueCardCount(now);

        if (dueCount > 0) {
            sendStudyNotification(context, dueCount);
        }

        return Result.success();
    }

    private void sendStudyNotification(Context context, int dueCount) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Study Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for due flashcards");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        String message = "You have " + dueCount + " flashcards ready for review!";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("FlashNote Review Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
