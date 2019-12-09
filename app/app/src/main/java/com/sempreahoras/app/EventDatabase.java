package com.sempreahoras.app;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Event.class}, version = 1, exportSchema = false)
public abstract class EventDatabase extends RoomDatabase {
    public abstract EventDao eventDao();

   static volatile EventDatabase INSTANCE;

   static final int NUMBER_OF_THREADS = 4;
   static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

   static EventDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (EventDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), EventDatabase.class, "db").addCallback(dbCallback).allowMainThreadQueries().build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback dbCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                EventDao dao = INSTANCE.eventDao();

                dao.insert(new Event("Comer pão matinal",    2019, 11, 5,  9, 10, 0, 2019, 11, 5,  9, 30, 0));
                dao.insert(new Event("Comer pão vespertino", 2019, 11, 5, 15, 10, 0, 2019, 11, 5, 15, 30, 0));
                dao.insert(new Event("Comer pão noturno",    2019, 11, 5, 20, 10, 0, 2019, 11, 5, 20, 30, 0));
                dao.insert(new Event("New day party",        2019, 11, 5, 23, 55, 0, 2019, 11, 6,  0, 30, 0));

                dao.insert(new Event("Looong event!",        2019, 11, 9, 10, 0, 0, 2019, 11, 11,  10, 0, 0));
                dao.insert(new Event("Small event",        2019, 11, 10, 10, 0, 0, 2019, 11, 10,  11, 0, 0));
            });
        }
    };
}
