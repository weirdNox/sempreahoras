package com.sempreahoras.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Calendar;

@Entity(tableName = "event_table")
public class Event {
    public static final int repeatNone = 0;
    public static final int repeatWeekly = 1;
    public static final int repeatMonthly = 2;
    public static final int repeatYearly = 3;

    @PrimaryKey(autoGenerate = true) @NonNull
    long id;

    @NonNull
    String userId = MainActivity.userId;

    @NonNull String title = "";
    @NonNull String description = "";

    long startMillis;
    long durationMillis;
    boolean isAllDay = false;
    int repeatType = repeatNone;
    int repeatCount = 0;
    // NOTE(nox): endMillis represents the effective end of the event; if the event is recurring, it will
    // be the end of its final repeat
    long endMillis;

    long notifMinutes = -1;

    long lastEdit = Calendar.getInstance().getTimeInMillis();

    int color = Color.rgb(252, 186, 3);

    @NonNull String location = "";

    boolean deleted = false;

    @JsonIgnore @Ignore public int numColumns;
    @JsonIgnore @Ignore public int columnIdx;

    public Event() {
        Calendar date = Calendar.getInstance();
        startMillis = date.getTimeInMillis() + 1000*60*60;
        durationMillis = 1000*60*60;
        ensureConsistency();
    }

    public Event(String title, int startYear, int startMonth, int startDay, int startHour, int startMinute,
                 int endYear, int endMonth, int endDay, int endHour, int endMinute)
    {
        this.title = title;

        Calendar date = Calendar.getInstance();

        date.set(startYear, startMonth, startDay, startHour, startMinute, 0);
        startMillis = date.getTimeInMillis();

        date.set(endYear, endMonth, endDay, endHour, endMinute, 0);
        durationMillis = date.getTimeInMillis() - startMillis;

        ensureConsistency();

        if(startMillis > endMillis) {
            throw new IllegalArgumentException("Start is after the end!");
        }
    }

    /**
     * Ensures consistency between all member variables
     * Should be called before sending the event to the server and when adding to the local database
     */
    void ensureConsistency() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startMillis);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        startMillis = c.getTimeInMillis();

        if(isAllDay) {
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            startMillis = c.getTimeInMillis();

            Calendar c2 = Calendar.getInstance();
            c2.setTimeInMillis(startMillis + durationMillis);
            c2.set(Calendar.HOUR_OF_DAY, 23);
            c2.set(Calendar.MINUTE, 59);
            c2.set(Calendar.SECOND, 59);
            c2.set(Calendar.MILLISECOND, 999);

            durationMillis = (Math.round(Math.ceil((c2.getTimeInMillis()-startMillis)/((float)24*60*60*1000)) * 24*60*60*1000)) - 1;
        }

        if(repeatType == repeatNone) {
            endMillis = startMillis + durationMillis;
        }
        else if(repeatCount == 0) {
            endMillis = 0;
        }
        else if(repeatType == repeatWeekly) {
            c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 7*(repeatCount-1));

            endMillis = c.getTimeInMillis() + durationMillis;
        }
        else if(repeatType == repeatMonthly) {
            c.set(Calendar.MONTH, c.get(Calendar.MONTH) + (repeatCount-1));

            endMillis = c.getTimeInMillis() + durationMillis;
        }
        else {
            assert(repeatType == repeatYearly);
            c.set(Calendar.YEAR, c.get(Calendar.YEAR) + (repeatCount-1));

            endMillis = c.getTimeInMillis() + durationMillis;
        }
    }

    /**
     * Compares events times
     * @param e other event
     * @return -1 if this event should be first; 0 when tied, and 1 otherwise
     */
    int compareTo(Event e) {
        if(this.startMillis < e.startMillis) {
            return -1;
        }
        else if(this.startMillis > e.startMillis) {
            return 1;
        }
        else {
            if(this.endMillis < e.endMillis) {
                return -1;
            }
            else if(this.endMillis > e.endMillis) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }

    /**
     * Get next time the event starts - useful for repeating events
     * @param now in millis since epoch, establishes when to start searching for next
     * @return the time in millis since epoch
     */
    long getNextStart(long now) {
        switch(repeatType) {
            case Event.repeatWeekly: {
                Calendar start = Calendar.getInstance();
                start.setTimeInMillis(startMillis);

                Calendar day = Calendar.getInstance();
                day.setTimeInMillis(now);

                int daysDiff = start.get(Calendar.DAY_OF_WEEK) - day.get(Calendar.DAY_OF_WEEK);
                if(daysDiff < 0) {
                    daysDiff += 7;
                }

                day.set(Calendar.DAY_OF_YEAR, day.get(Calendar.DAY_OF_YEAR) + daysDiff);
                day.set(Calendar.HOUR_OF_DAY, start.get(Calendar.HOUR_OF_DAY));
                day.set(Calendar.MINUTE, start.get(Calendar.MINUTE));
                day.set(Calendar.SECOND, start.get(Calendar.SECOND));

                return day.getTimeInMillis();
            }

            case Event.repeatMonthly: {
                Calendar start = Calendar.getInstance();
                start.setTimeInMillis(startMillis);

                Calendar day = Calendar.getInstance();
                day.setTimeInMillis(now);
                day.set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH));
                day.set(Calendar.HOUR_OF_DAY, start.get(Calendar.HOUR_OF_DAY));
                day.set(Calendar.MINUTE, start.get(Calendar.MINUTE));
                day.set(Calendar.SECOND, start.get(Calendar.SECOND));

                if(day.getTimeInMillis() < now) {
                    day.set(Calendar.MONTH, day.get(Calendar.MONTH) + 1);
                }

                return day.getTimeInMillis();
            }

            case Event.repeatYearly: {
                Calendar start = Calendar.getInstance();
                start.setTimeInMillis(startMillis);

                Calendar day = Calendar.getInstance();
                day.setTimeInMillis(now);
                day.set(Calendar.MONTH, start.get(Calendar.MONTH));
                day.set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH));
                day.set(Calendar.HOUR_OF_DAY, start.get(Calendar.HOUR_OF_DAY));
                day.set(Calendar.MINUTE, start.get(Calendar.MINUTE));
                day.set(Calendar.SECOND, start.get(Calendar.SECOND));

                if(day.getTimeInMillis() < now) {
                    day.set(Calendar.YEAR, day.get(Calendar.YEAR) + 1);
                }

                return day.getTimeInMillis();
            }

            default: {
                return startMillis;
            }
        }
    }

    /**
     * Schedule event to be notified
     * @param context app context
     * @param now millis since epoch
     */
    void schedule(Context context, long now) {
        if(notifMinutes >= 0) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            long next = getNextStart(now);

            if (next > now && next <= endMillis) {
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra(AlarmReceiver.INTENT_TYPE, AlarmReceiver.TYPE_EVENT);
                intent.putExtra(AlarmReceiver.EVENT_ID, id);
                intent.putExtra(AlarmReceiver.EVENT_TITLE, title);
                intent.putExtra(AlarmReceiver.EVENT_START, next);
                intent.putExtra(AlarmReceiver.EVENT_NOTIFMIN, notifMinutes);
                intent.putExtra(AlarmReceiver.EVENT_NOW, now);
                intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next - notifMinutes * 60 * 1000, pendingIntent);
            }
        }
    }
}
