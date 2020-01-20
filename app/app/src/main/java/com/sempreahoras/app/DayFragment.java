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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;

public class DayFragment extends Fragment implements UpdatableUi {
    MainActivity a;
    View v;

    int numDays;

    public DayFragment(int numDays) {
        this.numDays = numDays;
    }

    public DayFragment() {
        this(1);
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
        v = inflater.inflate(R.layout.fragment_day, container, false);

        EventsView eventsView = v.findViewById(R.id.events);
        eventsView.floatingButton = a.b;
        eventsView.frag = this;

        Button dateButton = a.findViewById(R.id.date_button);
        dateButton.setVisibility(View.VISIBLE);
        a.b.show();
        a.b.setOnClickListener(v -> a.createEvent());

        updateUi();

        return v;
    }

    /**
     * Updates the UI, by checking consistency, fetching events for the selected dates,
     * and redrawing the view
     */
    public void updateUi() {
        if(v != null) {
            if(numDays == 7) {
                int diff = a.selectedDate.getFirstDayOfWeek() - a.selectedDate.get(Calendar.DAY_OF_WEEK);
                a.selectedDate.set(Calendar.DAY_OF_MONTH, a.selectedDate.get(Calendar.DAY_OF_MONTH)+diff);
            }

            Button dateButton = a.findViewById(R.id.date_button);

            if(numDays == 1) {
                dateButton.setText(a.dateFormat.format(a.selectedDate.getTime()));
            }
            else {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("MMMM, yyyy");
                dateButton.setText(format.format(a.selectedDate.getTime()));
            }

            long firstDayStartMillis = a.selectedDate.getTimeInMillis();
            List<Event>[] events = new List[numDays];
            List<Event>[] allDayEvents = new List[numDays];
            for(int idx = 0; idx < numDays; ++idx) {
                events[idx] = new ArrayList<>();
                allDayEvents[idx] = new ArrayList<>();

                long dayStartMillis = firstDayStartMillis + idx*(24*60*60*1000);
                a.repo.getEventsForDay(dayStartMillis, events[idx], allDayEvents[idx]);
            }

            EventsView view = v.findViewById(R.id.events);
            view.setEvents(events, allDayEvents, firstDayStartMillis);
        }
    }

    /**
     * Change to previous or next group of days, depending on velocity
     * @param velX if positive, goes to the next; else, go to the previous
     */
    void changeTo(float velX) {
        a.selectedDate.set(Calendar.DAY_OF_YEAR, a.selectedDate.get(Calendar.DAY_OF_YEAR) + (velX > 0 ? numDays : -numDays));
        updateUi();
    }

    /**
     * View a single day
     * @param dayIdx index of the day in the current group to view
     */
    void gotoDay(int dayIdx) {
        numDays = 1;
        a.selectedDate.set(Calendar.DAY_OF_YEAR, a.selectedDate.get(Calendar.DAY_OF_YEAR) + dayIdx);
        updateUi();
    }
}
