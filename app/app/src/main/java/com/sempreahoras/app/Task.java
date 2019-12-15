package com.sempreahoras.app;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_table")
public class Task {
    @PrimaryKey(autoGenerate = true) @NonNull
    long id;

    @NonNull
    String userId = MainActivity.userId;

    String title = "";
    String description = "";

    int color = Color.rgb(244, 67, 54);

    long lastEdit;

    boolean deleted = false;

    public Task() {
    }
}
