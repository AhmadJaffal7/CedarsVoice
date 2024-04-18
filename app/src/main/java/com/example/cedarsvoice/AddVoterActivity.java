package com.example.cedarsvoice;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
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
import android.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AddVoterActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextNationalID;
    private byte[] capturedFingerprintData;
    private Executor executor;

    private Uri fileUri;

    private static final int FILE_PICK_REQUEST_CODE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_voter);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextNationalID = findViewById(R.id.editTextNationalID);

        executor = Executors.newSingleThreadExecutor();
    }

    public void addFingerprint(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Fingerprint")
                .setItems(new CharSequence[]{"Capture Fingerprint", "Upload Fingerprint File"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            captureFingerprint();
                        } else {
                            uploadFingerprint();
                        }
                    }
                });
        builder.create().show();
    }

    public void addVoter(View view) {
        String firstName = editTextFirstName.getText().toString();
        String lastName = editTextLastName.getText().toString();
        String nationalID = editTextNationalID.getText().toString();
        if (capturedFingerprintData == null) {
            Toast.makeText(getApplicationContext(), "Please add fingerprint", Toast.LENGTH_SHORT).show();
            return;
        }
        String fingerprintData = Base64.encodeToString(capturedFingerprintData, Base64.DEFAULT);

        // Validate user input
        if (firstName.isEmpty() || lastName.isEmpty() || nationalID.isEmpty() || fingerprintData.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate firstName and lastName to prevent SQL injection
        if (!firstName.matches("[a-zA-Z ]+") || !lastName.matches("[a-zA-Z ]+")) {
            Toast.makeText(getApplicationContext(), "Invalid input. Only alphabetic characters and spaces are allowed for first name and last name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate nationalID to prevent SQL injection
        if (!nationalID.matches("\\d+")) { // "\\d+" matches one or more digit characters
            Toast.makeText(getApplicationContext(), "Invalid input. Only numbers are allowed for national ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nationalID.length() > 11) {
            Toast.makeText(getApplicationContext(), "Invalid input. National ID should not be more than 11 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2/cedarsvoice/add_voter.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        // Show the ProgressBar
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
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
                            capturedFingerprintData = null; // Clear the captured fingerprint data
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
        });
    }


    public void captureFingerprint() {
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
                        if (result.getCryptoObject() != null && result.getCryptoObject().getCipher() != null) {
                            capturedFingerprintData = result.getCryptoObject().getCipher().doFinal();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddVoterActivity.this, "Fingerprint captured", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddVoterActivity.this, "Error capturing fingerprint: CryptoObject or Cipher is null", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddVoterActivity.this, "Error capturing fingerprint: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.e("FingerprintCapture", "Error: ", e);                    }
                }

                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    // Fingerprint authentication error, handle accordingly
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddVoterActivity.this, "Fingerprint authentication error: " + errString, Toast.LENGTH_SHORT).show();
                        }
                    });                    Log.e("FingerprintAuth", "Error code: " + errorCode + ", error message: " + errString);
                }
            });

            // Start the fingerprint authentication process
            biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing fingerprint capture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("FingerprintInit", "Error: ", e);
        }
    }

    private void uploadFingerprint() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_PICK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICK_REQUEST_CODE && resultCode ==  Activity.RESULT_OK) {
            fileUri = data.getData();
            String fileName = getFileName(fileUri, getContentResolver());
            Log.d("AddVoterActivity", "Selected file: " + fileName);
            capturedFingerprintData = readFileBytes(fileUri, getContentResolver());
            Toast.makeText(AddVoterActivity.this, "Fingerprint file uploaded successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri, ContentResolver contentResolver) {
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        cursor.moveToFirst();
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        String fileName = cursor.getString(nameIndex);
        cursor.close();
        return fileName;
    }

    private byte[] readFileBytes(Uri uri, ContentResolver contentResolver) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.e("AddVoterActivity", "Error reading file: " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e("AddVoterActivity", "Error closing input stream: " + e.getMessage());
                }
            }
        }
        return outputStream.toByteArray();
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