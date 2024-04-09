package com.example.cedarsvoice;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Base64;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

public class AddVoterActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextNationalID;
    private byte[] capturedFingerprintData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_voter);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextNationalID = findViewById(R.id.editTextNationalID);

//        Button buttonAddVoter = findViewById(R.id.buttonAddVoter);
//        buttonAddVoter.setOnClickListener(v -> addVoter());
    }

    private void addVoter() {
        String firstName = editTextFirstName.getText().toString();
        String lastName = editTextLastName.getText().toString();
        String nationalID = editTextNationalID.getText().toString();
//        String fingerprintData = Base64.encodeToString(capturedFingerprintData, Base64.DEFAULT);

        String url = "http://10.0.2.2/cedarsvoice/adminLogin.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("first_name", firstName);
                params.put("last_name", lastName);
                params.put("national_id", nationalID);
//                params.put("fingerprint_data", fingerprintData);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    public void captureFingerprint(View view) {
        // Capture the fingerprint and store the data in capturedFingerprintData
    }
}