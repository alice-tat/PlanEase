package deakin.sit.planease.dto;

import java.io.Serializable;

public class Task implements Serializable {
    private String id;
    private String userId;
    private String goalId;
    private String name;
    private String date;
    private boolean finish;

    public Task(String id, String userId, String goalId, String name, String date, boolean finish) {
        this.id = id;
        this.userId = userId;
        this.goalId = goalId;
        this.name = name;
        this.date = date;
        this.finish = finish;
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

    public String getGoalId() {
        return goalId;
    }

    public void setGoalId(String goalId) {
        this.goalId = goalId;
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
}
