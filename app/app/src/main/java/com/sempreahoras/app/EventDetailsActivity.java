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

public class EventDetailsActivity extends AppCompatActivity {
    final static int OPTION_EDIT = 1;
    final static int OPTION_DELETE = 2;

    final static int EDIT_EVENT_CODE = 1;
    final static String EVENT_ID = "event_id";
    Repository repo;

    long eventId;

    private ServerSyncer syncer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        syncer = new ServerSyncer(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("View event");

        repo = new Repository(getApplication());

        long eventId = getIntent().getLongExtra(EVENT_ID, -1);
        if(eventId < 0) {
            throw new IllegalArgumentException("Invalid event ID passed in");
        }
        this.eventId = eventId;

        updateUi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, OPTION_EDIT, Menu.NONE, "Edit event");
        menu.add(Menu.NONE, OPTION_DELETE, Menu.NONE, "Delete event");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case OPTION_EDIT: {
                Intent intent = new Intent(this, EventEditorActivity.class);
                intent.putExtra(EventDetailsActivity.EVENT_ID, eventId);
                startActivityForResult(intent, EDIT_EVENT_CODE);
            } break;

            case OPTION_DELETE: {
                syncer.deleteEvent(eventId, MainActivity.userId, (err) -> {
                        if(err == null) {
                            Toast.makeText(this, "Event successfully deleted.", Toast.LENGTH_LONG).show();
                            repo.deleteEvent(eventId);
                            finish();
                        }
                        else {
                            Toast.makeText(this, "Could not delete event: " + (err == null ? "Unknown error" : err), Toast.LENGTH_LONG).show();
                        }
                    });

            } break;

            default: return false;
        }

        return true;
    }

    void updateUi() {
        Event e = repo.getEventById(eventId);

        if(e != null) {
            TextView title = findViewById(R.id.title);
            title.setText(e.title.isEmpty() ? "Event" : e.title);

            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

            TextView time = findViewById(R.id.time);
            Date startDate = new Date(e.startMillis);
            Date endDate   = new Date(e.startMillis + e.durationMillis);
            time.setText(dateFormat.format(startDate) + " " + timeFormat.format(startDate) + " - " +
                         dateFormat.format(endDate)   + " " + timeFormat.format(endDate));

            TextView repeat = findViewById(R.id.repeat);
            switch(e.repeatType) {
                case Event.repeatWeekly: {
                    repeat.setText("Repeats weekly");
                } break;

                case Event.repeatMonthly: {
                    repeat.setText("Repeats monthly");
                } break;

                case Event.repeatYearly: {
                    repeat.setText("Repeats yearly");
                } break;
            }

            if(e.repeatType != Event.repeatNone) {
                repeat.setVisibility(View.VISIBLE);

                if(e.repeatCount != 0) {
                    repeat.setText(repeat.getText().toString() + " for " + e.repeatCount + " times");
                }
            }
            else {
                repeat.setVisibility(View.GONE);
            }

            TextView location = findViewById(R.id.location);
            if(e.location.isEmpty()) {
                location.setVisibility(View.GONE);
            }
            else {
                location.setVisibility(View.VISIBLE);
                location.setText(e.location);
            }

            TextView description = findViewById(R.id.desc);
            description.setText(e.description != null ? e.description : "");

            LinearLayout topBanner = findViewById(R.id.top_banner);
            topBanner.setBackgroundColor(e.color);
        }
        else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_EVENT_CODE) {
            updateUi();
        }
    }
}
