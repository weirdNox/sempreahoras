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

    @Query("SELECT * from event_table WHERE startMillis <= :endMillis AND endMillis >= :startMillis ORDER BY startMillis ASC, endMillis ASC")
    List<Event> getEventsBetweenMillis(long startMillis, long endMillis);

    @Query("SELECT * from event_table WHERE id == :id LIMIT 1")
    Event getEventById(long id);
}