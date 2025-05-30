package deakin.sit.planease.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import java.util.Arrays;
import java.util.List;

import deakin.sit.planease.Constant;
import deakin.sit.planease.R;
import deakin.sit.planease.TaskFormActivity;
import deakin.sit.planease.dto.Goal;
import deakin.sit.planease.dto.Message;
import deakin.sit.planease.dto.Task;
import deakin.sit.planease.dto.User;
import deakin.sit.planease.home.adapter.MessageAdapter;

public class AIChatFragment extends Fragment {
    private static final String TAG = "INFO:AIChatFragment";

    EditText inputGoal, targetDate, chatInputBox;
    Button sendButton;
    ProgressBar progressBar;

    RecyclerView recyclerView;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ai_chat, container, false);

        // Get data
        currentUser = ((HomeActivity) getActivity()).currentUser;
        messageList = ((HomeActivity) getActivity()).currentAINessageList;

        // Init adapter
        messageAdapter = new MessageAdapter(messageList, this);

        // Setup view
        inputGoal = view.findViewById(R.id.inputGoal);
        targetDate = view.findViewById(R.id.targetDate);
        chatInputBox = view.findViewById(R.id.chatInputBox);
        sendButton = view.findViewById(R.id.sendButton);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recyclerView);

        // Config recycler view
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Config button
        sendButton.setOnClickListener(this::handleSendButton);

        return view;
    }

    // Operation handling
    private void resetInputFields() {
        inputGoal.setText("");
        targetDate.setText("");
        chatInputBox.setText("");
    }

    public void changeMessage(Message message) {
        inputGoal.setText(message.getSelectedGoalName());
        targetDate.setText(message.getSelectedGoalDate());
        chatInputBox.setText(message.getContent());
    }

    private void handleSendButton(View view) {
        String goalName = inputGoal.getText().toString();
        String goalDate = targetDate.getText().toString();
        String message = chatInputBox.getText().toString().trim();

        if (goalName.isEmpty() || goalDate.isEmpty() || message.isEmpty()) {
            Toast.makeText(getContext(), "Please enter all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        messageAdapter.addNewMessage(new Message(message, currentUser.getName(), false, goalName, goalDate));

        progressBar.setVisibility(View.VISIBLE);
        resetInputFields();
        closeKeyboard();

        sendMessageToServer(goalName, goalDate, message);
    }

    private void closeKeyboard()
    {
        View view = getActivity().getCurrentFocus();

        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Backend interaction
    public void generateTaskBasedOnMessage(Message message) {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        // Prepare data
        int request_method = Request.Method.POST;
        String url = Constant.BACKEND_URL + Constant.SAVE_GENERATED_TASK_ROUTE;

        JSONArray taskJSONArray = message.getGeneratedTaskArray();
        try {
            for (int i = 0; i < taskJSONArray.length(); i++) {
                JSONObject taskJSON = taskJSONArray.getJSONObject(i);
                taskJSON.put("user_id", currentUser.getId());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating task JSON array: " + e.getMessage(), e);
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", currentUser.getId());
            jsonBody.put("goal_name", message.getSelectedGoalName());
            jsonBody.put("goal_date", message.getSelectedGoalDate());
            jsonBody.put("task_list", taskJSONArray);
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON body: " + e.getMessage(), e);
            return;
        }

        // Send request
        JsonObjectRequest request = new JsonObjectRequest(request_method, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Send result
                            ((HomeActivity) getActivity()).showToastMessage(response.getString("message"));
                            ((HomeActivity) getActivity()).bottomNavigationView.setSelectedItemId(HomeActivity.ID_MENU_NATIVATION_TASK_LIST);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                            ((HomeActivity) getActivity()).showToastMessage("Error when processing response: " + e.getMessage());
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

    private void sendMessageToServer(String goal, String date, String message) {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        // Prepare data
        int request_method = Request.Method.GET;
        String url = Constant.BACKEND_URL + Constant.AI_ASSISTANT_ROUTE + "?goal=" + goal + "&end=" + date + "&requirement=" + message;

        // Send request
        JsonObjectRequest request = new JsonObjectRequest(request_method, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            progressBar.setVisibility(View.GONE);

                            // Get result
                            Message aiMessage = new Message(
                                    response.getString("message"),
                                    "AI",
                                    true,
                                    goal,
                                    date,
                                    response.getJSONArray("task")
                            );
                            messageAdapter.addNewMessage(aiMessage);
                        } catch (Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                            ((HomeActivity) getActivity()).showToastMessage("Error when processing response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        String errorMsg = error.networkResponse != null ? "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data) : error.getMessage() != null ? error.getMessage() : "Unknown error";
                        Log.e(TAG, "Error: " + errorMsg, error);
                        ((HomeActivity) getActivity()).showToastMessage("Error: " + errorMsg);
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }
}