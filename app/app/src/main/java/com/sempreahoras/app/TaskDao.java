package com.sempreahoras.app;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Task t);

    @Query("SELECT * from task_table")
    List<Task> getTasks();

    @Query("SELECT * from task_table WHERE id == :id LIMIT 1")
    Task getTaskById(long id);

    @Query("DELETE from task_table WHERE id == :id")
    void deleteTask(long id);
}
