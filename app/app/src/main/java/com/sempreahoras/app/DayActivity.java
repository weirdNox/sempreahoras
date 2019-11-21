package com.sempreahoras.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class DayActivity extends AppCompatActivity {
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
}
