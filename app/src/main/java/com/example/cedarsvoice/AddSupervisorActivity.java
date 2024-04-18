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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AddSupervisorActivity  extends AppCompatActivity {

    private EditText editTextID, editTextName;
    private Spinner spinnerPoliceID;
    private byte[] capturedFingerprintData;
    private Executor executor;
    private Uri fileUri;
    private static final int FILE_PICK_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_supervisor);

        editTextID = findViewById(R.id.editTextSupervisorID);
        editTextName = findViewById(R.id.editTextSupervisorName);
        spinnerPoliceID = findViewById(R.id.spinnerPoliceID);

        executor = Executors.newSingleThreadExecutor();
        fetchPoliceIds();
    }

    private void fetchPoliceIds() {
        String url = "http://10.0.2.2/cedarsvoice/get_police_ids.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse the JSON response
                            JSONArray jsonArray = new JSONArray(response);

                            // Convert the JSONArray to a List
                            List<String> policeIDs = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                policeIDs.add(jsonArray.getString(i));
                            }

                            // Add the default option to the list
                            policeIDs.add(0, "--Select Police ID--");

                            // If there are no police IDs, add the default option
                            if (policeIDs.isEmpty()) {
                                policeIDs.add("Add police and try again");
                            }

                            // Create an ArrayAdapter and set it to the Spinner
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AddSupervisorActivity.this, android.R.layout.simple_spinner_item, policeIDs);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerPoliceID.setAdapter(adapter);
                        } catch (JSONException e) {
                            Toast.makeText(AddSupervisorActivity.this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddSupervisorActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(stringRequest);
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
                                    Toast.makeText(AddSupervisorActivity.this, "Fingerprint captured", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddSupervisorActivity.this, "Error capturing fingerprint: CryptoObject or Cipher is null", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddSupervisorActivity.this, "Error capturing fingerprint: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AddSupervisorActivity.this, "Fingerprint authentication error: " + errString, Toast.LENGTH_SHORT).show();
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

    public void addSupervisor(View view) {
        String name = editTextName.getText().toString();
        String id = editTextID.getText().toString();
        String policeId = spinnerPoliceID.getSelectedItem().toString();

        if (capturedFingerprintData == null) {
            Toast.makeText(AddSupervisorActivity.this, "Please capture fingerprint", Toast.LENGTH_SHORT).show();
            return;
        }
        String fingerprintData = Base64.encodeToString(capturedFingerprintData, Base64.DEFAULT);

        if (name.isEmpty() || id.isEmpty() || policeId.isEmpty() || fingerprintData.isEmpty()) {
            Toast.makeText(AddSupervisorActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the selected police ID is the default option
        if (policeId.equals("Add police and try again")) {
            Toast.makeText(AddSupervisorActivity.this, "Please add police before adding a supervisor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the selected police ID is the default option
        if (policeId.equals("--Select Police ID--")) {
            Toast.makeText(AddSupervisorActivity.this, "Please select a police ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2/cedarsvoice/add_supervisor.php";
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
                            Toast.makeText(getApplicationContext(), "Supervisor added successfully", Toast.LENGTH_SHORT).show();
                            editTextName.setText("");
                            editTextID.setText("");
                            capturedFingerprintData = null; // Clear the captured fingerprint data
                        } else {
                            // Login failed
                            Log.e("AddSupervisor", response.trim());
                            Toast.makeText(getApplicationContext(), "Failed to add supervisor, try again later", Toast.LENGTH_SHORT).show();
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
                params.put("supervisor_id", id);
                params.put("supervisor_name", name);
                params.put("fingerprint_data", fingerprintData);
                params.put("police_id", policeId);
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
            Log.d("AddSupervisorActivity", "Selected file: " + fileName);
            capturedFingerprintData = readFileBytes(fileUri, getContentResolver());
            Toast.makeText(AddSupervisorActivity.this, "Fingerprint file uploaded successfully", Toast.LENGTH_SHORT).show();
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
            Log.e("AddSupervisorActivity", "Error reading file: " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e("AddSupervisorActivity", "Error closing input stream: " + e.getMessage());
                }
            }
        }
        return outputStream.toByteArray();
    }

}

