package com.luanarabelo.treinodaluana.v12.wear;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.wear.ongoing.OngoingActivity;

import com.luanarabelo.treinodaluana.v12.WorkoutData;

/** Mantém um atalho persistente para o treino atual no mostrador e nos recentes. */
public final class WatchOngoingActivity {
    private static final String CHANNEL_ID = "treino_em_andamento";
    private static final int NOTIFICATION_ID = 1801;

    private WatchOngoingActivity() {}

    public static void show(Context context, int workout, String currentStep) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Treino em andamento", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Atalho para voltar ao exercício atual no relógio");
            manager.createNotificationChannel(channel);
        }

        Intent open = new Intent(context, WatchMainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent touchIntent = PendingIntent.getActivity(
                context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = "Treino " + WorkoutData.LETTERS[workout] + " em andamento";
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_watch_app)
                .setContentTitle(title)
                .setContentText(currentStep)
                .setContentIntent(touchIntent)
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        OngoingActivity ongoing = new OngoingActivity.Builder(
                context, NOTIFICATION_ID, notification)
                .setStaticIcon(R.drawable.ic_watch_app)
                .setTouchIntent(touchIntent)
                .build();
        ongoing.apply(context);
        manager.notify(NOTIFICATION_ID, notification.build());
    }

    public static void stop(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) manager.cancel(NOTIFICATION_ID);
    }
}
