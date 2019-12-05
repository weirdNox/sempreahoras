package com.sempreahoras.app;

import android.graphics.Color;

import java.util.Calendar;

public class Event {
    int id;

    String title;

    Calendar startDate;
    Calendar endDate;

    String description;

    int color = Color.rgb(232, 98, 88);
    String location;

    public int numColumns;
    public int columnIdx;

    public Event(String title, int startYear, int startMonth, int startDay, int startHour, int startMinute, int startSecond,
                 int endYear, int endMonth, int endDay, int endHour, int endMinute, int endSecond)
    {
        this.title = title;

        startDate = Calendar.getInstance();
        startDate.set(startYear, startMonth, startDay, startHour, startMinute, startSecond);

        endDate = Calendar.getInstance();
        endDate.set(endYear, endMonth, endDay, endHour, endMinute, endSecond);
    }

    int compareTo(Event e) {
        if(this.startDate.getTimeInMillis() < e.startDate.getTimeInMillis()) {
            return -1;
        }
        else if(this.startDate.getTimeInMillis() > e.startDate.getTimeInMillis()) {
            return 1;
        }
        else {
            if(this.endDate.getTimeInMillis() < e.endDate.getTimeInMillis()) {
                return -1;
            }
            else if(this.endDate.getTimeInMillis() > e.endDate.getTimeInMillis()) {
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
