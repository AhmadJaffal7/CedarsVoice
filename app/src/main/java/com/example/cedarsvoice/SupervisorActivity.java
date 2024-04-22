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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SupervisorActivity extends AppCompatActivity {

    private TextView startTimeLabel, endTimeLabel;
    private String startTimeString, endTimeString; // Variable to store the end time
    private EditText startTimeEditText, endTimeEditText;
    private Button butStart, butStartElection;


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
        try{
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
                            startTimeString = selectedTime;                        }
                    }, hour, minute, false);
            timePickerDialog.show();
        } catch (Exception e) {
            Log.e("SupervisorActivity", "Error showing start time picker", e);
            Toast.makeText(this, "Error showing start time picker. Please try again.", Toast.LENGTH_SHORT).show();
        }

    }

    public void showEndTimePicker(View view) {
        try{
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

            long startTimeMillis = getStartTimeMillis();
            long currentTimeMillis = System.currentTimeMillis();

            // If current time is before the start time, show a dialog with remaining time
            if (currentTimeMillis < startTimeMillis) {
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
                        Intent intent = new Intent(SupervisorActivity.this, VoterAct.class);
                        intent.putExtra("endTime", endTimeString);
                        startActivity(intent);
                        finish(); // Close current activity
                    }
                }.start();
            } else {
                // If current time is after the start time, open the VoterActivity
                Intent intent = new Intent(SupervisorActivity.this, VoterAct.class);
                intent.putExtra("endTime", endTimeString);
                startActivity(intent);
                finish(); // Close current activity
            }
        } catch (Exception e) {
            // Log the exception
            Log.e("SupervisorActivity", "Error during save times", e);
            // Show an error message to the user
            Toast.makeText(this, "Error during save times. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    // Method to get the start time in milliseconds
    private long getStartTimeMillis() {
        long startTimeMillis = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date startTimeDate = sdf.parse(startTimeString);

            // Create a Calendar instance for the parsed date
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTimeDate);

            // Create a Calendar instance for today's date
            Calendar today = Calendar.getInstance();

            // Set the hour and minute to the start time
            today.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
            today.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));

            // Get the time in milliseconds
            startTimeMillis = today.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            throw new RuntimeException(e);
        }
        return startTimeMillis;
    }
}