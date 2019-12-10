package com.sempreahoras.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventDetailsActivity extends AppCompatActivity {
    final static String EVENT_ID = "event_id";
    EventRepository eventRepo;

    long eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("View event");

        eventRepo = new EventRepository(getApplication());

        long eventId = getIntent().getLongExtra(EVENT_ID, -1);
        if(eventId < 0) {
            throw new IllegalArgumentException("Invalid event ID passed in");
        }
        this.eventId = eventId;

        updateUi();
    }

    void updateUi() {
        Event e = eventRepo.getEventById(eventId);

        if(e != null) {
            TextView title = findViewById(R.id.title);
            title.setText(e.title);

            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

            TextView time = findViewById(R.id.time);
            Date startDate = new Date(e.startMillis);
            Date endDate   = new Date(e.endMillis);
            time.setText(dateFormat.format(startDate) + " " + timeFormat.format(startDate) + " - " +
                         dateFormat.format(endDate)   + " " + timeFormat.format(endDate));

            TextView location = findViewById(R.id.location);
            if(e.location == null) {
                location.setVisibility(View.GONE);
            }
            else {
                location.setText(e.location);
            }

            TextView description = findViewById(R.id.desc);
            description.setText(e.description != null ? e.description : "");

            LinearLayout topBanner = findViewById(R.id.top_banner);
            topBanner.setBackgroundColor(e.color);
        }
        else {
            // TODO: Go back?
        }
    }
}
