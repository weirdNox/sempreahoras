package com.sempreahoras.app;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Event.class, Task.class}, version = 1, exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase {
    public abstract EventDao eventDao();
    public abstract TaskDao taskDao();

    static volatile LocalDatabase INSTANCE;

    static LocalDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LocalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), LocalDatabase.class, "db").allowMainThreadQueries().build();
                }
            }
        }
        return INSTANCE;
    }
}
