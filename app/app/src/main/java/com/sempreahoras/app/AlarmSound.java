package com.sempreahoras.app;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Singleton class that manages sound interfaces
 */
public class AlarmSound {
    private static AlarmSound instance;
    private Context context;
    private MediaPlayer mp;

    public AlarmSound(Context context) {
        this.context = context;
        this.mp = new MediaPlayer();
    }

    /**
     * Obtain singleton instance of this class
     * @param context app context
     * @return the instance
     */
    public static AlarmSound getInstance(Context context) {
        if (instance == null) {
            instance = new AlarmSound(context);
        }
        return instance;
    }

    /**
     * Get media player
     * @param c app context
     * @return media player
     */
    static MediaPlayer getPlayer(Context c) {
        AlarmSound as = AlarmSound.getInstance(c);
        if(as.mp == null) {
            as.mp = new MediaPlayer();
        }

        return as.mp;
    }

    /**
     * Release media player
     * @param c app context
     */
    static void release(Context c) {
        AlarmSound as = AlarmSound.getInstance(c);
        if(as.mp != null) {
            as.mp.release();
            as.mp = null;
        }
    }
}
