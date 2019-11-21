package com.sempreahoras.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class DayActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String GOOGLE_ACC = "google_account";

    protected DateFormat dateFormat = DateFormat.getDateInstance();
    protected Calendar currentDate = Calendar.getInstance();

    private String userId = "testId";

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
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
           case R.id.menu_hello: {
               FloatingActionButton b = findViewById(R.id.floatingActionButton);
               if(b.isOrWillBeShown()) {
                    b.hide();
               }
               else {
                   b.show();
               }

               break;
            }

            default: {
                return false;
            }
        }

        DrawerLayout d = findViewById(R.id.drawer_layout);
        d.closeDrawer(GravityCompat.START);
        return true;
    }

    public void createEvent(View v) {
        Intent intent = new Intent(this, CrtEdtEvent.class);
        startActivity(intent);
    }
}
