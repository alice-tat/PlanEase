package deakin.sit.planease.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import deakin.sit.planease.R;
import deakin.sit.planease.dto.Goal;
import deakin.sit.planease.dto.Task;
import deakin.sit.planease.home.GoalListFragment;
import deakin.sit.planease.home.TaskListFragment;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {
    List<Goal> goalList;
    GoalListFragment fragment;
    boolean isEditable;

    public GoalAdapter(List<Goal> goalList, GoalListFragment fragment) {
        this.goalList = goalList;
        this.fragment = fragment;
        isEditable = false;
    }

    @NonNull
    @Override
    public GoalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalAdapter.ViewHolder holder, int position) {
        Goal goal = goalList.get(position);

        holder.goalName.setText(goal.getName());
        holder.goalDate.setText(goal.getDate());

        holder.goalEditButton.setOnClickListener(view -> {
            fragment.handleEditGoalButton(goal);
        });
        holder.goalDeleteButton.setOnClickListener(view -> {
            fragment.deleteGoalFromServer(goal.getId());
        });

        if (isEditable) {
            holder.goalEditButton.setVisibility(View.VISIBLE);
            holder.goalDeleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.goalEditButton.setVisibility(View.GONE);
            holder.goalDeleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    public void updateGoalList(List<Goal> newGoalList) {
        this.goalList = newGoalList;
        notifyDataSetChanged();
    }

    public void setIsEditable(boolean isEditable) {
        this.isEditable = isEditable;
        notifyDataSetChanged();
    }

    public void sortData() {
        this.goalList.sort(new Comparator<Goal>() {
            public int compare(Goal t1, Goal t2) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate t1Date = LocalDate.parse(t1.getDate(), formatter);
                LocalDate t2Date = LocalDate.parse(t2.getDate(), formatter);

                return t1Date.compareTo(t2Date);
            }
        });
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView goalDate, goalName;
        ImageButton goalEditButton, goalDeleteButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            goalDate = itemView.findViewById(R.id.goalDate);
            goalName = itemView.findViewById(R.id.goalName);

            goalEditButton = itemView.findViewById(R.id.goalEditButton);
            goalDeleteButton = itemView.findViewById(R.id.goalDeleteButton);
        }
    }
}
