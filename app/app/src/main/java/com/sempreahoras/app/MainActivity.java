package com.sempreahoras.app;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String GOOGLE_ACC = "google_account";
    static String userId = "testId";

    private final int DAY = 0;
    private final int WEEK = 1;
    private final int MONTH = 2;
    private final int TASKS = 3;
    private final int TIMER = 4;
    private final int STOPWATCH = 5;

    private static final long millisPerDay = 86400000L;

    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL);
    Calendar selectedDate = Calendar.getInstance();
    FloatingActionButton b;

    EventRepository eventRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventRepo = new EventRepository(getApplication());

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

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new DayFragment(7)).commit();
        getSupportFragmentManager().executePendingTransactions();

        b = findViewById(R.id.floatingActionButton);
        b.setOnClickListener(v -> createEvent());


        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest getRequest = new StringRequest(Request.Method.GET, "http://192.168.1.213:8080/get?userId=2&since=" + prefs.getLong("lastEdit", 0),
            response -> {
                Log.d("Response", response);
                try {
                    Event[] events = mapper.readValue(response, Event[].class);

                    long newLastEdit = prefs.getLong("lastEdit", 0);
                    for(Event event : events) {
                        if(event.lastEdit > newLastEdit) {
                            newLastEdit = event.lastEdit;
                        }
                    }

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong("lastEdit", newLastEdit);
                    editor.apply();
                }
                catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            },
            error -> {
                Log.d("Error.Response", error.toString());
            });
        queue.add(getRequest);
        // StringRequest postRequest = new StringRequest(Request.Method.POST, "http://192.168.1.213:8080",
        //     response -> {
        //         Log.d("Response", response);
        //     },
        //     error -> {
        //         Log.d("Error.Response", error.toString());
        //     })
        // {
        //     @Override
        //     public byte[] getBody()  {
        //         String result = "{\"type\":1, \"events\":[{\"id\":12},{\"id\":56}]}";
        //         return result.getBytes();
        //     }
        // };
        // queue.add(postRequest);



        updateUi();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case DAY: {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new DayFragment()).commit();
            } break;

            case WEEK: {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new DayFragment(7)).commit();
            } break;

            case MONTH: {
            } break;

            case TASKS: {
            } break;

            case TIMER: {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new TimerFragment()).commit();
            } break;

            case STOPWATCH: {
            } break;

            default: {
            } break;
        }

        ((DrawerLayout)findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        updateUi();

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

        ((UpdatableUi) getSupportFragmentManager().findFragmentById(R.id.container)).updateUi();
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

    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateUi();
    }
}
