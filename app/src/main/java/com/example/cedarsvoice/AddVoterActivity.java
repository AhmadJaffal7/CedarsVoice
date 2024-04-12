package com.example.cedarsvoice;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricManager;


import android.util.Base64;
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                BiometricManager biometricManager = BiometricManager.from(this);
                switch (biometricManager.canAuthenticate()) {
                    case BiometricManager.BIOMETRIC_SUCCESS:
                        Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                        break;
                    case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                        Log.e("MY_APP_TAG", "No biometric features available on this device.");
                        Toast.makeText(this, "No biometric features available on this device.", Toast.LENGTH_SHORT).show();
                        return;
                    case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                        Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                        Toast.makeText(this, "Biometric features are currently unavailable.", Toast.LENGTH_SHORT).show();
                        return;
                    case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                        Log.e("MY_APP_TAG", "The user hasn't associated any biometric credentials with their account.");
                        Toast.makeText(this, "The user hasn't associated any biometric credentials with their account.", Toast.LENGTH_SHORT).show();
                        return;
                }
            }else {
                FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
                if (!fingerprintManager.isHardwareDetected()) {
                    // Device doesn't support fingerprint authentication
                    Toast.makeText(this, "No biometric features available on this device.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                    // User hasn't enrolled any fingerprints to authenticate with
                    Toast.makeText(this, "The user hasn't associated any biometric credentials with their account.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
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
                        Toast.makeText(AddVoterActivity.this, "Error capturing fingerprint: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("FingerprintCapture", "Error: ", e);                    }
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
            Toast.makeText(this, "Error initializing fingerprint capture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("FingerprintInit", "Error: ", e);
        }
    }

    public void addVoter(View view) {
        String firstName = editTextFirstName.getText().toString();
        String lastName = editTextLastName.getText().toString();
        String nationalID = editTextNationalID.getText().toString();
        String fingerprintData = Base64.encodeToString(capturedFingerprintData, Base64.DEFAULT);

        if (firstName.isEmpty() || lastName.isEmpty() || nationalID.isEmpty() || fingerprintData.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2/cedarsvoice/add_voter.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        // Show the ProgressBar
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Hide the ProgressBar
                        progressBar.setVisibility(View.GONE);
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
                        // Hide the ProgressBar
                        progressBar.setVisibility(View.GONE);
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
    private SecretKey generateSecretKey() throws Exception {
        // Get an instance of KeyGenerator with the desired algorithm and provider
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

        // Initialize the KeyGenParameterSpec specifying the key alias, purposes, and other parameters
        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder("MyKeyAlias", KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true) // Require user authentication (e.g., fingerprint) for every use of the key
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

        // Initialize the KeyGenerator with the KeyGenParameterSpec
        keyGenerator.init(builder.build());

        // Generate the secret key
        return keyGenerator.generateKey();
    }
}