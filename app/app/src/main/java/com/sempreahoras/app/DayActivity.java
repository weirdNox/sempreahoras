package com.sempreahoras.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.text.DateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class DayActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String GOOGLE_ACC = "google_account";

    private final int DAY = 0;
    private final int WEEK = 1;
    private final int MONTH = 2;
    private final int COUNTDOWN = 3;
    private final int TIMER = 4;

    protected DateFormat dateFormat = DateFormat.getDateInstance();
    protected Calendar currentDate = Calendar.getInstance();

    private String userId = "testId";

    private static final long millisInDays = 86400000L;
    private SparseArray<ArrayList<Event>> events = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        GoogleSignInAccount account = getIntent().getParcelableExtra(GOOGLE_ACC);
        if(account != null) {
            userId = account.getId();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        SubMenu sub = menu.addSubMenu("View type");
        sub.add(0, DAY, Menu.NONE, "Day");
        sub.add(0, WEEK, Menu.NONE, "Week");
        sub.add(0, MONTH, Menu.NONE, "Month");

        menu.add(0, TIMER, Menu.NONE, "Timer");
        menu.add(0, COUNTDOWN, Menu.NONE, "Countdown");

        ((EventsView)findViewById(R.id.events)).floatingButton = findViewById(R.id.floatingActionButton);

        ArrayList<Event> eventsForToday = new ArrayList<>();
        eventsForToday.add(new Event("Olá", 2019, 12, 5, 0, 0, 0,
                                            2019, 12, 5, 1, 30, 0));

        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 7, 0, 0,
                                                  2019, 12, 5, 8, 30, 0));

        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 7, 30, 0,
                                                  2019, 12, 5, 8, 45, 0));

        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 30, 0,
                                                  2019, 12, 5, 9, 0, 0));

        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0,
                                                  2019, 12, 5, 9, 0, 0));

        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0,
                                                  2019, 12, 5, 9, 0, 0));

        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0,
                                                  2019, 12, 5, 9, 0, 0));

        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0,
                                                  2019, 12, 5, 9, 0, 0));

        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0,
                                                  2019, 12, 5, 9, 0, 0));

        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0,
                                                  2019, 12, 5, 9, 0, 0));

        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0,
                                                  2019, 12, 5, 9, 0, 0));

        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0,
                                                  2019, 12, 5, 9, 0, 0));

        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0,
                                                  2019, 12, 5, 9, 0, 0));

        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0,
                                                  2019, 12, 5, 9, 0, 0));

        eventsForToday.add(new Event("Adeus", 2019, 12, 5, 21, 0, 0,
                                              2019, 12, 5, 21, 30, 0));

        events.append(getDayNumber(currentDate), eventsForToday);

        updateUi();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    DatePickerDialog.OnDateSetListener currentDateChangeListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        currentDate.set(Calendar.YEAR, year);
        currentDate.set(Calendar.MONTH, monthOfYear);
        currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        updateUi();
        }
    };

    public void changeDate(View v) {
        new DatePickerDialog(this, currentDateChangeListener, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    protected void updateUi() {
        Button dateButton = findViewById(R.id.date_button);
        dateButton.setText(dateFormat.format(currentDate.getTime()));

        ArrayList<Event> events = this.events.get(getDayNumber(currentDate));
        EventsView view = (EventsView)findViewById(R.id.events);
        view.setEvents(events);
        view.setCal(currentDate);

//        Log.d(null, ""+currentDate.get(Calendar.WEEK_OF_YEAR));
//        currentDate.set(Calendar.DAY_OF_WEEK, currentDate.getFirstDayOfWeek());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case DAY: {
                Log.d(null, "Day");
            } break;

            case WEEK: {
                Log.d(null, "Week");
            } break;

            case MONTH: {
                Log.d(null, "Month");
            } break;

            default: {
            } break;
        }

        DrawerLayout d = findViewById(R.id.drawer_layout);
        d.closeDrawer(GravityCompat.START);
        return true;
    }

    public void createEvent(View v) {
        Intent intent = new Intent(this, CrtEdtEvent.class);
        startActivity(intent);
    }

    static public int getDayNumber(Calendar cal) {
        return (int)(cal.getTimeInMillis() / millisInDays);
    }

    public static Event eventToEdit;
    public void editEvent(Event e) {
        eventToEdit = e;
        Intent intent = new Intent(this, CrtEdtEvent.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(null, eventToEdit.title);
        eventToEdit = null;
    }
}
