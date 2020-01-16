package com.sempreahoras.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class App extends Application {
    static final String CHANNEL1_ID = "sempreahoras_notifs!";
    static final String CHANNEL2_ID = "sempreahoras_notifs2";

    static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel1 = new NotificationChannel(CHANNEL1_ID, "Sempre a Horas", importance);

            NotificationChannel channel2 = new NotificationChannel(CHANNEL2_ID, "Sempre a Horas", importance);
            channel2.setVibrationPattern(new long[] {0,300,300,300,600,300,300,300,300,300,300,300,300,250,50,550,50,200});

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel1);
            notificationManager.createNotificationChannel(channel2);
        }
    }
}
