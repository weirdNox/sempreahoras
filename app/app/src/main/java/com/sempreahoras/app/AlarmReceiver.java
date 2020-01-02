package com.sempreahoras.app;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;

public class AlarmReceiver extends BroadcastReceiver {
    static final String INTENT_TYPE = "type";

    static final int TYPE_TIMER = 0;
    static final int TYPE_CANCEL = 999;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager p = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock lock = p.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sempreahoras::alarmreceiver");
        lock.acquire(5000);

        int type = intent.getIntExtra(INTENT_TYPE, -1);
        switch(type) {
            case TYPE_TIMER: {
                Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if(alert == null) {
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    if(alert == null) {
                        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    }
                }

                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);

                MediaPlayer mp = AlarmSound.getPlayer(context);
                if(mp.isPlaying()) {
                    mp.stop();
                }

                mp.setAudioAttributes(new AudioAttributes.Builder()
                      .setUsage(AudioAttributes.USAGE_ALARM)
                      .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                      .build());
                try {
                    mp.setDataSource(context, alert);
                    mp.prepare();
                    mp.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent cancelIntent = new Intent(context, AlarmReceiver.class);
                cancelIntent.putExtra(INTENT_TYPE, TYPE_CANCEL);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, App.CHANNEL_ID)
                        .setContentTitle("Timer finished!")
                        .setContentText("The timer has expired.")
                        .setSmallIcon(R.drawable.ic_alarm_black_24dp)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setAutoCancel(true)
                        .setDeleteIntent(pendingIntent);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(12398981, builder.build());
            } break;

            case TYPE_CANCEL: {
                AlarmSound.release(context);
            } break;

            default: {} break;
        }
    }
}
