package com.sempreahoras.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskItemAdapter extends RecyclerView.Adapter<TaskItemAdapter.TaskViewHolder> {
    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                Task task = tasks.get(position);
                context.viewTask(task);
            });
        }
    }

    private final LayoutInflater inflater;
    private List<Task> tasks;
    private MainActivity context;

    public TaskItemAdapter(MainActivity context) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        if(tasks != null) {
            Task t = tasks.get(position);
            holder.title.setText(t.title);
            holder.title.setBackgroundColor(t.color);
        }
        else {
            holder.title.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return tasks == null ? 0 : tasks.size();
    }

    void setTasks(List<Task> tasks){
        this.tasks = tasks;
        notifyDataSetChanged();
    }
}
