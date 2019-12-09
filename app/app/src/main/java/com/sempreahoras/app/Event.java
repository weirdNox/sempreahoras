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
    @PrimaryKey(autoGenerate = true) @NonNull
    int id;

    @NonNull
    String title = "Event";
    String description;

    long startMillis;
    long endMillis;

    int color = Color.rgb(252, 186, 3);

    String location;

    @Ignore public int numColumns;
    @Ignore public int columnIdx;

    public Event() {
        Calendar date = Calendar.getInstance();
        startMillis = date.getTimeInMillis();
        endMillis = startMillis + 1000*60*60;
    }

    public Event(String title, int startYear, int startMonth, int startDay, int startHour, int startMinute, int startSecond,
                 int endYear, int endMonth, int endDay, int endHour, int endMinute, int endSecond)
    {
        this.title = title;

        Calendar date = Calendar.getInstance();

        date.set(startYear, startMonth, startDay, startHour, startMinute, startSecond);
        startMillis = date.getTimeInMillis();

        date.set(endYear, endMonth, endDay, endHour, endMinute, endSecond);
        endMillis = date.getTimeInMillis();

        if(startMillis > endMillis) {
            throw new IllegalArgumentException("Start is after the end!");
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
