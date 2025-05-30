package deakin.sit.planease;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import deakin.sit.planease.home.adapter.TaskAdapter;

public class GoalFormActivity extends AppCompatActivity {
    private static final String TAG = "INFO:GoalFormActivity";

    EditText inputName, inputDate;
    Button cancelButton, saveGoalButton;
    RecyclerView taskListRecyclerView;
    LinearLayout taskSectionLayout;

    List<Task> currentTaskList;
    TaskAdapter taskAdapter;
    User currentUser;
    Goal currentGoal;

    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_goal_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get data
        currentUser = (User) getIntent().getSerializableExtra("User");
        currentGoal = (Goal) getIntent().getSerializableExtra("Goal");
        currentTaskList = new ArrayList<Task>();
        getTaskListFromServer();

        // Setup view
        inputName = findViewById(R.id.inputName);
        inputDate = findViewById(R.id.inputDate);
        cancelButton = findViewById(R.id.cancelButton);
        saveGoalButton = findViewById(R.id.saveGoalButton);
        taskListRecyclerView = findViewById(R.id.taskListRecyclerView);
        taskSectionLayout = findViewById(R.id.taskSectionLayout);
        fillCurrentGoalFields();

        // Config button
        cancelButton.setOnClickListener(this::handleCancelButton);
        saveGoalButton.setOnClickListener(this::handleSaveGoalButton);

        // Config recycler views
        taskAdapter = new TaskAdapter(currentTaskList, this);
        taskListRecyclerView.setAdapter(taskAdapter);
        taskListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Config register launcher
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleResultFromActivity
        );
    }

    // Operation handling
    private void handleCancelButton(View view) {
        Intent intent = new Intent().putExtra("Message", "Cancelled");
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void handleSaveGoalButton(View view) {
        String name = inputName.getText().toString();
        String date = inputDate.getText().toString();

        if (name.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        addGoalToServer(currentUser.getId(), name, date);
    }

    public void handleEditTaskButton (Task task) {
        Intent intent = new Intent(this, TaskFormActivity.class);
        intent.putExtra("User", currentUser);
        intent.putExtra("Task", task);
        activityResultLauncher.launch(intent);
    }

    private void handleResultFromActivity(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            getTaskListFromServer();
        }
    }

    private void resetInputFields() {
        inputName.setText("");
        inputDate.setText("");
    }

    // Fill and load data
    private void fillCurrentGoalFields() {
        if (currentGoal==null) {
            taskSectionLayout.setVisibility(View.GONE);
            return;
        }
        taskSectionLayout.setVisibility(View.VISIBLE);
        inputName.setText(currentGoal.getName());
        inputDate.setText(currentGoal.getDate());
    }

    private void loadTaskAndRefresh(List<Task> newTaskList) {
        currentTaskList = newTaskList;
        taskAdapter.updateTaskList(currentTaskList);
    }

    // Backend interaction
    private void addGoalToServer(String userId, String name, String date) {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Prepare data
        int request_method = Request.Method.POST;
        String url = Constant.BACKEND_URL + Constant.GOAL_CRUD_ROUTE;
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
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
                            Toast.makeText(GoalFormActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent().putExtra("Message", "Add new goal successful");
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                            Toast.makeText(GoalFormActivity.this, "Error when processing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = error.networkResponse != null ? "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data) : error.getMessage() != null ? error.getMessage() : "Unknown error";
                        Log.e(TAG, "Error: " + errorMsg, error);
                        Toast.makeText(GoalFormActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    private void getTaskListFromServer() {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Prepare data
        int request_method = Request.Method.GET;
        String url = Constant.BACKEND_URL + Constant.TASK_CRUD_ROUTE + "?user_id=" + currentUser.getId()  + "&finish=0" + "&goal_id=" + (currentGoal!=null ? currentGoal.getId() : "");

        // Send request
        JsonObjectRequest request = new JsonObjectRequest(request_method, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i(TAG, "Response: " + response.toString());

                            JSONArray tasksArray = response.getJSONArray("tasks");

                            List<Task> newTaskList = new ArrayList<Task>();
                            for (int i = 0; i < tasksArray.length(); i++) {
                                JSONObject taskJSON = tasksArray.getJSONObject(i);

                                Task task = new Task(
                                        taskJSON.getString("_id"),
                                        taskJSON.getString("user_id"),
                                        taskJSON.getString("goal_id"),
                                        taskJSON.getString("name"),
                                        taskJSON.getString("date"),
                                        taskJSON.getBoolean("finish")
                                );

                                newTaskList.add(task);
                            }

                            loadTaskAndRefresh(newTaskList);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                            Toast.makeText(GoalFormActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = error.networkResponse != null ? "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data) : error.getMessage() != null ? error.getMessage() : "Unknown error";
                        Log.e(TAG, "Error saving note: " + errorMsg, error);
                        Toast.makeText(GoalFormActivity.this, "Error saving note: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });

        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    public void deleteTaskFromServer(String taskId) {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Prepare data
        int request_method = Request.Method.DELETE;
        String url = Constant.BACKEND_URL + Constant.TASK_CRUD_ROUTE + "/" + taskId;

        // Send request
        JsonObjectRequest request = new JsonObjectRequest(request_method, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Toast.makeText(GoalFormActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            getTaskListFromServer();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = error.networkResponse != null ? "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data) : error.getMessage() != null ? error.getMessage() : "Unknown error";
                        Toast.makeText(GoalFormActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error: " + errorMsg, error);
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }
}