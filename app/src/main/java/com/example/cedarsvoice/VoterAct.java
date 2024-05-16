package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.net.ParseException;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class VoterAct extends AppCompatActivity {
    EditText editTextId;
    private TextView remainingTimeTextView;
    private String endTime;
    private CountDownTimer countDownTimer;
    private Executor executor;
    SecretKey secretKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter);

        secretKey = getStoredSecretKey();
        // Retrieve the end time passed from AdminActivity
        endTime = getIntent().getStringExtra("endTime");
        remainingTimeTextView = findViewById(R.id.remainingTimeTextView);
        // Calculate remaining time
        calculateRemainingTime();
        // Start the countdown timer
        startCountdownTimer();
        editTextId = findViewById(R.id.Nid);
        executor = Executors.newSingleThreadExecutor();
    }

    public void VoterLogin(View view) {
        String nid = editTextId.getText().toString().trim();
        if (!nid.isEmpty()) {
            String url = "http://10.0.2.2/cedarsvoice/check_id.php?id=" + nid;
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean idExists = jsonResponse.getBoolean("exists");
                                if (idExists) {
                                    AuthenticateFingerprint(Integer.parseInt(nid));
                                } else {
                                    Toast.makeText(VoterAct.this, "ID doesn't exist in the database", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(VoterAct.this, "Error occurred " + error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", nid);
                    return params;
                }
            };
            queue.add(request);
        } else {
            Toast.makeText(VoterAct.this, "Fingerprint doesn't match or ID is empty", Toast.LENGTH_SHORT).show();
        }
    }

    public void AuthenticateFingerprint(int nid) {
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
                        Log.d("SecretKey", "Secret key: " + secretKey);
                        if (result.getCryptoObject() != null && result.getCryptoObject().getCipher() != null) {
                            // Extract encrypted fingerprint data and IV from the AuthenticationResult
                            byte[] encryptedFingerprintData = result.getCryptoObject().getCipher().doFinal();
                            Log.d("FingerprintCapture", "Encrypted fingerprint data: " + Arrays.toString(encryptedFingerprintData));
                            byte[] iv = result.getCryptoObject().getCipher().getIV();
                            Log.d("FingerprintCapture", "IV: " + Arrays.toString(iv));

                            // Fetch the stored fingerprint data from the database
                            fetchFingerprintFromDatabase(nid, new FingerprintCallback() {
                                @Override
                                public void onFingerprintReceived(byte[] storedFingerprintData) {
                                    // Decrypt the captured fingerprint data
                                    byte[] decryptedCapturedFingerprintData = decryptData(encryptedFingerprintData, secretKey, iv);

                                    // Decrypt the stored fingerprint data
                                    byte[] decryptedStoredFingerprintData = decryptData(storedFingerprintData, secretKey, iv);

                                    // Compare the decrypted fingerprint data
                                    if (compareFingerprints(decryptedCapturedFingerprintData, decryptedStoredFingerprintData)) {
                                        // If the fingerprints match, proceed with login
                                        proceedWithLogin(String.valueOf(nid));
                                    }
                                }
                                @Override
                                public void onError(String error) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(VoterAct.this, "Error fetching fingerprint: " + error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(VoterAct.this, "Error capturing fingerprint: CryptoObject or Cipher is null", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VoterAct.this, "Error capturing fingerprint: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(VoterAct.this, "Fingerprint authentication error: " + errString, Toast.LENGTH_SHORT).show();
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

    interface FingerprintCallback {
        void onFingerprintReceived(byte[] fingerprint);
        void onError(String error);
    }

    private void fetchFingerprintFromDatabase(int voterId, FingerprintCallback callback) {
        String url = "http://10.0.2.2/cedarsvoice/retrieve_fingerprint.php?voter_id=" + voterId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
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
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Error fetching fingerprint: " + error.toString());
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
    private void login() {
        String nid = editTextId.getText().toString().trim();
        String url = "http://10.0.2.2/cedarsvoice/check_vote.php?id=" + nid; // replace with your server URL
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean hasVoted = jsonResponse.getBoolean("hasVoted");
                            if (hasVoted) {
                                Toast.makeText(VoterAct.this, "You have already voted", Toast.LENGTH_SHORT).show();
                            } else {
                                proceedWithLogin(nid);
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
                params.put("id", nid);
                return params;
            }
        };
        queue.add(request);
    }

    private void proceedWithLogin(String nid) {
        try{
            Log.d("Login", "Login method called");
            Intent intent = new Intent(VoterAct.this, VotingAct.class);
            intent.putExtra("message", "Hello from VoterActivity!");
            intent.putExtra("voter_id", nid); // replace with actual logged in voter id
            intent.putExtra("endTime", endTime);
            startActivity(intent);
            Toast.makeText(VoterAct.this, "Logged in Successfully.", Toast.LENGTH_SHORT).show();
        }catch (Exception e) {
            // Log the exception
            Log.e("VoterAct", "Error during login", e);
            // Show an error message to the user
            Toast.makeText(this, "Error during login. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private SecretKey getStoredSecretKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return (SecretKey) keyStore.getKey("MySecretKeyAlias", null);
        } catch (Exception e) {
            Log.e("KeystoreError", "Error retrieving key", e);
            return null;
        }
    }

    private void calculateRemainingTime() {
        try {
            if (endTime != null) {
                // Parse end time to get hours and minutes
                String[] endTimeParts = endTime.split(":");
                int endHour = Integer.parseInt(endTimeParts[0]);
                int endMinute = Integer.parseInt(endTimeParts[1]);

                // Get current time
                Calendar currentTime = Calendar.getInstance();
                int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                int currentMinute = currentTime.get(Calendar.MINUTE);

                // Calculate remaining time in minutes
                int remainingMinutes = (endHour - currentHour) * 60 + (endMinute - currentMinute);

                // Check if end time is the next day
                if (remainingMinutes < 0) {
                    remainingMinutes += 24 * 60; // Add 24 hours
                }

                // Convert remaining time to hours and minutes
                int remainingHours = remainingMinutes / 60;
                int remainingMinutesAfterHours = remainingMinutes % 60;

                // Display remaining time in TextView
                String remainingTimeString = String.format(Locale.getDefault(), "%d hours %d minutes", remainingHours, remainingMinutesAfterHours);
                remainingTimeTextView.setText(remainingTimeString);
            } else {
                Log.e("VoterAct", "End time is null");
                remainingTimeTextView.setText("End time is not set");
            }
        } catch (NumberFormatException e) {
            Log.e("VoterAct", "Error parsing end time", e);
            remainingTimeTextView.setText("Error parsing end time");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e("VoterAct", "Error splitting end time", e);
            remainingTimeTextView.setText("Error splitting end time");
        }
    }
    private void startCountdownTimer() {
        if (endTime == null || endTime.isEmpty()) {
            Log.e("VoterAct", "End time is null or empty");
            remainingTimeTextView.setText("End time is not set");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date endTimeDate = sdf.parse(endTime);
            // Set the date of endTimeDate to the current date
            Calendar endTimeCalendar = Calendar.getInstance();
            endTimeCalendar.setTime(endTimeDate);
            endTimeCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            endTimeCalendar.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
            endTimeCalendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

            long endTimeMillis = endTimeCalendar.getTimeInMillis();
            long currentTimeMillis = System.currentTimeMillis();
            long remainingTimeMillis = endTimeMillis - currentTimeMillis;

            if (remainingTimeMillis <= 0) {
                Log.e("VoterAct", "End time has already passed");
                remainingTimeTextView.setText("End time has already passed");
                return;
            }

            countDownTimer = new CountDownTimer(remainingTimeMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Update remaining time on each tick
                    updateRemainingTime(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    // Close VoterActivity and open SupervisorActivity
                    Intent intent = new Intent(VoterAct.this, SupervisorActivity.class);
                    startActivity(intent);
                    finish(); // Close current activity
                }
            }.start();
        } catch (ParseException e) {
            Log.e("VoterAct", "Error parsing end time", e);
            remainingTimeTextView.setText("Error parsing end time");
        } catch (java.text.ParseException e) {
            Log.e("VoterAct", "Error parsing end time", e);
            remainingTimeTextView.setText("Error parsing end time");
        }
    }
    private void updateRemainingTime(long millisUntilFinished) {
        if (millisUntilFinished <= 0) {
            Log.e("VoterAct", "Time has already finished");
            remainingTimeTextView.setText("Time has already finished");
            return;
        }

        try {
            // Convert milliseconds until finished to hours, minutes and seconds
            long totalSeconds = millisUntilFinished / 1000;
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;

            // Create a StringBuilder to build the remaining time string
            StringBuilder remainingTimeString = new StringBuilder();
            remainingTimeString.append("Remaining time: ");
            // Append hours to the string if not zero
            if (hours > 0) {
                remainingTimeString.append(hours).append(" hours ");
            }

            // Append minutes to the string if not zero
            if (minutes > 0) {
                remainingTimeString.append(minutes).append(" minutes ");
            }

            // Always append seconds
            remainingTimeString.append(String.format(Locale.getDefault(), "%02d seconds", seconds));

            // Display remaining time in TextView
            remainingTimeTextView.setText(remainingTimeString.toString());
        } catch (Exception e) {
            Log.e("VoterAct", "Error updating remaining time", e);
            remainingTimeTextView.setText("Error updating remaining time");
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the countdown timer to prevent memory leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}
