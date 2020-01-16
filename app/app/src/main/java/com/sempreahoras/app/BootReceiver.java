package com.sempreahoras.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.AlarmManagerCompat;

import java.util.Calendar;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent i) {
        Repository repo = new Repository(context);
        List<Event> events = repo.getEventsForNotif();

        long now = Calendar.getInstance().getTimeInMillis();
        for(Event e : events) {
            e.schedule(context, now);
        }
    }
}
