package com.example.cedarsvoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SupervisorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor);

    }

    public void startElection(View view) {
        Intent intent = new Intent(SupervisorActivity.this, VoterAct.class);
        startActivity(intent);
    }
}