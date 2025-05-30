package deakin.sit.planease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import deakin.sit.planease.dto.Goal;
import deakin.sit.planease.dto.Task;
import deakin.sit.planease.dto.User;

public class TaskFormActivity extends AppCompatActivity {
    private static final String TAG = "INFO:TaskFormActivity";

    EditText inputName, inputDate;
    Spinner selectGoalSpinner;
    Button cancelButton, saveTaskButton;

    List<Goal> goalList;
    ArrayAdapter<Goal> goalListAdapter;
    User currentUser;
    Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get data
        currentUser = (User) getIntent().getSerializableExtra("User");
        currentTask = (Task) getIntent().getSerializableExtra("Task");
        goalList = new ArrayList<Goal>();
        getGoalListFromServer();

        // Setup view
        inputName = findViewById(R.id.inputName);
        inputDate = findViewById(R.id.inputDate);
        selectGoalSpinner = findViewById(R.id.selectGoalSpinner);
        cancelButton = findViewById(R.id.cancelButton);
        saveTaskButton = findViewById(R.id.saveTaskButton);

        // Config button
        cancelButton.setOnClickListener(this::handleCancelButton);
        saveTaskButton.setOnClickListener(this::handleSaveTaskButton);

        // Config Spinner
        goalListAdapter = new ArrayAdapter<Goal>(this, android.R.layout.simple_spinner_item, goalList);
        selectGoalSpinner.setAdapter(goalListAdapter);
    }

    // Operation handling
    private void handleCancelButton(View view) {
        Intent intent = new Intent().putExtra("Message", "Cancelled");
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void handleSaveTaskButton(View view) {
        String name = inputName.getText().toString();
        String date = inputDate.getText().toString();
        Goal selectedGoal = (Goal) selectGoalSpinner.getSelectedItem();

        if (name.isEmpty() || date.isEmpty() || selectedGoal==null) {
            Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        addTaskToServer(currentUser.getId(), selectedGoal.getId(), name, date);
    }

    private void resetInputFields() {
        inputName.setText("");
        inputDate.setText("");
    }

    // Fill and load data
    private void fillCurrentTaskFields() {
        if (currentTask==null) {
            return;
        }
        inputName.setText(currentTask.getName());
        inputDate.setText(currentTask.getDate());
        if (goalList == null) {
            return;
        }
        for (int i=0; i<goalList.size(); i++) {
            Goal goal = goalList.get(i);
            if (goal.getId().equals(currentTask.getGoalId())) {
                selectGoalSpinner.setSelection(i);
                return;
            }
        }
    }

    public void loadGoalAndRefresh(List<Goal> updatedGoalList) {
        goalList = updatedGoalList;
        goalListAdapter = new ArrayAdapter<Goal>(this, android.R.layout.simple_spinner_item, goalList);
        selectGoalSpinner.setAdapter(goalListAdapter);
    }

    // Backend interaction
    private void getGoalListFromServer() {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Prepare data
        int request_method = Request.Method.GET;
        String url = Constant.BACKEND_URL + Constant.GOAL_CRUD_ROUTE + "?user_id=" + currentUser.getId() + "&finish=0";

        // Send request
        JsonObjectRequest request = new JsonObjectRequest(request_method, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Get result
                            JSONArray goalArray = response.getJSONArray("goals");

                            List<Goal> receivedGoalList = new ArrayList<Goal>();
                            for (int i = 0; i < goalArray.length(); i++) {
                                JSONObject goalJSON = goalArray.getJSONObject(i);
                                Goal goal = new Goal(
                                        goalJSON.getString("_id"),
                                        goalJSON.getString("user_id"),
                                        goalJSON.getString("name"),
                                        goalJSON.getString("date"),
                                        goalJSON.getBoolean("finish"),
                                        null
                                );
                                receivedGoalList.add(goal);
                            }

                            // Load data
                            loadGoalAndRefresh(receivedGoalList);
                            fillCurrentTaskFields();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                            Toast.makeText(TaskFormActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = error.networkResponse != null ? "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data) : error.getMessage() != null ? error.getMessage() : "Unknown error";
                        Log.e(TAG, "Error saving note: " + errorMsg, error);
                        Toast.makeText(TaskFormActivity.this, "Error saving note: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    private void addTaskToServer(String userId, String goalId, String name, String date) {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Prepare data
        int request_method = currentTask!=null ? Request.Method.PUT : Request.Method.POST;
        String url = Constant.BACKEND_URL + Constant.TASK_CRUD_ROUTE + (currentTask!=null ? ("/" + currentTask.getId()) : "");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
            jsonBody.put("goal_id", goalId);
            jsonBody.put("name", name);
            jsonBody.put("date", date);
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON: " + e.getMessage(), e);
            return;
        }

        // Send request
        JsonObjectRequest request = new JsonObjectRequest(request_method, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Reset fields and end activity
                            resetInputFields();

                            // Send result
                            Toast.makeText(TaskFormActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent().putExtra("Message", "Success");
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                            Toast.makeText(TaskFormActivity.this, "Error when processing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = error.networkResponse != null ? "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data) : error.getMessage() != null ? error.getMessage() : "Unknown error";
                        Log.e(TAG, "Error: " + errorMsg, error);
                        Toast.makeText(TaskFormActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }
}