package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class AdminLoginActivity extends AppCompatActivity {
    private EditText userIdInput, passwordInput;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_login);

        userIdInput = findViewById(R.id.editTextID);
        passwordInput = findViewById(R.id.editTextPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    public void login(View view) {
        String adminName = userIdInput.getText().toString();
        String password = passwordInput.getText().toString();

        // Validate user input
        if (!adminName.isEmpty() && !password.isEmpty()) {
            String url = getString(R.string.server)+"admin_login.php";
            RequestQueue queue = Volley.newRequestQueue(this);

            // Show the ProgressBar
            progressBar.setVisibility(View.VISIBLE);

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Hide the ProgressBar
                            progressBar.setVisibility(View.GONE);

                            if (response.trim().equals("success")) {
                                // Login successful
                                Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AdminLoginActivity.this, AdminActivity.class);
                                startActivity(intent);
                            } else {
                                // Login failed
                                Toast.makeText(getApplicationContext(), "Login failed. Incorrect name or password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Hide the ProgressBar
                            progressBar.setVisibility(View.GONE);

                            Toast.makeText(getApplicationContext(), "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                            Log.e("VolleyError",error.toString());
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("admin_name", adminName);
                    params.put("admin_password", password);
                    return params;
                }
            };
            queue.add(request);
        } else {
            Toast.makeText(getApplicationContext(), "Please enter both user ID and password", Toast.LENGTH_SHORT).show();
        }
    }
}