package com.sempreahoras.app;

import android.app.Application;

import java.util.List;

public class EventRepository {
    private EventDao eventDao;

    EventRepository(Application application) {
        EventDatabase db = EventDatabase.getDatabase(application);
        eventDao = db.eventDao();
    }

    List<Event> getEventsBetweenMillis(long startMillis, long endMillis) {
        return eventDao.getEventsBetweenMillis(startMillis,endMillis);
    }


    Event getEventById(long id) {
        return eventDao.getEventById(id);
    }

    void insert(Event e) {
        e.calculateEnd();
        EventDatabase.databaseWriteExecutor.execute(() -> eventDao.insert(e));
    }
}
