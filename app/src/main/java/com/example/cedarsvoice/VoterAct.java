package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VoterAct extends AppCompatActivity {
    EditText editTextId;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.AuthenticationCallback authenticationCallback;
    private byte[] scannedFingerprintData; // Assuming fingerprint data is scanned

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter);

        editTextId = findViewById(R.id.Nid);

        // Initialize BiometricPrompt
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    // Proceed with fingerprint and database check
                    checkFingerprintAndDatabase();
                }

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    // Handle authentication errors
                    Toast.makeText(VoterAct.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                }
            };
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricPrompt = new BiometricPrompt.Builder(this)
                    .setTitle("Fingerprint Authentication")
                    .setSubtitle("Login using your fingerprint")
                    .setNegativeButton("Cancel", this.getMainExecutor(), (dialogInterface, i) -> {
                        // User canceled fingerprint authentication
                    })
                    .build();
        }
    }

    public void VoterLogin(View view) {
        // Check if fingerprint hardware is available and permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                biometricPrompt.authenticate(new CancellationSignal(), this.getMainExecutor(), authenticationCallback);
            }
        } else {
            // Fingerprint authentication not available or permission not granted, proceed with regular login
            regularLogin();
        }
    }

    private void checkFingerprintAndDatabase() {
        String nid = editTextId.getText().toString().trim();
        String url = "http://10.0.2.2/cedarsvoice/cedars.php";

        // Send the national ID and scanned fingerprint data to the server
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean fingerprintMatch = jsonResponse.getBoolean("fingerprintMatch");
                            if (fingerprintMatch) {
                                // Fingerprint matches, proceed with login
                                login();
                            } else {
                                // Fingerprint does not match
                                Toast.makeText(VoterAct.this, "Fingerprint doesn't match", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NetworkError) {
                            Toast.makeText(VoterAct.this, "Network error occurred", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(VoterAct.this, "JSON parsing error occurred", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VoterAct.this, "Error occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", nid);
                // Convert scanned fingerprint data to base64 string
                String base64FingerprintData = android.util.Base64.encodeToString(scannedFingerprintData, android.util.Base64.DEFAULT);
                params.put("fingerprint", base64FingerprintData);
                return params;
            }
        };

        queue.add(request);
    }

    private void regularLogin() {
        String nid = editTextId.getText().toString().trim();
        String url = "http://10.0.2.2/cedarsvoice/cedars.php?id=" + nid;

        // Your existing code for sending request to server and handling response
        // ...
    }

    private void login() {
        // Code to handle login after ID verification
        // For example, navigate to another activity
        Intent intent = new Intent(VoterAct.this, VotingAct.class);
        intent.putExtra("message", "Hello from VoterActivity!");
        startActivity(intent);
        Toast.makeText(VoterAct.this, "ID exists in the database. You can now log in.", Toast.LENGTH_SHORT).show();
    }
}
