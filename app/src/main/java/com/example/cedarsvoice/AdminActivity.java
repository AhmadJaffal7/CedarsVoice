package com.example.cedarsvoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize buttons
        Button btnAddCandidate = findViewById(R.id.btnAddCandidate);
        Button btnAddSupervisor = findViewById(R.id.btnAddSupervisor);
        Button btnAddVoter = findViewById(R.id.btnAddVoter);

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
                // Implement logic to add a supervisor
                Toast.makeText(AdminActivity.this, "Add Supervisor clicked", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddVoter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, VoterAct.class);
                startActivity(intent);
            }
        });
    }
}
