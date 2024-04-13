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
        Button btnAddPolice = findViewById(R.id.btnAddPolice);

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
                Intent intent = new Intent(AdminActivity.this, VoterAct.class);
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


}
