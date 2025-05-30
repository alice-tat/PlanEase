package deakin.sit.planease.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import deakin.sit.planease.Constant;
import deakin.sit.planease.R;
import deakin.sit.planease.dto.User;

public class AccountFragment extends Fragment {
    private static final String TAG = "INFO:HomeActivity-AccountFragment";

    TextView welcomeUserTextView;
    Button logoutButton, deleteAccountButton;

    User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Get data
        currentUser = ((HomeActivity) getActivity()).currentUser;

        // Setup view
        welcomeUserTextView = view.findViewById(R.id.welcomeUserTextView);
        logoutButton = view.findViewById(R.id.logoutButton);
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);

        // Config view
        welcomeUserTextView.setText(currentUser.getName());
        logoutButton.setOnClickListener(this::handleLogoutButton);
        deleteAccountButton.setOnClickListener(this::handleDeleteAccountButton);

        return view;
    }

    // Operation handling
    private void handleLogoutButton(View view) {
        Intent intent = new Intent().putExtra("Message", "Logout successful");
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    private void handleDeleteAccountButton(View view) {
        deleteUserFromServer(currentUser.getId());
    }

    // Backend interaction
    private void deleteUserFromServer(String userId) {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        // Prepare data
        int request_method = Request.Method.DELETE;
        String url = Constant.BACKEND_URL + Constant.USER_DELETE_ROUTE + "/" + userId;

        // Send request
        JsonObjectRequest request = new JsonObjectRequest(request_method, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ((HomeActivity) getActivity()).showToastMessage(response.getString("message"));

                            // Send result
//                            Intent intent = new Intent().putExtra("Message", "Account deleted");
//                            getActivity().setResult(Activity.RESULT_OK, intent);
                            getActivity().finish();
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