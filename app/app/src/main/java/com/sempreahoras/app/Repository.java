package com.sempreahoras.app;

import android.content.Context;

import androidx.room.Dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Repository {
    private EventDao eventDao;
    private TaskDao taskDao;

    Repository(Context context) {
        LocalDatabase db = LocalDatabase.getDatabase(context);
        eventDao = db.eventDao();
        taskDao = db.taskDao();
    }

    List<Event> getEventsBetweenMillis(long startMillis, long endMillis) {
        return eventDao.getEventsBetweenMillis(startMillis,endMillis, MainActivity.userId);
    }

    void getEventsForDay(long dayStartMillis, List<Event> eventsForDay, List<Event> allDayEvents) {
        List<Event> events = getEventsBetweenMillis(dayStartMillis, dayStartMillis + (24*60*60*1000));

        for(Event e : events) {
            switch(e.repeatType) {
                case Event.repeatWeekly: {
                    Calendar start = Calendar.getInstance();
                    start.setTimeInMillis(e.startMillis);

                    Calendar day = Calendar.getInstance();
                    day.setTimeInMillis(dayStartMillis);

                    int daysDiff = start.get(Calendar.DAY_OF_WEEK) - day.get(Calendar.DAY_OF_WEEK);
                    if(daysDiff > 0) {
                        daysDiff -= 7;
                    }

                    day.set(Calendar.DAY_OF_YEAR, day.get(Calendar.DAY_OF_YEAR) + daysDiff);
                    day.set(Calendar.HOUR_OF_DAY, start.get(Calendar.HOUR_OF_DAY));
                    day.set(Calendar.MINUTE, start.get(Calendar.MINUTE));
                    day.set(Calendar.SECOND, start.get(Calendar.SECOND));

                    e.startMillis = day.getTimeInMillis();
                    e.endMillis = e.startMillis + e.durationMillis;
                } break;

                case Event.repeatMonthly: {
                    Calendar start = Calendar.getInstance();
                    start.setTimeInMillis(e.startMillis);

                    Calendar day = Calendar.getInstance();
                    day.setTimeInMillis(dayStartMillis);
                    day.set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH));
                    day.set(Calendar.HOUR_OF_DAY, start.get(Calendar.HOUR_OF_DAY));
                    day.set(Calendar.MINUTE, start.get(Calendar.MINUTE));
                    day.set(Calendar.SECOND, start.get(Calendar.SECOND));

                    e.startMillis = day.getTimeInMillis();
                    e.endMillis = e.startMillis + e.durationMillis;
                } break;

                case Event.repeatYearly: {
                    Calendar start = Calendar.getInstance();
                    start.setTimeInMillis(e.startMillis);

                    Calendar day = Calendar.getInstance();
                    day.setTimeInMillis(dayStartMillis);
                    day.set(Calendar.MONTH, start.get(Calendar.MONTH));
                    day.set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH));
                    day.set(Calendar.HOUR_OF_DAY, start.get(Calendar.HOUR_OF_DAY));
                    day.set(Calendar.MINUTE, start.get(Calendar.MINUTE));
                    day.set(Calendar.SECOND, start.get(Calendar.SECOND));

                    e.startMillis = day.getTimeInMillis();
                    e.endMillis = e.startMillis + e.durationMillis;
                } break;
            }

            if(e.startMillis >= dayStartMillis+1000*60*60*24 || e.endMillis < dayStartMillis) {
            }
            else if(e.isAllDay) {
                allDayEvents.add(e);
            }
            else {
                eventsForDay.add(e);
            }
        }
    }

    boolean hasEvents(long dayStartMillis) {
        List<Event> e = new ArrayList<>();
        List<Event> ed = new ArrayList<>();
        getEventsForDay(dayStartMillis, e, ed);
        return e.size() > 0 || ed.size() > 0;
    }

    List<Event> getEventsForNotif() {
        return eventDao.getEventsForNotif(Calendar.getInstance().getTimeInMillis());
    }

    Event getEventById(long id) {
        return eventDao.getEventById(id, MainActivity.userId);
    }

    void insertEvent(Event e) {
        if(e.deleted) {
            deleteEvent(e.id);
        }
        else {
            e.ensureConsistency();
            eventDao.insert(e);
            e.schedule(App.context, Calendar.getInstance().getTimeInMillis());
        }
    }

    void deleteEvent(long id) {
        eventDao.deleteEvent(id, MainActivity.userId);
    }

    void insertTask(Task t) {
        if(t.deleted) {
            deleteTask(t.id);
        }
        else {
            taskDao.insert(t);
        }
    }

    List<Task> getTasks() {
        return taskDao.getTasks(MainActivity.userId);
    }

    Task getTaskById(long id) {
        return taskDao.getTaskById(id, MainActivity.userId);
    }

    void deleteTask(long id) {
        taskDao.deleteTask(id, MainActivity.userId);
    }
}
