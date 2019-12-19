package com.sempreahoras.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventListFragment extends Fragment implements UpdatableUi {
    private MainActivity a;
    private View v;

    EventItemAdapter eventAdapter;

    public EventListFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        a = ((MainActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_list, container, false);

        a.findViewById(R.id.date_button).setVisibility(View.VISIBLE);
        a.b.show();
        a.b.setOnClickListener(v -> a.createEvent());

        eventAdapter = new EventItemAdapter(a);

        RecyclerView list = v.findViewById(R.id.list);
        list.setAdapter(eventAdapter);
        list.setLayoutManager(new LinearLayoutManager(a));

        updateUi();

        return v;
    }

    @Override
    public void updateUi() {
        Button dateButton = a.findViewById(R.id.date_button);
        dateButton.setText(a.dateFormat.format(a.selectedDate.getTime()));

        List<Event> events = new ArrayList<>();
        long dayStartMillis = a.selectedDate.getTimeInMillis();
        a.repo.getEventsForDay(dayStartMillis, events, events);

        Collections.sort(events, Event::compareTo);
        eventAdapter.setEvents(events);
    }
}
