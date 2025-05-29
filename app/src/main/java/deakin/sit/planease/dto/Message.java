package deakin.sit.planease.dto;

import org.json.JSONArray;
import org.json.JSONObject;

public class Message {
    private String username;
    private String content;
    private boolean isAIgenerated;
    private String selectedGoalName;
    private String selectedGoalDate;
    private JSONArray generatedTaskArray;

    public Message(String content, String username, boolean isAIgenerated) {
        this.content = content;
        this.username = username;
        this.isAIgenerated = isAIgenerated;
    }

    public Message(String content, String username, boolean isAIgenerated, String selectedGoalName, String selectedGoalDate, JSONArray generatedTaskArray) {
        this.content = content;
        this.username = username;
        this.isAIgenerated = isAIgenerated;
        this.selectedGoalName = selectedGoalName;
        this.selectedGoalDate = selectedGoalDate;
        this.generatedTaskArray = generatedTaskArray;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isAIgenerated() {
        return isAIgenerated;
    }

    public void setAIgenerated(boolean AIgenerated) {
        isAIgenerated = AIgenerated;
    }

    public String getSelectedGoalName() {
        return selectedGoalName;
    }

    public void setSelectedGoalName(String selectedGoalName) {
        this.selectedGoalName = selectedGoalName;
    }

    public String getSelectedGoalDate() {
        return selectedGoalDate;
    }

    public void setSelectedGoalDate(String selectedGoalDate) {
        this.selectedGoalDate = selectedGoalDate;
    }

    public JSONArray getGeneratedTaskArray() {
        return generatedTaskArray;
    }

    public void setGeneratedTaskArray(JSONArray generatedTaskArray) {
        this.generatedTaskArray = generatedTaskArray;
    }
}
