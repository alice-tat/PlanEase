package deakin.sit.planease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import deakin.sit.planease.dto.User;
import deakin.sit.planease.home.HomeActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "INFO:LoginActivity";

    EditText inputEmail, inputPassword;
    Button loginButton;
    TextView registerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup view
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        loginButton = findViewById(R.id.loginButton);
        registerText = findViewById(R.id.registerText);

        // Config view
        loginButton.setOnClickListener(this::handleLoginButton);
        registerText.setOnClickListener(this::handleRegisterButton);
    }

    // Operation handling
    private void resetInputFields() {
        inputEmail.setText("");
        inputPassword.setText("");
    }

    private void handleLoginButton(View view) {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        getUserFromServer(email, password);
    }

    private void handleRegisterButton(View view) {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    // Activity handling
    private void startHomeActivity(User currentUser) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra("User", currentUser);
        startActivity(intent);
    }

    // Backend interaction
    private void getUserFromServer(String email, String password) {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Prepare data
        int request_method = Request.Method.POST;
        String url = Constant.BACKEND_URL + Constant.USER_LOGIN_ROUTE;
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
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
                            // Get result
                            JSONObject userJSON = response.getJSONObject("user");
                            User currentUser = new User(
                                    userJSON.getString("_id"),
                                    userJSON.getString("name"),
                                    userJSON.getString("email"),
                                    userJSON.getString("password")
                            );

                            // Reset fields and start activity
                            resetInputFields();
                            startHomeActivity(currentUser);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = error.networkResponse != null ? "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data) : error.getMessage() != null ? error.getMessage() : "Unknown error";
                        Log.e(TAG, "Error saving note: " + errorMsg, error);
                        Toast.makeText(MainActivity.this, "Error saving note: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }
}