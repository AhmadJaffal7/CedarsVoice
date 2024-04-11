package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class VoterAct extends AppCompatActivity {
    EditText editTextId;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private byte[] scannedFingerprintData; // Assuming fingerprint data is scanned
    private Executor executor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter);

        editTextId = findViewById(R.id.Nid);
        executor = Executors.newSingleThreadExecutor();

        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(VoterAct.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                checkFingerprintAndDatabase();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();
    }

    public void VoterLogin(View view) {
        biometricPrompt.authenticate(promptInfo);
    }
    public void AuthenticateFingerprint(View view) {
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
                        scannedFingerprintData = result.getCryptoObject().getCipher().doFinal();
                        Toast.makeText(VoterAct.this, "Fingerprint captured", Toast.LENGTH_SHORT).show();
                        // Check if the ID exists in the database and proceed with login
                        checkFingerprintAndDatabase();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    // Fingerprint authentication error, handle accordingly
                    Toast.makeText(VoterAct.this, "Fingerprint authentication error: " + errString, Toast.LENGTH_SHORT).show();
                    Log.e("FingerprintAuth", "Error code: " + errorCode + ", error message: " + errString);
                }
            });

            // Start the fingerprint authentication process
            biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkFingerprintAndDatabase() {
        // You may need to add validation here to ensure the ID is not empty
        String nid = editTextId.getText().toString().trim();
        String url = "http://10.0.2.2/cedarsvoice/fingerprint.php"; // Update the server URL

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
                        Toast.makeText(VoterAct.this, "Error occurred" + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", nid); // Send voter ID instead of national ID
                // Convert scanned fingerprint data to base64 string if it's not null
                if (scannedFingerprintData != null) {
                    String base64FingerprintData = android.util.Base64.encodeToString(scannedFingerprintData, android.util.Base64.DEFAULT);
                    params.put("fingerprint", base64FingerprintData);
                }
                return params;
            }
        };

        queue.add(request);
    }

    private void login() {
        // Code to handle login after ID verification
        // For example, navigate to another activity
        Intent intent = new Intent(VoterAct.this, VotingAct.class);
        intent.putExtra("message", "Hello from VoterActivity!");
        startActivity(intent);
        Toast.makeText(VoterAct.this, "ID exists in the database. You can now log in.", Toast.LENGTH_SHORT).show();
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