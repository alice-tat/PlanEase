package deakin.sit.planease.dto;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class Goal implements Serializable {
    private String id;
    private String userId;
    private String name;
    private String date;
    private boolean finish;
    private List<String> taskIds;

    public Goal(String id, String userId, String name, String date, boolean finish, List<String> taskIds) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.date = date;
        this.finish = finish;
        this.taskIds = taskIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public List<String> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;
    }

    @NonNull
    @Override
    public String toString() {
        return getName();
    }
}
