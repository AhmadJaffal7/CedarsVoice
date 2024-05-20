package com.example.cedarsvoice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddCandidateActivity extends AppCompatActivity {

    private EditText editTextName, editTextLastName, editTextAge;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_candidate);

        editTextName = findViewById(R.id.editTextName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextAge = findViewById(R.id.editTextAge);

        requestQueue = Volley.newRequestQueue(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Disable title
        getSupportActionBar().setTitle("");

        // Set navigation icon click listener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the Admin Activity
                Intent intent = new Intent(AddCandidateActivity.this, AdminActivity.class);
                startActivity(intent);
                finish(); // Optional: close current activity
            }
        });
    }

    public void addCandidate(View view) {
        final String name = editTextName.getText().toString();
        final String lastName = editTextLastName.getText().toString();
        final String ageString = editTextAge.getText().toString();

        if (name.isEmpty() || lastName.isEmpty() || ageString.isEmpty()) {
            Toast.makeText(AddCandidateActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        // Check if age is a valid number
        try {
            int age = Integer.parseInt(ageString);
            if (age < 21) {
                Toast.makeText(AddCandidateActivity.this, "Age must be at least 21", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(AddCandidateActivity.this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate name and lastName to prevent SQL injection
        if (!name.matches("[a-zA-Z ]+") || !lastName.matches("[a-zA-Z ]+")) {
            Toast.makeText(getApplicationContext(), "Invalid input. Only alphabetic characters and spaces are allowed for name and last name", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getString(R.string.server)+"add_candidate.php";
        // Show the ProgressBar
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                StringRequest request = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(AddCandidateActivity.this, "Candidate added successfully", Toast.LENGTH_SHORT).show();
                                editTextName.setText("");
                                editTextLastName.setText("");
                                editTextAge.setText("");
                                progressBar.setVisibility(View.GONE);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = null;
                        if (error instanceof NetworkError || error instanceof NoConnectionError || error instanceof AuthFailureError) {
                            message = getString(R.string.cannot_connect);
                        } else if (error instanceof ServerError) {
                            message = getString(R.string.server_not_found);
                        } else if (error instanceof ParseError) {
                            message = getString(R.string.parsing_error);
                        } else if (error instanceof TimeoutError) {
                            message = getString(R.string.connection_timeout);
                        }
                        Toast.makeText(AddCandidateActivity.this, message, Toast.LENGTH_SHORT).show();
                        Log.e("AddCandidateActivity", "Error: " + message, error);
                        progressBar.setVisibility(View.GONE);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("first_name", name);
                        params.put("last_name", lastName);
                        params.put("age", ageString);
                        return params;
                    }
                };

                requestQueue.add(request);
            }
        });
    }
}