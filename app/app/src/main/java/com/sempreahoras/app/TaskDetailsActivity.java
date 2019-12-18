package com.sempreahoras.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskDetailsActivity extends AppCompatActivity {
    final static int OPTION_EDIT = 1;
    final static int OPTION_DELETE = 2;

    final static int EDIT_TASK_CODE = 1;
    final static String TASK_ID = "task_id";
    Repository repo;

    long taskId;

    private ServerSyncer syncer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        syncer = new ServerSyncer(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("View task");

        repo = new Repository(getApplication());

        long taskId = getIntent().getLongExtra(TASK_ID, -1);
        if(taskId < 0) {
            throw new IllegalArgumentException("Invalid task ID passed in");
        }
        this.taskId = taskId;

        updateUi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, OPTION_EDIT, Menu.NONE, "Edit task");
        menu.add(Menu.NONE, OPTION_DELETE, Menu.NONE, "Delete task");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case OPTION_EDIT: {
                Intent intent = new Intent(this, TaskEditorActivity.class);
                intent.putExtra(TaskDetailsActivity.TASK_ID, taskId);
                startActivityForResult(intent, EDIT_TASK_CODE);
            } break;

            case OPTION_DELETE: {
                syncer.deleteTask(taskId, MainActivity.userId, (err) -> {
                    if(err == null) {
                        Toast.makeText(this, "Task successfully deleted", Toast.LENGTH_LONG).show();
                        repo.deleteTask(taskId);
                        finish();
                    }
                    else {
                        Toast.makeText(this, "Could not delete task: " + (err == null ? "Unknown error" : err), Toast.LENGTH_LONG).show();
                    }
                });
            } break;

            default: return false;
        }

        return true;
    }

    void updateUi() {
        Task t = repo.getTaskById(taskId);

        if(t != null) {
            TextView title = findViewById(R.id.title);
            title.setText(t.title.isEmpty() ? "Task" : t.title);

            TextView description = findViewById(R.id.desc);
            description.setText(t.description != null ? t.description : "");

            LinearLayout topBanner = findViewById(R.id.top_banner);
            topBanner.setBackgroundColor(t.color);
        }
        else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_TASK_CODE) {
            updateUi();
        }
    }
}
