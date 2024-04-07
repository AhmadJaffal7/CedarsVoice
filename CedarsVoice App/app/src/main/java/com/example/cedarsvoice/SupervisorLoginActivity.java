package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SupervisorLoginActivity extends AppCompatActivity {
    private EditText editTextSupervisorID;
    private EditText editTextPoliceID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.supervisor_login);

        editTextSupervisorID = findViewById(R.id.editTextSupervisorID);
        editTextPoliceID = findViewById(R.id.editTextPoliceID);
    }

    public void verifyLogin(View view) {
        String supervisorID = editTextSupervisorID.getText().toString().trim();
        String policeID = editTextPoliceID.getText().toString().trim();
        // Validate input fields
        if (!supervisorID.isEmpty() && !policeID.isEmpty()) {
            // Perform supervisor login logic
            String message = "Logged in as Supervisor\nSupervisor ID: " + supervisorID + "\nPolice ID: " + policeID;
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            // Show toast if any field is empty
            Toast.makeText(SupervisorLoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }
}