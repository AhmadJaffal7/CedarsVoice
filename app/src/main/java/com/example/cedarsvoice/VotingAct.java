package com.example.cedarsvoice;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VotingAct extends AppCompatActivity {

    private Spinner spinnerCandidates;
    private HashMap<String, String> candidateNameToIdMap;
    private String voterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        spinnerCandidates = findViewById(R.id.candidates);

        // Get the voter_id from the intent extras
        voterId = getIntent().getStringExtra("voter_id");

        fetchCandidateNames();
    }

    private void fetchCandidateNames() {
        String url = "http://10.0.2.2/cedarsvoice/get_candidate_names.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            List<String> candidateNames = new ArrayList<>();
                            candidateNameToIdMap = new HashMap<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String candidateName = jsonObject.getString("candidate_name");
                                String candidateId = jsonObject.getString("candidate_id");
                                candidateNames.add(candidateName);
                                candidateNameToIdMap.put(candidateName, candidateId);
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(VotingAct.this, android.R.layout.simple_spinner_item, candidateNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerCandidates.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(VotingAct.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(stringRequest);
    }

    public void VoterVote(View view) {
        String url = "http://10.0.2.2/cedarsvoice/record_vote.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(VotingAct.this, response, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(VotingAct.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("voter_id", voterId);
                String selectedCandidateName = spinnerCandidates.getSelectedItem().toString();
                String selectedCandidateId = candidateNameToIdMap.get(selectedCandidateName);
                params.put("candidate_id", selectedCandidateId);
                return params;
            }
        };

        queue.add(stringRequest);
    }
}