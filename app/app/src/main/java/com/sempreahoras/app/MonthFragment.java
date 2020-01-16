package com.sempreahoras.app;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.icu.text.Edits;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class MonthFragment extends Fragment implements UpdatableUi {
    MainActivity a;
    View v;

    int numDays = 30;

    public MonthFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        a = ((MainActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_month, container, false);

        EventsMonthView eventsView = v.findViewById(R.id.events);
        eventsView.floatingButton = a.b;
        eventsView.frag = this;

        Button dateButton = a.findViewById(R.id.date_button);
        dateButton.setVisibility(View.VISIBLE);
        a.b.show();
        a.b.setOnClickListener(v -> a.createEvent());

        updateUi();

        return v;
    }

    public void updateUi() {
        if(v != null) {
            int diff = a.selectedDate.getFirstDayOfWeek() - a.selectedDate.get(Calendar.DAY_OF_WEEK);
            a.selectedDate.set(Calendar.DAY_OF_MONTH, 1);

            Button dateButton = a.findViewById(R.id.date_button);

            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("MMMM, yyyy");
            dateButton.setText(format.format(a.selectedDate.getTime()));

            long firstDayStartMillis = a.selectedDate.getTimeInMillis();

            int numDays = a.selectedDate.getActualMaximum(Calendar.DAY_OF_MONTH);
            boolean[] hasEvents = new boolean[numDays];
            for(long idx = 0; idx < numDays; ++idx) {
                long dayStartMillis = firstDayStartMillis + idx*(24*60*60*1000);
                hasEvents[(int)idx] = a.repo.hasEvents(dayStartMillis);
            }

            EventsMonthView view = v.findViewById(R.id.events);
            view.setEvents(hasEvents, firstDayStartMillis);
        }
    }

    void changeTo(float velX) {
        a.selectedDate.set(Calendar.MONTH, a.selectedDate.get(Calendar.MONTH) + (velX > 0 ? 1 : -1));
        updateUi();
    }

    void viewWeek(int weekIdx) {
        a.selectedDate.setTimeInMillis(a.selectedDate.getTimeInMillis() + 7*24*60*60*1000*(long)weekIdx);
        a.viewWeek();
    }
}
