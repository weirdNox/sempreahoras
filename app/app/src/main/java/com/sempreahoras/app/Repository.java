package com.sempreahoras.app;

import android.content.Context;

import androidx.room.Dao;

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
        return eventDao.getEventsBetweenMillis(startMillis,endMillis);
    }


    Event getEventById(long id) {
        return eventDao.getEventById(id);
    }

    void insertEvent(Event e) {
        if(e.deleted) {
            deleteEvent(e.id);
        }
        else {
            e.ensureConsistency();
            eventDao.insert(e);
        }
    }

    void deleteEvent(long id) {
        eventDao.deleteEvent(id);
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
        return taskDao.getTasks();
    }

    Task getTaskById(long id) {
        return taskDao.getTaskById(id);
    }

    void deleteTask(long id) {
        taskDao.deleteTask(id);
    }
}
