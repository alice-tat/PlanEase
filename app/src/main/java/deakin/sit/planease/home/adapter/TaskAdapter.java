package deakin.sit.planease.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import deakin.sit.planease.GoalFormActivity;
import deakin.sit.planease.R;
import deakin.sit.planease.dto.Task;
import deakin.sit.planease.home.HomeActivity;
import deakin.sit.planease.home.TaskListFragment;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    List<Task> taskList;
    TaskListFragment fragment;
    GoalFormActivity activity;
    boolean isEditable;

    public TaskAdapter(List<Task> taskList, TaskListFragment fragment) {
        this.taskList = taskList;
        this.fragment = fragment;
        this.activity = null;
        isEditable = false;
    }

    public TaskAdapter(List<Task> taskList, GoalFormActivity activity) {
        this.taskList = taskList;
        this.fragment = null;
        this.activity = activity;
        isEditable = true;
    }

    @NonNull
    @Override
    public TaskAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdapter.ViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.taskName.setText(task.getName());
        holder.taskDate.setText(task.getDate());

        holder.taskEditButton.setOnClickListener(view -> {
            if (fragment!=null) {
                fragment.handleEditTaskButton(task);
            }
            if (activity!=null) {
                activity.handleEditTaskButton(task);
            }
        });
        holder.taskDeleteButton.setOnClickListener(view -> {
            if (fragment!=null) {
                fragment.deleteTaskFromServer(task.getId());
            }
            if (activity!=null) {
                activity.deleteTaskFromServer(task.getId());
            }
        });

        if (isEditable) {
            holder.taskEditButton.setVisibility(View.VISIBLE);
            holder.taskDeleteButton.setVisibility(View.VISIBLE);
            holder.markFinishButton.setVisibility(View.GONE);
        } else {
            holder.taskEditButton.setVisibility(View.GONE);
            holder.taskDeleteButton.setVisibility(View.GONE);
            holder.markFinishButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTaskList(List<Task> newTaskList) {
        this.taskList = newTaskList;
        notifyDataSetChanged();
    }

    public void setIsEditable(boolean isEditable) {
        this.isEditable = isEditable;
        notifyDataSetChanged();
    }

    public void sortData() {
        taskList.sort(new Comparator<Task>() {
            public int compare(Task t1, Task t2) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate t1Date = LocalDate.parse(t1.getDate(), formatter);
                LocalDate t2Date = LocalDate.parse(t2.getDate(), formatter);

                return t1Date.compareTo(t2Date);
            }
        });
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskDate, taskName;
        ImageButton taskEditButton, taskDeleteButton;
        Button markFinishButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            taskDate = itemView.findViewById(R.id.taskDate);
            taskName = itemView.findViewById(R.id.taskName);

            taskEditButton = itemView.findViewById(R.id.taskEditButton);
            taskDeleteButton = itemView.findViewById(R.id.taskDeleteButton);

            markFinishButton = itemView.findViewById(R.id.markFinishButton);
        }
    }
}
