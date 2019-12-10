package com.sempreahoras.app;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventEditorActivity extends AppCompatActivity {
    final static String EVENT_ID = "event_id";
    EventRepository eventRepo;
    Event e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit event");

        eventRepo = new EventRepository(getApplication());

        long eventId = getIntent().getLongExtra(EVENT_ID, -1);
        if(eventId < 0) {
            e = new Event();
        }
        else {
            e = eventRepo.getEventById(eventId);

            if(e == null) {
                throw new IllegalArgumentException("Non existing ID passed in.");
            }
        }

        final ColorPicker cp = new ColorPicker(this, Color.red(e.color), Color.green(e.color), Color.blue(e.color));
        cp.setCallback(color -> {
            e.color = color;
            updateUi();
        });
        cp.enableAutoClose();

        LinearLayout topBanner = findViewById(R.id.top_banner);
        topBanner.setOnClickListener(v -> cp.show());

        updateUi();
    }

    void updateUi() {
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
}
