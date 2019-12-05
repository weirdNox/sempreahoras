package com.sempreahoras.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.DatePicker;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.navigation.NavigationView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String GOOGLE_ACC = "google_account";

    private final int DAY = 0;
    private final int WEEK = 1;
    private final int MONTH = 2;
    private final int TASKS = 3;
    private final int TIMER = 4;
    private final int STOPWATCH = 5;

    private static final long millisPerDay = 86400000L;

    DateFormat dateFormat = DateFormat.getDateInstance();
    Calendar currentDate = Calendar.getInstance();

    SparseArray<ArrayList<Event>> events = new SparseArray<>();

    String userId = "testId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        SubMenu sub = menu.addSubMenu("View type");
        sub.add(0, DAY, Menu.NONE, "Day");
        sub.add(0, WEEK, Menu.NONE, "Week");
        sub.add(0, MONTH, Menu.NONE, "Month");

        menu.add(0, TASKS, Menu.NONE, "Tasks");
        menu.add(0, TIMER, Menu.NONE, "Timer");
        menu.add(0, STOPWATCH, Menu.NONE, "Stopwatch");

        GoogleSignInAccount account = getIntent().getParcelableExtra(GOOGLE_ACC);
        if(account != null) {
            userId = account.getId();
        }


        // TODO(nox): Remove dummy data
        ArrayList<Event> eventsForToday = new ArrayList<>();
        eventsForToday.add(new Event("Olá", 2019, 12, 5, 0, 0, 0, 2019, 12, 5, 1, 30, 0));
        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 7, 0, 0, 2019, 12, 5, 8, 30, 0));
        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 7, 30, 0, 2019, 12, 5, 8, 45, 0));
        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 30, 0, 2019, 12, 5, 9, 0, 0));
        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0, 2019, 12, 5, 9, 0, 0));
        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0, 2019, 12, 5, 9, 0, 0));
        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0, 2019, 12, 5, 9, 0, 0));
        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0, 2019, 12, 5, 9, 0, 0));
        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0, 2019, 12, 5, 9, 0, 0));
        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0, 2019, 12, 5, 9, 0, 0));
        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0, 2019, 12, 5, 9, 0, 0));
        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0, 2019, 12, 5, 9, 0, 0));
        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0, 2019, 12, 5, 9, 0, 0));
        eventsForToday.add(new Event("Comer pão", 2019, 12, 5, 8, 40, 0, 2019, 12, 5, 9, 0, 0));
        eventsForToday.get(eventsForToday.size()-1).color = Color.rgb(99, 235, 164);
        eventsForToday.add(new Event("Adeus", 2019, 12, 5, 21, 0, 0, 2019, 12, 5, 21, 30, 0));
        events.append(getDayNumber(currentDate), eventsForToday);



        getSupportFragmentManager().beginTransaction().replace(R.id.container, new DayFragment()).commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case DAY: {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new DayFragment()).commit();
            } break;

            case WEEK: {
            } break;

            case MONTH: {
            } break;

            case TASKS: {
                // getSupportFragmentManager().beginTransaction().replace(R.id.container, new TasksFragment()).commit();
            } break;

            case TIMER: {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new TimerFragment()).commit();
            } break;

            case STOPWATCH: {
            } break;

            default: {
            } break;
        }

        ((UpdatableUi) getSupportFragmentManager().findFragmentById(R.id.container)).updateUi();

        ((DrawerLayout)findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return true;
    }

    static public int getDayNumber(Calendar cal) {
        return (int)(cal.getTimeInMillis() / millisPerDay);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void changeDate(View v) {
        new DatePickerDialog(this, currentDateChangeListener,
                             currentDate.get(Calendar.YEAR),
                             currentDate.get(Calendar.MONTH),
                             currentDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    DatePickerDialog.OnDateSetListener currentDateChangeListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                currentDate.set(Calendar.YEAR, year);
                currentDate.set(Calendar.MONTH, monthOfYear);
                currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                ((UpdatableUi) getSupportFragmentManager().findFragmentById(R.id.container)).updateUi();
            }
        };
}
