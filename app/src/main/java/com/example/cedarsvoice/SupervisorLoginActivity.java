package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class SupervisorLoginActivity extends AppCompatActivity {
    private EditText editTextSupervisorID;
    private EditText editTextPoliceID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.supervisor_login);

        editTextSupervisorID = findViewById(R.id.editTextSupervisorID);
        editTextPoliceID = findViewById(R.id.editTextPoliceID);
    }

    public void verifyLogin(View view) {
        String supervisorID = editTextSupervisorID.getText().toString().trim();
        String policeID = editTextPoliceID.getText().toString().trim();
        // Validate input fields
        if (!supervisorID.isEmpty() && !policeID.isEmpty()) {
            // Initialize Volley RequestQueue
            RequestQueue queue = Volley.newRequestQueue(this);

            // URL of your backend endpoint to verify IDs
            String url = "http://10.0.2.2/cedarsvoice/supervisor_login.php";

            // Get the ProgressBar and make it visible
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            // Create a StringRequest with POST method
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Hide the ProgressBar
                            progressBar.setVisibility(View.GONE);
                            if (response.equals("success")) {
                                // Police ID and Supervisor ID verified successfully
                                Intent intent = new Intent(SupervisorLoginActivity.this, SupervisorActivity.class);
                                startActivity(intent);
                                Toast.makeText(SupervisorLoginActivity.this, "Logged in as Supervisor", Toast.LENGTH_SHORT).show();
                            } else {
                                // Verification failed
                                Toast.makeText(SupervisorLoginActivity.this, "Incorrect Supervisor ID or Police ID", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Hide the ProgressBar
                            progressBar.setVisibility(View.GONE);

                            if (error instanceof TimeoutError) {
                                // Handle timeout error
                                Toast.makeText(SupervisorLoginActivity.this, "Timeout Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            } else if (error instanceof NoConnectionError) {
                                // Handle no connection error
                                Toast.makeText(SupervisorLoginActivity.this, "No Connection Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            } else if (error instanceof AuthFailureError) {
                                // Handle authentication failure error
                                Toast.makeText(SupervisorLoginActivity.this, "Authentication Failure Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            } else if (error instanceof ServerError) {
                                // Handle server error
                                Toast.makeText(SupervisorLoginActivity.this, "Server Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            } else if (error instanceof NetworkError) {
                                // Handle network error
                                Toast.makeText(SupervisorLoginActivity.this, "Network Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle general error
                                Toast.makeText(SupervisorLoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            // Error occurred during the request
                            if (error.networkResponse != null) {
                                switch (error.networkResponse.statusCode) {
                                    case 400:
                                        // Handle 400 Bad Request error
                                        Toast.makeText(SupervisorLoginActivity.this, "Bad Request", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 401:
                                        // Handle 401 Unauthorized error
                                        Toast.makeText(SupervisorLoginActivity.this, "Unauthorized", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 404:
                                        // Handle 404 Not Found error
                                        Toast.makeText(SupervisorLoginActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 500:
                                        // Handle 500 Server Error
                                        Toast.makeText(SupervisorLoginActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        // Handle general error
                                        Toast.makeText(SupervisorLoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            } else {
                                // Handle general error
                                Toast.makeText(SupervisorLoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }                        }
                    }) {
                // Override getParams() method to pass parameters to POST request
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("supervisorID", supervisorID);
                    params.put("policeID", policeID);
                    return params;
                }
            };

            // Add the request to the RequestQueue
            queue.add(stringRequest);
        } else {
            // Show toast if any field is empty
            Toast.makeText(SupervisorLoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }
}