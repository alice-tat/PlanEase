package deakin.sit.planease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONObject;

import deakin.sit.planease.dto.Goal;
import deakin.sit.planease.dto.User;

public class GoalFormActivity extends AppCompatActivity {
    private static final String TAG = "INFO:GoalFormActivity";
    EditText inputName, inputDate;
    Button cancelButton, saveGoalButton;

    User currentUser;

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

        // Setup view
        inputName = findViewById(R.id.inputName);
        inputDate = findViewById(R.id.inputDate);
        cancelButton = findViewById(R.id.cancelButton);
        saveGoalButton = findViewById(R.id.saveGoalButton);

        // Config button
        cancelButton.setOnClickListener(this::handleCancelButton);
        saveGoalButton.setOnClickListener(this::handleSaveGoalButton);
    }

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

    private void resetInputFields() {
        inputName.setText("");
        inputDate.setText("");
    }

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
}