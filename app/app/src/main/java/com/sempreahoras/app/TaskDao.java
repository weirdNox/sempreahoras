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

    @Query("SELECT * from task_table WHERE userId == :userId")
    List<Task> getTasks(String userId);

    @Query("SELECT * from task_table WHERE id == :id AND userId == :userId LIMIT 1")
    Task getTaskById(long id, String userId);

    @Query("DELETE from task_table WHERE id == :id AND userId == :userId")
    void deleteTask(long id, String userId);
}
