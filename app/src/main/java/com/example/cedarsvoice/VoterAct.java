package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class VoterAct extends AppCompatActivity {
    EditText editTextId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter);

        editTextId = findViewById(R.id.Nid);
    }

    public void VoterLogin(View view) {
        String nid = editTextId.getText().toString().trim();
        String url = "http://10.0.2.2/cedarsvoice/cedars.php?id=" + nid;

        // Add logging statement here
        Log.d("VoterAct", "Sending request to: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean exists = jsonResponse.getBoolean("exists");
                            if (exists) {
                                // ID exists, allow the user to log in
                                login();
                            } else {
                                // ID doesn't exist, display a message
                                Toast.makeText(VoterAct.this, "ID does not exist in the database!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NetworkError) {
                            Toast.makeText(VoterAct.this, "Network error occurred", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(VoterAct.this, "JSON parsing error occurred", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VoterAct.this, "Error occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Add the request to the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(VoterAct.this);
        requestQueue.add(stringRequest);
    }


    private void login() {
        // Code to handle login after ID verification
        // For example, navigate to another activity
        Intent intent = new Intent(VoterAct.this, MainActivity.class);
        intent.putExtra("message", "Hello from VoterAct!");
        startActivity(intent);
        Toast.makeText(VoterAct.this, "ID exists in the database. You can now log in.", Toast.LENGTH_SHORT).show();
    }
}
