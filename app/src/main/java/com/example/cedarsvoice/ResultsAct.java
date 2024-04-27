package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResultsAct extends AppCompatActivity {

    private TableLayout resultsTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        resultsTable = findViewById(R.id.resultsTable);

        fetchResults();
    }

    private void fetchResults() {
        String url = "http://10.0.2.2/cedarsvoice/results.php"; // Replace with your actual URL

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject candidate = response.getJSONObject(i);

                                String candidateName = candidate.getString("candidate_name");
                                int votes = candidate.getInt("votes");

                                TableRow row = new TableRow(ResultsAct.this);
                                TextView name = new TextView(ResultsAct.this);
                                TextView voteCount = new TextView(ResultsAct.this);

                                name.setText(candidateName);
                                voteCount.setText(String.valueOf(votes));

                                row.addView(name);
                                row.addView(voteCount);

                                resultsTable.addView(row);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        queue.add(jsonArrayRequest);
    }
}