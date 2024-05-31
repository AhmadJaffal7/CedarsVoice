package com.example.cedarsvoice;

import android.content.Intent;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.net.ParseException;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        progressBar = findViewById(R.id.progressBar);

        // Initialize buttons
        LinearLayout btnAddCandidate = findViewById(R.id.llAddCandidate);
        LinearLayout btnAddSupervisor = findViewById(R.id.llAddSupervisor);
        LinearLayout btnAddVoter = findViewById(R.id.llAddVoter);
        LinearLayout btnAddPolice = findViewById(R.id.llAddPolice);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set navigation icon click listener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the Admin Activity
                Intent intent = new Intent(AdminActivity.this, AdminLoginActivity.class);
                startActivity(intent);
                finish(); // Optional: close current activity
            }
        });

        // Set onClickListeners
        btnAddCandidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, AddCandidateActivity.class);
                startActivity(intent);
            }
        });

        btnAddSupervisor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, AddSupervisorActivity.class);
                startActivity(intent);
            }
        });

        btnAddVoter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, AddVoterActivity.class);
                startActivity(intent);
            }
        });
        btnAddPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, AddPoliceActivity.class);
                startActivity(intent);
            }
        });
    }

    public void CheckResults(View view) {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar

        String url = getString(R.string.server) + "get_all_end_time.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE); // Hide progress bar when response is received

                        Log.d("ElectionTimeChecker", "Response: " + response); // Log the raw response

                        String[] endTimes = response.split(",");
                        boolean canOpenResultsActivity = true; // Initialize flag

                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            dateFormat.setLenient(false); // Ensure strict parsing

                            Calendar currentTime = Calendar.getInstance();
                            Log.d("ElectionTimeChecker", "Current Time: " + dateFormat.format(currentTime.getTime())); // Log current time

                            for (String endTime : endTimes) {
                                endTime = endTime.trim();
                                Log.d("ElectionTimeChecker", "End Time: " + endTime); // Log each end time

                                if (!endTime.isEmpty() && !endTime.equals("End time not found")) {
                                    Date electionEndTimeDate = dateFormat.parse(endTime);
                                    Calendar electionEndTime = Calendar.getInstance();
                                    electionEndTime.setTime(electionEndTimeDate);

                                    // Adjust the electionEndTime to today's date for accurate comparison
                                    electionEndTime.set(Calendar.YEAR, currentTime.get(Calendar.YEAR));
                                    electionEndTime.set(Calendar.MONTH, currentTime.get(Calendar.MONTH));
                                    electionEndTime.set(Calendar.DAY_OF_MONTH, currentTime.get(Calendar.DAY_OF_MONTH));

                                    Log.d("ElectionTimeChecker", "Parsed End Time: " + dateFormat.format(electionEndTime.getTime())); // Log parsed end time

                                    if (currentTime.before(electionEndTime)) {
                                        // Current time is before the end time of this election
                                        canOpenResultsActivity = false;
                                        break; // Exit the loop since we can't open the activity
                                    }
                                } else {
                                    Log.e("ElectionTimeChecker", "End time is not found or empty");
                                    // Handle the case when end time is not found or empty
                                }
                            }
                        } catch (java.text.ParseException e) {
                            throw new RuntimeException(e);
                        }

                        if (canOpenResultsActivity) {
                            // Open the Results activity
                            Intent intent = new Intent(AdminActivity.this, ResultsActivity.class);
                            startActivity(intent);
                        } else {
                            // Show a toast message
                            Toast.makeText(AdminActivity.this, "Election is still ongoing. Check back later.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE); // Hide progress bar in case of error

                        Log.e("ElectionTimeChecker", "Error fetching end times: " + error.getMessage());
                        // Handle the error
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(AdminActivity.this);
        queue.add(stringRequest);
    }
}
