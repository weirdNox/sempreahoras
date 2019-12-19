package com.sempreahoras.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventItemAdapter extends RecyclerView.Adapter<EventItemAdapter.EventViewHolder> {
    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                Event event = events.get(position);
                context.viewEvent(event);
            });
        }
    }

    private final LayoutInflater inflater;
    private List<Event> events;
    private MainActivity context;

    public EventItemAdapter(MainActivity context) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.task_item, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        if(events != null) {
            Event e = events.get(position);
            holder.title.setText(e.title);
            holder.title.setBackgroundColor(e.color);
        }
        else {
            holder.title.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return events == null ? 0 : events.size();
    }

    void setEvents(List<Event> events){
        this.events = events;
        notifyDataSetChanged();
    }
}
