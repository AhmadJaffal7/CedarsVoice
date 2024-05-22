package com.example.cedarsvoice;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.net.ParseException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.security.keystore.KeyProperties;
import android.text.InputType;
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

public class VoterActivity extends AppCompatActivity {
    EditText editTextId;
    private TextView remainingTimeTextView;
    private CountDownTimer countDownTimer;
    private Executor executor;
    SecretKey secretKey;
    private int electionId;
    private String current_endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter);

        secretKey = getStoredSecretKey();

        electionId = getIntent().getIntExtra("electionID",0);
        remainingTimeTextView = findViewById(R.id.remainingTimeTextView);
        fetchEndTimeFromDatabase();

        editTextId = findViewById(R.id.Nid);

        executor = Executors.newSingleThreadExecutor();
    }

    public void VoterLogin(View view) {
        String nid = editTextId.getText().toString().trim();
        if (!nid.isEmpty()) {
            String url = getString(R.string.server) + "check_vote.php"; // replace with your server URL
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean hasVoted = jsonResponse.getBoolean("hasVoted");
                                if (hasVoted) {
                                    Toast.makeText(VoterActivity.this, "You have already voted", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Existing login process
                                    String url = getString(R.string.server) + "check_id.php?id=" + nid;
                                    RequestQueue queue = Volley.newRequestQueue(VoterActivity.this);
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
                                                            Toast.makeText(VoterActivity.this, "ID doesn't exist in the database", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Toast.makeText(VoterActivity.this, "Error occurred " + error.toString(), Toast.LENGTH_SHORT).show();
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
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(VoterActivity.this, "Error occurred " + error.toString(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(VoterActivity.this, "Fingerprint doesn't match or ID is empty", Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(VoterActivity.this, "Error fetching fingerprint: " + error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(VoterActivity.this, "Error capturing fingerprint: CryptoObject or Cipher is null", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VoterActivity.this, "Error capturing fingerprint: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(VoterActivity.this, "Fingerprint authentication error: " + errString, Toast.LENGTH_SHORT).show();
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
        String url = getString(R.string.server)+"retrieve_fingerprint.php?voter_id=" + voterId;
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
//    private void login() {
//        String nid = editTextId.getText().toString().trim();
//        String url = "http://10.0.2.2/cedarsvoice/check_vote.php?id=" + nid; // replace with your server URL
//        RequestQueue queue = Volley.newRequestQueue(this);
//        StringRequest request = new StringRequest(Request.Method.POST, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            JSONObject jsonResponse = new JSONObject(response);
//                            boolean hasVoted = jsonResponse.getBoolean("hasVoted");
//                            if (hasVoted) {
//                                Toast.makeText(VoterAct.this, "You have already voted", Toast.LENGTH_SHORT).show();
//                            } else {
//                                proceedWithLogin(nid);
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(VoterAct.this, "Error occurred" + error.toString(), Toast.LENGTH_SHORT).show();
//                    }
//                }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("id", nid);
//                return params;
//            }
//        };
//        queue.add(request);
//    }

    private void proceedWithLogin(String nid) {
        try{
            Log.d("Login", "Login method called");
            Intent intent = new Intent(VoterActivity.this, VotingActivity.class);
            intent.putExtra("message", "Hello from VoterActivity!");
            intent.putExtra("voter_id", nid);
            intent.putExtra("election_id", electionId);
            startActivity(intent);
            Toast.makeText(VoterActivity.this, "Logged in Successfully.", Toast.LENGTH_SHORT).show();
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

    private void fetchEndTimeFromDatabase() {
        String url = getString(R.string.server)+"get_end_time.php?electionId=" + electionId; // Replace with your server URL

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the server's response
                        Log.d("VoterAct", "Server Response: " + response);
                        String endTime = response.trim(); // Assuming the server returns only the end time

                        if (!endTime.isEmpty()) {
                            current_endTime = endTime;
                            // Calculate remaining time
                            calculateRemainingTime(endTime);
                            // Start the countdown timer
                            startCountdownTimer(endTime);
                        } else {
                            Log.e("VoterAct", "End time is empty");
                            remainingTimeTextView.setText("End time is not set");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error
                        Log.e("VoterAct", "Error fetching end time: " + error.getMessage());
                        remainingTimeTextView.setText("Error fetching end time");
                    }
                });

        // Add the request to the Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void calculateRemainingTime(String endTime) {
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
    private void startCountdownTimer(String endTime) {
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
                    Intent intent = new Intent(VoterActivity.this, SupervisorActivity.class);
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

            // Append hours to the string if not zero
            if (hours > 0) {
                remainingTimeString.append(hours).append(":");
            }

            // Append minutes to the string if not zero
            if (minutes > 0) {
                remainingTimeString.append(minutes);
            }

            // Always append seconds
            remainingTimeString.append(String.format(Locale.getDefault(), ":%02d ", seconds));

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

    public void DelayTime(View view) {
        showDelayTimeDialog();
    }
    private void showDelayTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delay Time");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter minutes to delay");
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String delayMinutesStr = input.getText().toString();
                if (!delayMinutesStr.isEmpty()) {
                    int delayMinutes = Integer.parseInt(delayMinutesStr);
                    updateEndTime(delayMinutes);
                } else {
                    Toast.makeText(VoterActivity.this, "Delay minutes cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void updateEndTime(int delayMinutes) {
        if (current_endTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            try {
                Date endTime = sdf.parse(current_endTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endTime);
                calendar.add(Calendar.MINUTE, delayMinutes); // Delay in minutes

                String newEndTime = sdf.format(calendar.getTime());

                // Now update the new end_time in the MySQL database
                updateEndTimeInDatabase(newEndTime);

                // Save the delay time in the database
                saveDelayTimeToDatabase(delayMinutes);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            Toast.makeText(VoterActivity.this, "Current end time is not set", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEndTimeInDatabase(final String newEndTime) {
        String url = getString(R.string.server)+"update_end_time.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the server's response
                        Log.d("VoterAct", "Update End Time Response: " + response);
                        Toast.makeText(VoterActivity.this, "End time updated successfully", Toast.LENGTH_SHORT).show();
                        // Update the end time in the UI
                        current_endTime = newEndTime;
                        calculateRemainingTime(newEndTime);
                        //Restart the countdown timer with the new end time
                        countDownTimer.cancel();
                        startCountdownTimer(newEndTime);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error
                        Log.e("VoterAct", "Error updating end time: " + error.getMessage());
                        // Optionally, you can handle the error if needed
                        Toast.makeText(VoterActivity.this, "Failed to update end time", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("end_time", newEndTime);
                params.put("electionId", String.valueOf(electionId));
                return params;
            }
        };

        // Add the request to the Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void saveDelayTimeToDatabase(final int delayMinutes) {
        String url = getString(R.string.server) + "save_delay_time.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the server's response
                        Log.d("VoterAct", "Save Delay Time Response: " + response);
                        Toast.makeText(VoterActivity.this, "Delay time saved successfully", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error
                        Log.e("VoterAct", "Error saving delay time: " + error.getMessage());
                        Toast.makeText(VoterActivity.this, "Failed to save delay time", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("delay_time", String.valueOf(delayMinutes));
                params.put("electionId", String.valueOf(electionId));
                return params;
            }
        };

        // Add the request to the Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

}
