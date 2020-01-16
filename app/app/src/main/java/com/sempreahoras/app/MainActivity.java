package com.sempreahoras.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String GOOGLE_ACC = "google_account";
    public static final String TestId = "testId";

    static String userId = TestId;

    private final int DAY_LIST = -1;
    private final int DAY = 0;
    private final int WEEK = 1;
    private final int MONTH = 2;
    private final int TASKS = 3;
    private final int TIMER = 4;
    private final int STOPWATCH = 5;

    private final int LOGOUT = 1000;

    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL);
    Calendar selectedDate = Calendar.getInstance();
    FloatingActionButton b;

    Repository repo;

    private ServerSyncer syncer;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repo = new Repository(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        SubMenu sub = menu.addSubMenu("Calendar");
        sub.add(0, DAY_LIST, Menu.NONE, "List");
        sub.add(0, DAY, Menu.NONE, "Day");
        sub.add(0, WEEK, Menu.NONE, "Week");
        sub.add(0, MONTH, Menu.NONE, "Month");

        sub = menu.addSubMenu("Utilities");
        sub.add(0, TASKS, Menu.NONE, "Tasks");
        sub.add(0, TIMER, Menu.NONE, "Timer");
        sub.add(0, STOPWATCH, Menu.NONE, "Stopwatch");

        menu.add(0, LOGOUT, Menu.NONE, "Logout");

        GoogleSignInAccount account = getIntent().getParcelableExtra(GOOGLE_ACC);
        if(account != null) {
            userId = account.getId();
        }
        else {
            userId = TestId;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new DayFragment(7)).commit();
        getSupportFragmentManager().executePendingTransactions();

        b = findViewById(R.id.floatingActionButton);

        syncer = new ServerSyncer(this);
        scheduler.scheduleWithFixedDelay(fetchNewData, 0, 3, TimeUnit.MINUTES);

        Button syncNow = findViewById(R.id.syncNow);
        syncNow.setOnClickListener(v -> {
            ((DrawerLayout)findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
            syncer.fetchNewData(error -> {
                if(error == null) {
                    Toast.makeText(this, "Finished syncing.", Toast.LENGTH_SHORT).show();
                    updateUi();
                }
                else {
                    Toast.makeText(this, "Could not fetch data from server: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        updateUi();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case DAY_LIST: {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new EventListFragment()).commit();
            } break;

            case DAY: {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new DayFragment()).commit();
            } break;

            case WEEK: {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new DayFragment(7)).commit();
            } break;

            case MONTH: {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new MonthFragment()).commit();
            } break;

            case TASKS: {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new TasksFragment()).commit();
            } break;

            case TIMER: {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new TimerFragment()).commit();
            } break;

            case STOPWATCH: {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new StopwatchFragment()).commit();
            } break;

            case LOGOUT: {
                Intent data = new Intent();
                setResult(RESULT_OK, data);
                finish();
            } break;

            default: {
            } break;
        }

        ((DrawerLayout)findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        updateUi();

        return true;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void changeDate(View v) {
        new DatePickerDialog(this, selectedDateChangeListener,
                             selectedDate.get(Calendar.YEAR),
                             selectedDate.get(Calendar.MONTH),
                             selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    DatePickerDialog.OnDateSetListener selectedDateChangeListener = (view, year, monthOfYear, dayOfMonth) -> {
        selectedDate.set(Calendar.YEAR, year);
        selectedDate.set(Calendar.MONTH, monthOfYear);
        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        updateUi();
    };

    void updateUi() {
        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);
        selectedDate.set(Calendar.MILLISECOND, 0);

        UpdatableUi ui = (UpdatableUi) getSupportFragmentManager().findFragmentById(R.id.container);
        if(ui != null) {
            ui.updateUi();
        }
    }

    void createEvent() {
        Intent intent = new Intent(this, EventEditorActivity.class);
        startActivity(intent);
    }

    void viewEvent(Event e) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra(EventDetailsActivity.EVENT_ID, e.id);
        startActivity(intent);
    }

    void createTask() {
        Intent intent = new Intent(this, TaskEditorActivity.class);
        startActivity(intent);
    }

    void viewTask(Task t) {
        Intent intent = new Intent(this, TaskDetailsActivity.class);
        intent.putExtra(TaskDetailsActivity.TASK_ID, t.id);
        startActivity(intent);
    }

    void viewWeek() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new DayFragment(7)).commit();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateUi();
    }

    Runnable fetchNewData = () -> {
        syncer.fetchNewData(error -> {
            if(error == null) {
                updateUi();
            }
            else {
                Toast.makeText(this, "Could not fetch data from server: " + error, Toast.LENGTH_LONG).show();
            }
        });
    };
}
