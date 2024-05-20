package com.example.cedarsvoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

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
        Intent intent = new Intent(AdminActivity.this, ResultsActivity.class);
        startActivity(intent);
    }


}
