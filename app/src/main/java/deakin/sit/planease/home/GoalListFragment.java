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
import deakin.sit.planease.GoalFormActivity;
import deakin.sit.planease.R;
import deakin.sit.planease.dto.Goal;
import deakin.sit.planease.dto.User;
import deakin.sit.planease.home.adapter.GoalAdapter;

public class GoalListFragment extends Fragment {
    private static final String TAG = "INFO:HomeActivity-GoalListFragment";
    ImageButton goalOptionButton, addGoalButton;
    RecyclerView goalListRecyclerView;

    GoalAdapter goalAdapter;
    List<Goal> currentGoalList;

    boolean isEditable = false;

    User currentUser;

    ActivityResultLauncher<Intent> activityResultLauncher;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_goal_list, container, false);

        // Get data
        currentUser = ((HomeActivity) getActivity()).currentUser;
        currentGoalList = new ArrayList<Goal>();
        getGoalListFromServer();

        // Setup view
        goalOptionButton = view.findViewById(R.id.goalOptionButton);
        addGoalButton = view.findViewById(R.id.addGoalButton);
        goalListRecyclerView = view.findViewById(R.id.goalListRecyclerView);

        // Config view
        goalOptionButton.setOnClickListener(this::handleGoalOptionButton);
        addGoalButton.setOnClickListener(this::handleAddGoalButton);

        // Config recycler views
        goalAdapter = new GoalAdapter(currentGoalList, this);
        goalListRecyclerView.setAdapter(goalAdapter);
        goalListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Config register launcher
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleResultFromActivity
        );

        return view;
    }

    private void loadGoalAndRefresh(List<Goal> newGoalList) {
        currentGoalList = newGoalList;
        goalAdapter.updateGoalList(currentGoalList);
    }

    private void handleGoalOptionButton(View view) {
        isEditable = !isEditable;
        goalAdapter.setIsEditable(isEditable);
        if (isEditable) {
            goalOptionButton.setVisibility(View.GONE);
            addGoalButton.setVisibility(View.VISIBLE);
        } else {
            goalOptionButton.setVisibility(View.VISIBLE);
            addGoalButton.setVisibility(View.GONE);
        }
    }

    private void handleAddGoalButton(View view) {
        Intent intent = new Intent(getActivity(), GoalFormActivity.class);
        intent.putExtra("User", currentUser);
        activityResultLauncher.launch(intent);
    }

    public void handleEditGoalButton (Goal goal) {
        Intent intent = new Intent(getActivity(), GoalFormActivity.class);
        intent.putExtra("User", currentUser);
        intent.putExtra("Goal", goal);
        activityResultLauncher.launch(intent);
    }

    private void handleResultFromActivity(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            getGoalListFromServer();
        }
    }

    private void getGoalListFromServer() {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        // Prepare data
        int request_method = Request.Method.GET;
        String url = Constant.BACKEND_URL + Constant.GOAL_CRUD_ROUTE + "?user_id=" + currentUser.getId() + "&finish=0";

        // Send request
        JsonObjectRequest request = new JsonObjectRequest(request_method, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i(TAG, "Response: " + response.toString());

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

                            loadGoalAndRefresh(receivedGoalList);
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

    public void deleteGoalFromServer(String goalId) {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        // Prepare data
        int request_method = Request.Method.DELETE;
        String url = Constant.BACKEND_URL + Constant.GOAL_CRUD_ROUTE + "/" + goalId;

        // Send request
        JsonObjectRequest request = new JsonObjectRequest(request_method, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ((HomeActivity) getActivity()).showToastMessage(response.getString("message"));
                            getGoalListFromServer();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
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
}