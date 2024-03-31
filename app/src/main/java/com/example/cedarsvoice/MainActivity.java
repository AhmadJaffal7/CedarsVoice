package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    Button adminLogin, supervisorLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adminLogin = findViewById(R.id.adminLoginBut);
        supervisorLogin = findViewById(R.id.supervisorLoginBut);

    }
    public void loginAdmin(View view) {
        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
        startActivity(intent);
    }
    public void loginSupervisor(View view) {
        Intent intent = new Intent(MainActivity.this, SupervisorLoginActivity.class);
        startActivity(intent);
    }
}