package com.sempreahoras.app;

import android.content.Context;
import android.media.MediaPlayer;

public class AlarmSound {
    private static AlarmSound instance;
    private Context context;
    private MediaPlayer mp;

    public AlarmSound(Context context) {
        this.context = context;
        this.mp = new MediaPlayer();
    }

    public static AlarmSound getInstance(Context context) {
        if (instance == null) {
            instance = new AlarmSound(context);
        }
        return instance;
    }

    static MediaPlayer getPlayer(Context c) {
        AlarmSound as = AlarmSound.getInstance(c);
        if(as.mp == null) {
            as.mp = new MediaPlayer();
        }

        return as.mp;
    }

    static void release(Context c) {
        AlarmSound as = AlarmSound.getInstance(c);
        if(as.mp != null) {
            as.mp.release();
            as.mp = null;
        }
    }
}
