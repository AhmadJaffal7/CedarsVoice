package com.example.cedarsvoice;

import android.content.Intent;
import android.hardware.biometrics.BiometricManager;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import android.util.Base64;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.content.Context;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AddVoterActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextNationalID;
    private byte[] capturedFingerprintData;
    private Context context;
    private RequestQueue requestQueue;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_voter);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextNationalID = findViewById(R.id.editTextNationalID);
        context = this;
        executor = Executors.newSingleThreadExecutor();
    }

    public void captureFingerprint(View view) {
        try {
            SecretKey secretKey = generateSecretKey();
            // Create a Cipher object for encryption
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // Initialize the Cipher for encryption
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Build the BiometricPrompt.PromptInfo object
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Scan your fingerprint")
                    .setNegativeButtonText("Cancel")
                    .setConfirmationRequired(false)
                    .build();

            // Create a BiometricPrompt object
            BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    try {
                        // Fingerprint authentication succeeded, handle success
                        capturedFingerprintData = result.getCryptoObject().getCipher().doFinal();
                        Toast.makeText(AddVoterActivity.this, "Fingerprint captured", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    // Fingerprint authentication error, handle accordingly
                    Toast.makeText(AddVoterActivity.this, "Fingerprint authentication error: " + errString, Toast.LENGTH_SHORT).show();
                    Log.e("FingerprintAuth", "Error code: " + errorCode + ", error message: " + errString);
                }
            });

            // Start the fingerprint authentication process
            biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addVoter(View view) {
        String firstName = editTextFirstName.getText().toString();
        String lastName = editTextLastName.getText().toString();
        String nationalID = editTextNationalID.getText().toString();
        String fingerprintData = Base64.encodeToString(capturedFingerprintData, Base64.DEFAULT);

        String url = "http://10.0.2.2/cedarsvoice/add_voter.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("success")) {
                            // Login successful
                            Toast.makeText(getApplicationContext(), "Voter added successfully", Toast.LENGTH_SHORT).show();
                            editTextFirstName.setText("");
                            editTextLastName.setText("");
                            editTextNationalID.setText("");
                        } else {
                            // Login failed
                            Log.e("AddVoter", response.trim());
                            Toast.makeText(getApplicationContext(), "Failed to add voter, try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                        Log.e("VolleyError",error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("national_id", nationalID);
                params.put("first_name", firstName);
                params.put("last_name", lastName);
                params.put("fingerprint_data", fingerprintData);
                return params;
            }
        };

        queue.add(stringRequest);
    }
}