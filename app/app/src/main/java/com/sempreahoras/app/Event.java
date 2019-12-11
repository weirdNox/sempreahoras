package com.sempreahoras.app;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

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

    // NOTE(nox): endMillis represents the effective end of the event; if the event is recurring, it will
    // be the end of its final repeat
    long startMillis;
    long durationMillis;
    int repeatType = repeatNone;
    int repeatCount = 0;
    long endMillis;

    long lastEdit = Calendar.getInstance().getTimeInMillis();

    int color = Color.rgb(252, 186, 3);

    @NonNull String location = "";

    @Ignore public int numColumns;
    @Ignore public int columnIdx;

    public Event() {
        Calendar date = Calendar.getInstance();
        startMillis = date.getTimeInMillis() + 1000*60*60;
        durationMillis = 1000*60*60;
        calculateEnd();
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

        calculateEnd();

        if(startMillis > endMillis) {
            throw new IllegalArgumentException("Start is after the end!");
        }
    }

    void calculateEnd() {
        if(repeatType == repeatNone) {
            endMillis = startMillis + durationMillis;
        }
        else if(repeatCount == 0) {
            endMillis = 0;
        }
        else if(repeatType == repeatWeekly) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(startMillis);
            c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 7*(repeatCount-1));
            endMillis = c.getTimeInMillis() + durationMillis;
        }
        else if(repeatType == repeatMonthly) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(startMillis);
            c.set(Calendar.MONTH, c.get(Calendar.MONTH) + (repeatCount-1));
            endMillis = c.getTimeInMillis() + durationMillis;
        }
        else {
            assert(repeatType == repeatYearly);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(startMillis);
            c.set(Calendar.YEAR, c.get(Calendar.YEAR) + (repeatCount-1));
            endMillis = c.getTimeInMillis() + durationMillis;
        }
    }

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

    boolean isGreaterThan(Event that) {
        return this.compareTo(that) > 0;
    }

    boolean isLessThan(Event that) {
        return this.compareTo(that) < 0;
    }
}
