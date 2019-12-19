package com.sempreahoras.app;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Event e);

    @Query("SELECT * from event_table WHERE startMillis < :endMillis AND (endMillis >= :startMillis OR endMillis == 0) AND userId == :userId")
    List<Event> getEventsBetweenMillis(long startMillis, long endMillis, String userId);

    @Query("SELECT * from event_table WHERE id == :id AND userId == :userId LIMIT 1")
    Event getEventById(long id, String userId);

    @Query("DELETE from event_table WHERE id == :id AND userId == :userId")
    void deleteEvent(long id, String userId);
}
