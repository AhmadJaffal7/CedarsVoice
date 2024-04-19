package com.example.cedarsvoice;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.net.ParseException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SupervisorActivity extends AppCompatActivity {

    private TextView startTimeLabel, endTimeLabel;
    private String endTimeString; // Variable to store the end time
    private EditText startTimeEditText, endTimeEditText;
    private Button butSave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor);

        startTimeLabel = findViewById(R.id.startTimeLabel);
        endTimeLabel = findViewById(R.id.endTimeLabel);
        startTimeEditText = findViewById(R.id.startTimeEditText);
        endTimeEditText = findViewById(R.id.endTimeEditText);
        butSave = findViewById(R.id.butSave);
    }

    public void showStartTimePicker(View view) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTimeEditText.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }

    public void showEndTimePicker(View view) {
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
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }

    public void startElection(View view) {
        startTimeLabel.setVisibility(View.VISIBLE);
        endTimeLabel.setVisibility(View.VISIBLE);
        startTimeEditText.setVisibility(View.VISIBLE);
        endTimeEditText.setVisibility(View.VISIBLE);
        butSave.setVisibility(View.VISIBLE);
    }

    public void saveTimes(View view) {
        Intent intent = new Intent(this, VoterAct.class);
        intent.putExtra("endTime", endTimeString); // endTime is a String containing the end time
        startActivity(intent);
    }
}