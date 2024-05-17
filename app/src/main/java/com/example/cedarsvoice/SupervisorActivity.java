package com.example.cedarsvoice;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.net.ParseException;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SupervisorActivity extends AppCompatActivity {

    private TextView startTimeLabel, endTimeLabel;
    private String startTimeString, endTimeString; // Variable to store the end time
    private EditText startTimeEditText, endTimeEditText;
    private Button butStart, butStartElection;
    private int election_id = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor);

        startTimeLabel = findViewById(R.id.startTimeLabel);
        endTimeLabel = findViewById(R.id.endTimeLabel);
        startTimeEditText = findViewById(R.id.startTimeEditText);
        endTimeEditText = findViewById(R.id.endTimeEditText);
        butStart = findViewById(R.id.butStart);
        butStartElection = findViewById(R.id.butStartElection);
    }

    public void showStartTimePicker(View view) {
        try {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                            startTimeEditText.setText(selectedTime);
                            // Store the end time in a string variable
                            startTimeString = selectedTime;
                        }
                    }, hour, minute, false);
            timePickerDialog.show();
        } catch (Exception e) {
            Log.e("SupervisorActivity", "Error showing start time picker", e);
            Toast.makeText(this, "Error showing start time picker. Please try again.", Toast.LENGTH_SHORT).show();
        }

    }

    public void showEndTimePicker(View view) {
        try {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            // Format selected time and set it to EditText
                            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                            endTimeEditText.setText(selectedTime);
                            // Store the end time in a string variable
                            endTimeString = selectedTime;
                            Log.d("SupervisorActivity", "End time: " + endTimeString);
                        }
                    }, hour, minute, false);
            timePickerDialog.show();
        } catch (Exception e) {
            Log.e("SupervisorActivity", "Error showing end time picker", e);
            Toast.makeText(this, "Error showing end time picker. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public void startElection(View view) {
        startTimeLabel.setVisibility(View.VISIBLE);
        endTimeLabel.setVisibility(View.VISIBLE);
        startTimeEditText.setVisibility(View.VISIBLE);
        endTimeEditText.setVisibility(View.VISIBLE);
        butStart.setVisibility(View.VISIBLE);
        butStartElection.setVisibility(View.GONE);
    }

    public void saveTimes(View view) {
        try {
            // Validate end time
            if (startTimeString == null || startTimeString.isEmpty()) {
                Toast.makeText(this, "Start time cannot be empty. Please enter the start time.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (endTimeString == null || endTimeString.isEmpty()) {
                Toast.makeText(this, "End time cannot be empty. Please enter the end time.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse start and end times
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date startTime = null;
            Date endTime = null;
            try {
                startTime = dateFormat.parse(startTimeString);
                endTime = dateFormat.parse(endTimeString);
            } catch (ParseException e) {
                Log.e("SupervisorActivity", "Error parsing date", e);
                Toast.makeText(this, "Error parsing date. Please enter the date in the correct format.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the start time is before the end time
            if (!startTime.before(endTime)) {
                Toast.makeText(this, "Start time must be before end time.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save start and end times to the database
            saveTimesToDatabase(startTime, endTime);

            long startTimeMillis = startTime.getTime();
            long currentTimeMillis = System.currentTimeMillis();
            long remainingTimeMillis = startTimeMillis - currentTimeMillis;

            // Display a dialog showing remaining time to start
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Time to Start");

            final TextView countdownTextView = new TextView(this);
            builder.setView(countdownTextView);

            final AlertDialog dialog = builder.create();
            dialog.setCancelable(false); // Make the dialog unremovable
            dialog.show();

            new CountDownTimer(remainingTimeMillis, 1000) {
                public void onTick(long millisUntilFinished) {
                    long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                    long remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(remainingMinutes);
                    countdownTextView.setText("The election will start in " + remainingMinutes + " minutes and " + remainingSeconds + " seconds.");
                }

                public void onFinish() {
                    dialog.dismiss();
                    // If current time is after the start time, open the VoterActivity
//                    Intent intent = new Intent(SupervisorActivity.this, VoterAct.class);
//                    Log.e("SupervisorActivity", "Election ID: " + election_id);
//                    intent.putExtra("election_id", election_id);
//                    startActivity(intent);
//                    finish(); // Close current activity
                }
            }.start();
        } catch (Exception e) {
            // Log the exception
            Log.e("SupervisorActivity", "Error during save times", e);
            // Show an error message to the user
            Toast.makeText(this, "Error during save times. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTimesToDatabase(final Date startTime, final Date endTime) {
        // Convert Date objects to the desired format for sending to the server
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        final String startTimeString = dateFormat.format(startTime);
        final String endTimeString = dateFormat.format(endTime);

        // URL of the server-side script that will handle the time data
        String url = "http://10.0.2.2/cedarsvoice/save_time.php";

        RequestQueue queue = Volley.newRequestQueue(SupervisorActivity.this);
        // Create a StringRequest object
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the server's response
                        Log.d("SaveTimes", "Server Response: " + response);
                        try {
                            // Assuming the server returns the ID of the newly inserted record
                            election_id = Integer.parseInt(response.trim());
                            Log.d("SaveTimes", "New record ID: " + election_id);
                            // Open the VoterActivity and pass the new record ID
                            Intent intent = new Intent(SupervisorActivity.this, VoterAct.class);
                            intent.putExtra("election_id", election_id);
                            startActivity(intent);
                        } catch (NumberFormatException e) {
                            Log.e("SaveTimes", "Error parsing server response", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error
                        Log.e("SaveTimes", "Error: " + error.getMessage());
                        // You can display an error message to the user here
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Add the start and end times as parameters to the request
                Map<String, String> params = new HashMap<>();
                params.put("start_time", startTimeString);
                params.put("end_time", endTimeString);
                return params;
            }
        };

        // Add the request to the Volley request queue
        queue.add(stringRequest);
    }
}