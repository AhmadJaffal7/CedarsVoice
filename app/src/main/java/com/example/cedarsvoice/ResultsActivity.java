package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ResultsActivity extends AppCompatActivity {

    private TableLayout resultsTable;
    private RequestQueue requestQueue;
    private String SECRET_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        SECRET_KEY = getString(R.string.Key);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        resultsTable = findViewById(R.id.resultsTable);

        fetchResults();
    }

    private void fetchResults() {
        String url = "http://10.0.2.2/cedarsvoice/get_results.php";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Split the response by the delimiter
                        String[] candidates = response.split(",");

                        // Iterate through each candidate in the response
                        for (String candidate : candidates) {
                            // Split each candidate's information (e.g., name and vote count)
                            String[] info = candidate.split(":");
                            if (info.length == 2) {
                                String candidateName = info[0];
                                String encryptedVoteCount = info[1];
                                int voteCount = decryptVoteCount(encryptedVoteCount);
                                // Create a new row for the candidate in the table
                                TableRow row = new TableRow(ResultsActivity.this);
                                TextView name = new TextView(ResultsActivity.this);
                                TextView voteCountTextView = new TextView(ResultsActivity.this);

                                // Set text for name and vote count
                                name.setText(candidateName);
                                voteCountTextView.setText(String.valueOf(voteCount));

                                // Add name and vote count to the row
                                row.addView(name);
                                row.addView(voteCountTextView);

                                // Add the row to the table
                                resultsTable.addView(row);
                            } else {
                                Log.e("ResultsActivity", "Invalid candidate information: " + candidate);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ResultsActivity", "Error fetching results: " + error.getMessage());
                    }
                });

        // Add the request to the request queue
        requestQueue.add(stringRequest);
    }

    private int decryptVoteCount(String encryptedVoteCount) {
        try {
            // Decode the Base64 encoded data
            byte[] encryptedData = android.util.Base64.decode(encryptedVoteCount, android.util.Base64.DEFAULT);

            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(encryptedData);

            // Convert the decrypted data to integer
            return Integer.parseInt(new String(decryptedData));
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Error occurred
        }
    }

}