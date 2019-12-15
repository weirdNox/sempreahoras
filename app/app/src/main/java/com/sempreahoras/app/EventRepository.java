package com.sempreahoras.app;

import android.app.Application;
import android.content.Context;

import java.util.List;

public class EventRepository {
    private EventDao eventDao;

    EventRepository(Context context) {
        EventDatabase db = EventDatabase.getDatabase(context);
        eventDao = db.eventDao();
    }

    List<Event> getEventsBetweenMillis(long startMillis, long endMillis) {
        return eventDao.getEventsBetweenMillis(startMillis,endMillis);
    }


    Event getEventById(long id) {
        return eventDao.getEventById(id);
    }

    void insert(Event e) {
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
}
