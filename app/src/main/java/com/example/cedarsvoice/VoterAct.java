package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class VoterAct extends AppCompatActivity {
    EditText editTextId;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private byte[] scannedFingerprintData; // Assuming fingerprint data is scanned
    private Executor executor;
    private byte[] capturedFingerprintData;
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
}