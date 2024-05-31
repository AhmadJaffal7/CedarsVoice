package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class SupervisorLoginActivity extends AppCompatActivity {
    private EditText editTextSupervisorID, editTextPoliceID;
    private Button btnVerifySupervisor, btnVerifyPolice, btnLogin;
    private String supervisorID;
    ProgressBar progressBar;
    private SecretKey secretKey;
    private Executor executor;
    private boolean isSupervisorVerified = false, isPoliceVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.supervisor_login);

        editTextSupervisorID = findViewById(R.id.editTextSupervisorID);
        editTextPoliceID = findViewById(R.id.editTextPoliceID);
        btnVerifySupervisor = findViewById(R.id.btnVerifySupervisor);
        btnVerifyPolice = findViewById(R.id.btnVerifyPolice);
        btnLogin = findViewById(R.id.btnSupervisorLogin);
        progressBar = findViewById(R.id.progressBar);
        executor = ContextCompat.getMainExecutor(this);
    }

    public void verifyLogin(View view) {
        supervisorID = editTextSupervisorID.getText().toString().trim();
        String policeID = editTextPoliceID.getText().toString().trim();
        // Validate input fields
        if (!supervisorID.isEmpty() && !policeID.isEmpty()) {
            // Initialize Volley RequestQueue
            RequestQueue queue = Volley.newRequestQueue(this);

            // URL of your backend endpoint to verify IDs
            String url = getString(R.string.server) + "supervisor_login.php";

            // Get the ProgressBar and make it visible
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            // Create a StringRequest with POST method
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Hide the ProgressBar
                    progressBar.setVisibility(View.GONE);
                    if (response.equals("success")) {
                        // Police ID and Supervisor ID verified successfully
                        btnVerifySupervisor.setVisibility(View.VISIBLE);
                        btnVerifyPolice.setVisibility(View.VISIBLE);
                        btnLogin.setVisibility(View.GONE);
                        editTextSupervisorID.setEnabled(false);
                        editTextPoliceID.setEnabled(false);
                        Toast.makeText(SupervisorLoginActivity.this, "IDs are correct. Verify your fingerprint", Toast.LENGTH_SHORT).show();
                    } else {
                        // Verification failed
                        Toast.makeText(SupervisorLoginActivity.this, "Incorrect Supervisor ID or Police ID", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
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
                    }
                }
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

    public void verifySupervisor(View view) {
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
            } else {
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

            // Create a Cipher and initialize it
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            SecretKey secretKey = getStoredSecretKey(); // Retrieve the secret key from the Android KeyStore
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Create a CryptoObject using the Cipher
            BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(cipher);

            // Build the BiometricPrompt.PromptInfo object
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Scan your fingerprint").setNegativeButtonText("Cancel").setConfirmationRequired(false).build();

            // Create a BiometricPrompt object
            BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    try {
                        Log.d("SecretKey", "Secret key: " + secretKey);
                        if (result.getCryptoObject() != null && result.getCryptoObject().getCipher() != null) {
                            // Extract encrypted fingerprint data and IV from the AuthenticationResult
                            byte[] encryptedFingerprintData = result.getCryptoObject().getCipher().doFinal();
                            Log.d("FingerprintCapture", "Encrypted fingerprint data: " + Arrays.toString(encryptedFingerprintData));
                            byte[] iv = result.getCryptoObject().getCipher().getIV();
                            Log.d("FingerprintCapture", "IV: " + Arrays.toString(iv));

                            // Fetch the stored fingerprint data from the database
                            fetchFingerprintFromDatabase(Integer.parseInt(supervisorID), new FingerprintCallback() {
                                @Override
                                public void onFingerprintReceived(byte[] storedFingerprintData) {
                                    // Decrypt the captured fingerprint data
                                    byte[] decryptedCapturedFingerprintData = decryptData(encryptedFingerprintData, secretKey, iv);

                                    // Decrypt the stored fingerprint data
                                    byte[] decryptedStoredFingerprintData = decryptData(storedFingerprintData, secretKey, iv);

                                    // Compare the decrypted fingerprint data
                                    if (compareFingerprints(decryptedCapturedFingerprintData, decryptedStoredFingerprintData)) {
                                        isSupervisorVerified = true;
                                        if (isPoliceVerified) {
                                            proceedWithLogin(supervisorID);
                                        }else {
                                            btnVerifySupervisor.setVisibility(View.GONE);
                                        }
                                        Toast.makeText(SupervisorLoginActivity.this, "Supervisor Fingerprint match", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onError(String error) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(SupervisorLoginActivity.this, "Error fetching fingerprint: " + error, Toast.LENGTH_SHORT).show();
                                            Log.e("FingerprintFetch", "Error: " + error);
                                        }
                                    });
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SupervisorLoginActivity.this, "Error capturing fingerprint: CryptoObject or Cipher is null", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SupervisorLoginActivity.this, "Error capturing fingerprint: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("FingerprintCapture", "Error: ", e);
                            }
                        });
                        Log.e("FingerprintCapture", "Error: ", e);
                    }
                }

                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    // Fingerprint authentication error, handle accordingly
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SupervisorLoginActivity.this, "Fingerprint authentication error: " + errString, Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("FingerprintAuth", "Error code: " + errorCode + ", error message: " + errString);
                }
            });

            // Start the fingerprint authentication process
            biometricPrompt.authenticate(promptInfo, cryptoObject);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing fingerprint capture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("FingerprintInit", "Error: ", e);
        }
    }

    private SecretKey getStoredSecretKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry("MySecretKeyAlias", null);
            return secretKeyEntry.getSecretKey();
        } catch (Exception e) {
            Log.e("KeystoreError", "Error retrieving key", e);
            return null;
        }
    }

    private void proceedWithLogin(String nid) {
        try {
            Log.d("Login", "Login method called");
            Intent intent = new Intent(SupervisorLoginActivity.this, SupervisorActivity.class);
            intent.putExtra("supervisorID", Integer.parseInt(supervisorID));
            startActivity(intent);
            finish();
            Toast.makeText(SupervisorLoginActivity.this, "Logged in Successfully.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Log the exception
            Log.e("VoterAct", "Error during login", e);
            // Show an error message to the user
            Toast.makeText(this, "Error during login. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }


    interface FingerprintCallback {
        void onFingerprintReceived(byte[] fingerprint);

        void onError(String error);
    }

    private void fetchFingerprintFromDatabase(int supervisor_id, SupervisorLoginActivity.FingerprintCallback callback) {
        String url = getString(R.string.server) + "retrieve_supervisor_fingerprint.php?supervisor_id=" + supervisor_id;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.equals("Fingerprint not found")) {
                    String[] parts = response.split(":");
                    if (parts.length == 2) { // Check if the response has both fingerprint and IV
                        byte[] encryptedFingerprintData = Base64.decode(parts[0], Base64.DEFAULT);
                        Log.d("FingerprintDatabase", "Encrypted fingerprint data: " + Arrays.toString(encryptedFingerprintData));
                        byte[] iv = Base64.decode(parts[1], Base64.DEFAULT);
                        Log.d("FingerprintDatabase", "IV: " + Arrays.toString(iv));
                        if (encryptedFingerprintData != null && iv != null) { // Check if the data is not null
                            byte[] storedFingerprintData = decryptData(encryptedFingerprintData, secretKey, iv);
                            callback.onFingerprintReceived(storedFingerprintData);
                        } else {
                            callback.onError("Invalid fingerprint data received from the server");
                        }
                    } else {
                        callback.onError("Invalid response format from the server");
                    }
                } else {
                    callback.onError("Fingerprint not found in the database");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError("Error fetching fingerprint: " + error.toString());
                Log.d("FingerprintDatabase", "Error: " + error.toString());
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private boolean compareFingerprints(byte[] capturedFingerprintData, byte[] storedFingerprintData) {
        String capturedFingerprintString = Arrays.toString(capturedFingerprintData);
        String storedFingerprintString = Arrays.toString(storedFingerprintData);

        Log.d("FingerprintComparison", "Captured Fingerprint: " + capturedFingerprintString);
        Log.d("FingerprintComparison", "Stored Fingerprint: " + storedFingerprintString);

        boolean matchResult = Arrays.equals(capturedFingerprintData, storedFingerprintData);
        Log.d("FingerprintComparison", "Fingerprints match: " + matchResult);

        return matchResult;
    }

    private byte[] decryptData(byte[] encryptedData, SecretKey secretKey, byte[] iv) {
        try {
            if (encryptedData == null) {
                Log.e("DecryptionError", "Null input buffer");
                return null;
            }
            if (iv.length != 16) {
                Log.e("DecryptionError", "Invalid IV: Incorrect size. Expected 16 bytes but got " + iv.length);
                return null;
            }
            if (encryptedData.length % 16 != 0) {
                Log.e("DecryptionError", "Invalid block size. Data length should be a multiple of 16");
                return null;
            }
            if (secretKey == null) {
                Log.e("DecryptionError", "Secret key is null");
                return null;
            }

            // Log encrypted data and IV for debugging
            Log.d("DecryptionDebug", "Encrypted data: " + Arrays.toString(encryptedData));
            Log.d("DecryptionDebug", "IV: " + Arrays.toString(iv));

            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decryptedData = cipher.doFinal(encryptedData);

            // Log decrypted data for debugging
            Log.d("DecryptionDebug", "Decrypted data: " + Arrays.toString(decryptedData));

            return decryptedData;
        } catch (InvalidAlgorithmParameterException e) {
            Log.e("DecryptionError", "Invalid algorithm parameter", e);
            return null;
        } catch (InvalidKeyException e) {
            Log.e("DecryptionError", "Invalid key", e);
            return null;
        } catch (Exception e) {
            Log.e("DecryptionError", "Error decrypting data", e);
            return null;
        }
    }

    public void verifyPolice(View view) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                BiometricManager biometricManager = BiometricManager.from(this);
                switch (biometricManager.canAuthenticate()) {
                    case BiometricManager.BIOMETRIC_SUCCESS:
                        Log.d(getString(R.string.app_name), "App can authenticate using biometrics.");
                        break;
                    case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                        Log.e(getString(R.string.app_name), "No biometric features available on this device.");
                        Toast.makeText(this, "No biometric features available on this device.", Toast.LENGTH_SHORT).show();
                        return;
                    case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                        Log.e(getString(R.string.app_name), "Biometric features are currently unavailable.");
                        Toast.makeText(this, "Biometric features are currently unavailable.", Toast.LENGTH_SHORT).show();
                        return;
                    case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                        Log.e(getString(R.string.app_name), "The user hasn't associated any biometric credentials with their account.");
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
            SecretKey secretKey = getStoredSecretKey();
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
                    Toast.makeText(SupervisorLoginActivity.this, "Police Fingerprint match", Toast.LENGTH_SHORT).show();
                    isPoliceVerified = true;
                    if (isSupervisorVerified) {
                        proceedWithLogin(supervisorID);
                    }else {
                        btnVerifyPolice.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    // Fingerprint authentication error, handle accordingly
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SupervisorLoginActivity.this, "Fingerprint authentication error: " + errString, Toast.LENGTH_SHORT).show();
                        }
                    });
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
}
