package com.sempreahoras.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskEditorActivity extends AppCompatActivity {
    final static String TASK_ID = "task_id";
    Repository repo;
    Task t;

    private ServerSyncer syncer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);

        if(!ServerSyncer.isNetworkAvailable(this)) {
            Toast.makeText(this, "You need to be connected to the Internet for editing tasks!", Toast.LENGTH_LONG).show();
        }

        syncer = new ServerSyncer(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit task");

        repo = new Repository(getApplication());

        long taskId = getIntent().getLongExtra(TASK_ID, -1);
        if(taskId < 0) {
            t = new Task();
        }
        else {
            t = repo.getTaskById(taskId);

            if(t == null) {
                throw new IllegalArgumentException("Non existing ID passed in.");
            }
        }

        Button save = findViewById(R.id.save_button);
        LinearLayout topBanner = findViewById(R.id.top_banner);
        EditText title = findViewById(R.id.title);
        EditText desc = findViewById(R.id.desc);

        save.setOnClickListener(v -> {
            t.title = title.getText().toString();
            t.description = desc.getText().toString();

            syncer.sendNewTask(t, (task, err) -> {
                if(task != null) {
                    Toast.makeText(this, "Task successfully saved", Toast.LENGTH_LONG).show();
                    repo.insertTask(task);
                    finish();
                }
                else {
                    Toast.makeText(this, "Could not edit task: " + (err == null ? "Unknown error" : err), Toast.LENGTH_LONG).show();
                }
            });
        });

        final ColorPicker cp = new ColorPicker(this, Color.red(t.color), Color.green(t.color), Color.blue(t.color));
        cp.enableAutoClose();
        cp.setCallback(color -> {
            t.color = color;
            topBanner.setBackgroundColor(color);
        });

        topBanner.setOnClickListener(v -> cp.show());
        topBanner.setBackgroundColor(t.color);

        title.setText(t.title);
        title.setSingleLine();
        title.setHorizontallyScrolling(false);
        title.setMaxLines(5);
        title.setImeOptions(EditorInfo.IME_ACTION_DONE);

        desc.setText(t.description);

        updateUi();
    }

    void updateUi() {}
}
