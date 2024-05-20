package com.example.cedarsvoice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

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

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AddPoliceActivity extends AppCompatActivity {

    private EditText editTextID, editTextName;
    private byte[] capturedFingerprintData;
    private Executor executor;
    private Uri fileUri;
    private static final int FILE_PICK_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_police);

        editTextID = findViewById(R.id.editTextPoliceID);
        editTextName = findViewById(R.id.editTextPoliceName);

        executor = Executors.newSingleThreadExecutor();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Disable title
        getSupportActionBar().setTitle("");

        // Set navigation icon click listener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the Admin Activity
                Intent intent = new Intent(AddPoliceActivity.this, AdminActivity.class);
                startActivity(intent);
                finish(); // Optional: close current activity
            }
        });
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

    public void captureFingerprint() {
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
                                    Toast.makeText(AddPoliceActivity.this, "Fingerprint captured", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddPoliceActivity.this, "Error capturing fingerprint: CryptoObject or Cipher is null", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddPoliceActivity.this, "Error capturing fingerprint: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AddPoliceActivity.this, "Fingerprint authentication error: " + errString, Toast.LENGTH_SHORT).show();
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

    public void addPolice(View view) {
        String name = editTextName.getText().toString();
        String id = editTextID.getText().toString().trim();

        // Validate user input
        if (name.isEmpty() || id.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!name.matches("[a-zA-Z]+")) {
            Toast.makeText(getApplicationContext(), "Invalid input. Only letters are allowed for name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate user input
        if (!id.matches("\\d+")) { // "\\d+" matches one or more digit characters
            Toast.makeText(getApplicationContext(), "Invalid input. Only numbers are allowed for id", Toast.LENGTH_SHORT).show();
            return;
        }

        if (id.length() > 11) {
            Toast.makeText(getApplicationContext(), "Invalid input. ID should not be more than 11 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (capturedFingerprintData == null) {
            Toast.makeText(getApplicationContext(), "Please capture fingerprint", Toast.LENGTH_SHORT).show();
            return;
        }

        String fingerprintData = Base64.encodeToString(capturedFingerprintData, Base64.DEFAULT);

        String url = getString(R.string.server)+"add_police.php";

        // Show the ProgressBar
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        String checkIDUrl = "getString(R.string.server)+check_police_id.php?police_id=" + id;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, checkIDUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("exists")) {
                            // Hide the ProgressBar
                            progressBar.setVisibility(View.GONE);
                            // Show an AlertDialog
                            new AlertDialog.Builder(AddPoliceActivity.this)
                                    .setTitle("Error")
                                    .setMessage("Police ID already exists")
                                    .setPositiveButton(android.R.string.ok, null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        } else {
                            ExecutorService executorService = Executors.newSingleThreadExecutor();

                            executorService.submit(new Runnable() {
                                @Override
                                public void run() {
                                    RequestQueue queue = Volley.newRequestQueue(AddPoliceActivity.this);

                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    // Hide the ProgressBar
                                                    progressBar.setVisibility(View.GONE);
                                                    if (response.trim().equals("success")) {
                                                        // Login successful
                                                        Toast.makeText(getApplicationContext(), "police added successfully", Toast.LENGTH_SHORT).show();
                                                        editTextName.setText("");
                                                        editTextID.setText("");
                                                        capturedFingerprintData = null; // Clear the captured fingerprint data
                                                    } else {
                                                        // Login failed
                                                        Log.e("AddSupervisor", response.trim());
                                                        Toast.makeText(getApplicationContext(), "Failed to add police, try again later", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    // Hide the ProgressBar
                                                    progressBar.setVisibility(View.GONE);
                                                    // Handle the error
                                                    Toast.makeText(getApplicationContext(), "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                                                    Log.e("VolleyError",error.toString());
                                                }
                                            }) {
                                        @Override
                                        protected Map<String, String> getParams() {
                                            Map<String, String> params = new HashMap<>();
                                            params.put("police_id", id);
                                            params.put("police_name", name);
                                            params.put("fingerprint_data", fingerprintData);
                                            return params;
                                        }
                                    };

                                    queue.add(stringRequest);
                                }
                            });
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                        // Hide the ProgressBar
                        progressBar.setVisibility(View.GONE);
                    }
                });

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
    private void uploadFingerprint() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_PICK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICK_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                fileUri = data.getData();
                String fileName = getFileName(fileUri, getContentResolver());
                Log.d("AddPoliceActivity", "Selected file: " + fileName);
                capturedFingerprintData = readFileBytes(fileUri, getContentResolver());
                Toast.makeText(AddPoliceActivity.this, "Fingerprint file uploaded successfully", Toast.LENGTH_SHORT).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user cancelled the file picker
                Toast.makeText(AddPoliceActivity.this, "File selection was cancelled", Toast.LENGTH_SHORT).show();
            }
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
                // Clear the buffer to free up memory
                buffer = new byte[4096];
            }
        } catch (IOException e) {
            Log.e("AddPoliceActivity", "Error reading file: " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e("AddPoliceActivity", "Error closing input stream: " + e.getMessage());
                }
            }
        }
        return outputStream.toByteArray();
    }
}
