package com.example.cedarsvoice;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
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

public class SupervisorActivity extends AppCompatActivity {

    private String startTimeString;
    private String endTimeString; // Variable to store the end time
    private EditText startTimeEditText, endTimeEditText;
    private int election_id = 0, supervisor_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor);

        startTimeEditText = findViewById(R.id.startTimeEditText);
        endTimeEditText = findViewById(R.id.endTimeEditText);
        supervisor_id = getIntent().getIntExtra("supervisorID", 0);
        Log.d("SupervisorActivity", "Supervisor ID: " + supervisor_id);
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

            // Parse start time
            Calendar startTimeCalendar = parseStartTime(startTimeString);
            Log.e("SupervisorActivity", "Start time: " + startTimeCalendar.getTime());
            if (startTimeCalendar == null) {
                Toast.makeText(this, "Error parsing start time. Please enter the date in the correct format.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse end time
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date endTime = null;
            try {
                endTime = dateFormat.parse(endTimeString);
            } catch (ParseException e) {
                Log.e("SupervisorActivity", "Error parsing end time", e);
                Toast.makeText(this, "Error parsing end time. Please enter the date in the correct format.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the start time is before the current time
            Calendar currentTime = Calendar.getInstance();
            Log.e("SupervisorActivity", "Current time: " + currentTime.getTime());
            if (currentTime.before(startTimeCalendar)) {
                // Start time is after the current time
                showCountdownTimer(startTimeCalendar, endTime);
            } else {
                // Check if the start time is before the end time
                if (!startTimeCalendar.getTime().before(endTime)) {
                    Toast.makeText(this, "Start time must be before end time.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save start and end times to the database
                saveTimesToDatabase(startTimeCalendar.getTime(), endTime);
            }

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
        String url = getString(R.string.server) + "save_time.php";

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
                            Intent intent = new Intent(SupervisorActivity.this, VoterActivity.class);
                            intent.putExtra("electionID", election_id);
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
                params.put("supervisor_id", String.valueOf(supervisor_id));
                return params;
            }
        };

        // Add the request to the Volley request queue
        queue.add(stringRequest);
    }

    private void showCountdownTimer(Calendar startTimeCalendar, Date endTime) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Countdown Timer");
        builder.setCancelable(false); // User can't dismiss the dialog by clicking outside of it

        final TextView countdownTextView = new TextView(this);
        countdownTextView.setTextSize(24);
        countdownTextView.setPadding(16, 16, 16, 16);
        builder.setView(countdownTextView);

        final AlertDialog dialog = builder.create();

        // Get the current time as a Calendar instance
        Calendar currentTime = Calendar.getInstance();

        // Calculate the time difference between the start time and the current time
        long timeDifference = startTimeCalendar.getTimeInMillis() - currentTime.getTimeInMillis();
        Log.d("SupervisorActivity", "Time difference: " + timeDifference);
        if (timeDifference > 0) {
            // Current time is before the start time
            dialog.show();

            // Start the countdown timer
            new CountDownTimer(timeDifference, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long seconds = millisUntilFinished / 1000;
                    long minutes = seconds / 60;
                    long hours = minutes / 60;

                    seconds %= 60;
                    minutes %= 60;

                    String countdownText = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                    countdownTextView.setText(countdownText);
                }

                @Override
                public void onFinish() {
                    dialog.dismiss();
                    // Save start and end times to the database
                    saveTimesToDatabase(startTimeCalendar.getTime(), endTime);
                }
            }.start();
        } else {
            // Current time is after or equal to the start time
            dialog.dismiss();
            Toast.makeText(SupervisorActivity.this, "Start time must be after the current time.", Toast.LENGTH_SHORT).show();
        }
    }
    private Calendar parseStartTime(String startTimeString) {
        Calendar startTimeCalendar = Calendar.getInstance();
        String[] timeParts = startTimeString.split(":");
        if (timeParts.length == 2) {
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            startTimeCalendar.set(Calendar.HOUR_OF_DAY, hour);
            startTimeCalendar.set(Calendar.MINUTE, minute);
            startTimeCalendar.set(Calendar.SECOND, 0);
            startTimeCalendar.set(Calendar.MILLISECOND, 0);
            return startTimeCalendar;
        } else {
            Log.e("SupervisorActivity", "Invalid start time format");
            return null;
        }
    }
}