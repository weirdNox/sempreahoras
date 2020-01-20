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

import com.google.android.gms.common.util.NumberUtils;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventEditorActivity extends AppCompatActivity {
    final static String EVENT_ID = "event_id";
    Repository repo;
    Event e;

    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    Button startDate;
    Button startTime;
    Button endDate;
    Button endTime;

    private ServerSyncer syncer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_editor);

        if(!ServerSyncer.isNetworkAvailable(this)) {
            Toast.makeText(this, "You need to be connected to the Internet for editing events!", Toast.LENGTH_LONG).show();
        }

        syncer = new ServerSyncer(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit event");

        repo = new Repository(getApplication());

        long eventId = getIntent().getLongExtra(EVENT_ID, -1);
        if(eventId < 0) {
            e = new Event();
        }
        else {
            e = repo.getEventById(eventId);

            if(e == null) {
                throw new IllegalArgumentException("Non existing ID passed in.");
            }
        }

        Button save = findViewById(R.id.save_button);
        LinearLayout topBanner = findViewById(R.id.top_banner);
        EditText title = findViewById(R.id.title);
        startDate = findViewById(R.id.startDate);
        startTime = findViewById(R.id.startTime);
        endDate   = findViewById(R.id.endDate);
        endTime   = findViewById(R.id.endTime);
        Spinner repeat = findViewById(R.id.repeat);
        EditText repeatCount = findViewById(R.id.repeatCount);
        EditText location = findViewById(R.id.location);
        EditText desc = findViewById(R.id.desc);
        CheckBox allDay = findViewById(R.id.allDay);
        EditText notifMinutes = findViewById(R.id.notifMinutes);

        save.setOnClickListener(v -> {
            e.title = title.getText().toString();

            String repeatCountStr = repeatCount.getText().toString();
            e.repeatCount = repeatCountStr.isEmpty() ? 0 : Integer.parseInt(repeatCountStr);

            String repeatOption = repeat.getSelectedItem().toString();
            if(repeatOption.equals("Weekly")) {
                e.repeatType = Event.repeatWeekly;
            }
            else if(repeatOption.equals("Monthly")) {
                e.repeatType = Event.repeatMonthly;
            }
            else if(repeatOption.equals("Yearly")) {
                e.repeatType = Event.repeatYearly;
            }
            else {
                e.repeatType = Event.repeatNone;
                e.repeatCount = 0;
            }

            try {
                e.notifMinutes = Long.parseLong(notifMinutes.getText().toString());
            }
            catch(Exception ex) {
                e.notifMinutes = -1;
            }
            e.location = location.getText().toString();
            e.description = desc.getText().toString();

            syncer.sendNewEvent(e, (event, err) -> {
                if(event != null) {
                    Toast.makeText(this, "Event successfully saved.", Toast.LENGTH_LONG).show();
                    repo.insertEvent(event);
                    finish();
                }
                else {
                    Toast.makeText(this, "Could not edit event: " + (err == null ? "Unknown error" : err), Toast.LENGTH_LONG).show();
                }
            });
        });

        final ColorPicker cp = new ColorPicker(this, Color.red(e.color), Color.green(e.color), Color.blue(e.color));
        cp.enableAutoClose();
        cp.setCallback(color -> {
            e.color = color;
            topBanner.setBackgroundColor(e.color);
        });

        topBanner.setOnClickListener(v -> cp.show());
        topBanner.setBackgroundColor(e.color);

        title.setText(e.title);
        title.setSingleLine();
        title.setHorizontallyScrolling(false);
        title.setMaxLines(5);
        title.setImeOptions(EditorInfo.IME_ACTION_DONE);

        startDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(e.startMillis);
            DatePickerDialog picker = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, monthOfYear);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                e.startMillis = c.getTimeInMillis();
                updateUi();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            picker.setTitle("Select start date");
            picker.show();
        });

        startTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(e.startMillis);
            TimePickerDialog picker = new TimePickerDialog(this, (p, h, m) -> {
                c.set(Calendar.HOUR_OF_DAY, h);
                c.set(Calendar.MINUTE, m);
                e.startMillis = c.getTimeInMillis();
                updateUi();
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
            picker.setTitle("Select start time");
            picker.show();
        });

        endDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(e.startMillis + e.durationMillis);
            DatePickerDialog picker = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, monthOfYear);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                long millis = c.getTimeInMillis();
                if(e.startMillis >= millis) {
                    millis = e.startMillis + 60*60*1000;
                    Toast t = Toast.makeText(this, "Finish date is before start date!\nSetting duration to 1 hour.", Toast.LENGTH_LONG);
                    t.show();
                }
                e.durationMillis = millis - e.startMillis;
                updateUi();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            picker.setTitle("Select end date");
            picker.show();
        });

        endTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(e.startMillis + e.durationMillis);
            TimePickerDialog picker = new TimePickerDialog(this, (p, h, m) -> {
                c.set(Calendar.HOUR_OF_DAY, h);
                c.set(Calendar.MINUTE, m);

                long millis = c.getTimeInMillis();
                if(e.startMillis >= millis) {
                    millis = e.startMillis + 60*60*1000;
                    Toast t = Toast.makeText(this, "Finish time is before start date!\nSetting duration to 1 hour.", Toast.LENGTH_LONG);
                    t.show();
                }
                e.durationMillis = millis - e.startMillis;
                updateUi();
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
            picker.setTitle("Select end time");
            picker.show();
        });

        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("None");
        spinnerArray.add("Weekly");
        spinnerArray.add("Monthly");
        spinnerArray.add("Yearly");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeat.setAdapter(adapter);
        repeat.setSelection(e.repeatType);
        if(e.repeatCount > 0) {
            repeatCount.setText(""+e.repeatCount);
        }

        allDay.setChecked(e.isAllDay);
        allDay.setOnClickListener(v -> {
            e.isAllDay = !e.isAllDay;
            updateUi();
        });

        if(e.notifMinutes >= 0) {
            notifMinutes.setText(String.valueOf(e.notifMinutes));
        }
        location.setText(e.location);
        desc.setText(e.description);

        updateUi();
    }

    /**
     * Update UI by updating times
     */
    void updateUi() {
        Date start = new Date(e.startMillis);
        Date end   = new Date(e.startMillis + e.durationMillis);
        startDate.setText(dateFormat.format(start));
        startTime.setText(timeFormat.format(start));
        endDate.setText(dateFormat.format(end));
        endTime.setText(timeFormat.format(end));

        if(e.isAllDay) {
            startTime.setVisibility(View.GONE);
            endTime.setVisibility(View.GONE);
        }
        else {
            startTime.setVisibility(View.VISIBLE);
            endTime.setVisibility(View.VISIBLE);
        }
    }
}
