package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.net.ParseException;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.security.keystore.KeyGenParameterSpec;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class VoterAct extends AppCompatActivity {
    EditText editTextId;
    private TextView remainingTimeTextView;
    private String endTime;
    private CountDownTimer countDownTimer;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private byte[] scannedFingerprintData; // Assuming fingerprint data is scanned
    private Executor executor;
    private byte[] capturedFingerprintData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter);

        // Retrieve the end time passed from AdminActivity
        endTime = getIntent().getStringExtra("endTime");
        remainingTimeTextView = findViewById(R.id.remainingTimeTextView);
        // Calculate remaining time
        calculateRemainingTime();
        // Start the countdown timer
        startCountdownTimer();
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
                try {
                    scannedFingerprintData = result.getCryptoObject().getCipher().doFinal();
                } catch (BadPaddingException e) {
                    throw new RuntimeException(e);
                } catch (IllegalBlockSizeException e) {
                    throw new RuntimeException(e);
                }

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


    String fingerprintData = Base64.encodeToString(capturedFingerprintData, Base64.DEFAULT);
    Log.d("Fingerprint Data", fingerprintData);
    String nid = editTextId.getText().toString().trim();
    String url = "http://10.0.2.2/cedarsvoice/fingerprint.php";
    RequestQueue queue = Volley.newRequestQueue(this);
    StringRequest request = new StringRequest(Request.Method.POST, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("Response", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        Log.d("Response", jsonResponse.toString());
                        boolean fingerprintMatch = jsonResponse.getBoolean("fingerprintMatch");
                        Log.d("Fingerprint Match", String.valueOf(fingerprintMatch));
                        if (fingerprintMatch) {
                            login();
                        } else {
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
            params.put("id", nid);
            params.put("fingerprint", fingerprintData);
            return params;
        }
    };
    queue.add(request);

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
                    capturedFingerprintData = result.getCryptoObject().getCipher().doFinal();
                    Toast.makeText(VoterAct.this, "Fingerprint captured", Toast.LENGTH_SHORT).show();
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





    private void login() {
    Log.d("Login", "Login method called");
    Intent intent = new Intent(VoterAct.this, VotingAct.class);
    intent.putExtra("message", "Hello from VoterActivity!");
    intent.putExtra("voter_id", "logged_in_voter_id"); // replace with actual logged in voter id
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
    private void calculateRemainingTime() {
        try {
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

            // Convert remaining time to hours and minutes
            int remainingHours = remainingMinutes / 60;
            int remainingMinutesAfterHours = remainingMinutes % 60;

            // Display remaining time in TextView
            String remainingTimeString = String.format(Locale.getDefault(), "%d hours %d minutes", remainingHours, remainingMinutesAfterHours);
            remainingTimeTextView.setText(remainingTimeString);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            remainingTimeTextView.setText("Error calculating remaining time");
        }
    }
    private void startCountdownTimer() {
        if (endTime != null && !endTime.isEmpty()) {
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

                if (remainingTimeMillis > 0) {
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
                }
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateRemainingTime(long millisUntilFinished) {
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the countdown timer to prevent memory leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}