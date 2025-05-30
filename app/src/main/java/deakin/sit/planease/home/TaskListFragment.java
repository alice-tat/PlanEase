package deakin.sit.planease.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

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

import deakin.sit.planease.Constant;
import deakin.sit.planease.R;
import deakin.sit.planease.TaskFormActivity;
import deakin.sit.planease.dto.Task;
import deakin.sit.planease.dto.User;
import deakin.sit.planease.home.adapter.TaskAdapter;

public class TaskListFragment extends Fragment {
    private static final String TAG = "INFO:TaskListFragment";

    ImageButton taskOptionButton, addTaskButton;
    RecyclerView taskListRecyclerView;

    boolean isEditable = false;
    List<Task> currentTaskList;
    TaskAdapter taskAdapter;
    User currentUser;

    ActivityResultLauncher<Intent> activityResultLauncher;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        // Get data
        currentUser = ((HomeActivity) getActivity()).currentUser;
        currentTaskList = new ArrayList<Task>();
        getTaskListFromServer();

        // Setup view
        taskOptionButton = view.findViewById(R.id.taskOptionButton);
        addTaskButton = view.findViewById(R.id.addTaskButton);
        taskListRecyclerView = view.findViewById(R.id.taskListRecyclerView);

        // Config view
        taskOptionButton.setOnClickListener(this::handleTaskOptionButton);
        addTaskButton.setOnClickListener(this::handleAddTaskButton);

        // Config recycler views
        taskAdapter = new TaskAdapter(currentTaskList, this);
        taskListRecyclerView.setAdapter(taskAdapter);
        taskListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Config register launcher
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleResultFromActivity
        );

        return view;
    }

    // Operation handling
    private void loadTaskAndRefresh(List<Task> newTaskList) {
        currentTaskList = newTaskList;
        taskAdapter.updateTaskList(currentTaskList);
    }

    private void handleTaskOptionButton(View view) {
        isEditable = !isEditable;
        taskAdapter.setIsEditable(isEditable);
    }

    private void handleAddTaskButton(View view) {
        Intent intent = new Intent(getActivity(), TaskFormActivity.class);
        intent.putExtra("User", currentUser);
        activityResultLauncher.launch(intent);
    }

    public void handleEditTaskButton (Task task) {
        Intent intent = new Intent(getActivity(), TaskFormActivity.class);
        intent.putExtra("User", currentUser);
        intent.putExtra("Task", task);
        activityResultLauncher.launch(intent);
    }

    private void handleResultFromActivity(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            getTaskListFromServer();
        }
    }

    // Backend interaction
    private void getTaskListFromServer() {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        // Prepare data
        int request_method = Request.Method.GET;
        String url = Constant.BACKEND_URL + Constant.TASK_CRUD_ROUTE + "?user_id=" + currentUser.getId() + "&finish=0" + "&goal_id=";

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
                            ((HomeActivity) getActivity()).showToastMessage("Error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = error.networkResponse != null ? "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data) : error.getMessage() != null ? error.getMessage() : "Unknown error";
                        Log.e(TAG, "Error saving note: " + errorMsg, error);
                        ((HomeActivity) getActivity()).showToastMessage("Error saving note: " + errorMsg);
                    }
                });

        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    public void deleteTaskFromServer(String taskId) {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        // Prepare data
        int request_method = Request.Method.DELETE;
        String url = Constant.BACKEND_URL + Constant.TASK_CRUD_ROUTE + "/" + taskId;

        // Send request
        JsonObjectRequest request = new JsonObjectRequest(request_method, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ((HomeActivity) getActivity()).showToastMessage(response.getString("message"));
                            getTaskListFromServer();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                            ((HomeActivity) getActivity()).showToastMessage("Error parsing response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = error.networkResponse != null ? "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data) : error.getMessage() != null ? error.getMessage() : "Unknown error";
                        ((HomeActivity) getActivity()).showToastMessage(errorMsg);
                        Log.e(TAG, "Error: " + errorMsg, error);
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    public void markTaskAsFinish(String taskId) {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        // Prepare data
        int request_method = Request.Method.PUT;
        String url = Constant.BACKEND_URL + Constant.TASK_FINISH_ROUTE + "/" + taskId;

        // Send request
        JsonObjectRequest request = new JsonObjectRequest(request_method, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ((HomeActivity) getActivity()).showToastMessage(response.getString("message"));
                            getTaskListFromServer();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                            ((HomeActivity) getActivity()).showToastMessage("Error parsing response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = error.networkResponse != null ? "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data) : error.getMessage() != null ? error.getMessage() : "Unknown error";
                        Log.e(TAG, "Error: " + errorMsg, error);
                        ((HomeActivity) getActivity()).showToastMessage("Error: " + errorMsg);
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }
}