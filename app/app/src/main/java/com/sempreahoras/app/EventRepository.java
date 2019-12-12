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
        e.ensureConsistency();
        eventDao.insert(e);
    }

    void deleteEvent(long id) {
        eventDao.deleteEvent(id);
    }
}
