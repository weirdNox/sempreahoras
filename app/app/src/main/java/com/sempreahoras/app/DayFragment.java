package com.sempreahoras.app;

import android.app.DatePickerDialog;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DayFragment extends Fragment implements UpdatableUi {
    MainActivity a;
    View v;

    public DayFragment() {}

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
        v = inflater.inflate(R.layout.fragment_day, container, false);

        Toolbar toolbar = v.findViewById(R.id.toolbar);
        a.setSupportActionBar(toolbar);
        a.getSupportActionBar().setDisplayShowTitleEnabled(false);
        a.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = (DrawerLayout) a.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(a, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ((EventsView)v.findViewById(R.id.events)).floatingButton = v.findViewById(R.id.floatingActionButton);

        updateUi();

        return v;
    }

    public void updateUi() {
        if(v != null) {
            Button dateButton = v.findViewById(R.id.date_button);
            dateButton.setText(a.dateFormat.format(a.selectedDate.getTime()));

            long firstDayStartMillis = a.selectedDate.getTimeInMillis();
            List<Event> events = a.eventRepo.getEventsBetweenMillis(firstDayStartMillis, firstDayStartMillis + 1000 * 60 * 60 * 24);

            EventsView view = v.findViewById(R.id.events);
            view.setEvents(new List[]{events}, firstDayStartMillis);
            view.setCal(a.selectedDate);
        }
    }
}
