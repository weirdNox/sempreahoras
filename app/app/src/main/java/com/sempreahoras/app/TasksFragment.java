package com.sempreahoras.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TasksFragment extends Fragment implements UpdatableUi {
    private MainActivity a;
    private View v;

    TaskItemAdapter taskAdapter;

    public TasksFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        a = ((MainActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_list, container, false);

        a.findViewById(R.id.date_button).setVisibility(View.GONE);
        a.b.show();
        a.b.setOnClickListener(v -> a.createTask());

        taskAdapter = new TaskItemAdapter(a);

        RecyclerView list = v.findViewById(R.id.list);
        list.setAdapter(taskAdapter);
        list.setLayoutManager(new LinearLayoutManager(a));

        updateUi();

        return v;
    }

    @Override
    public void updateUi() {
        taskAdapter.setTasks(a.repo.getTasks());
    }
}
