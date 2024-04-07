package com.example.cedarsvoice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AdminLoginActivity extends AppCompatActivity {
    private EditText userIdInput, passwordInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_login);

        userIdInput = findViewById(R.id.editTextID);
        passwordInput = findViewById(R.id.editTextPassword);
    }

    public void login(View view) {
        String userId = userIdInput.getText().toString();
        String password = passwordInput.getText().toString();

        // Validate user input
        if (!userId.isEmpty() && !password.isEmpty()) {
            // Authenticate user
            if (authenticateUser()) {
                // User authenticated successfully
                Intent intent = new Intent(AdminLoginActivity.this, AdminActivity.class);
                startActivity(intent);
            } else {
                // User authentication failed
                Toast.makeText(getApplicationContext(), "Invalid user ID or password", Toast.LENGTH_SHORT).show();
            }
        }else
            Toast.makeText(getApplicationContext(), "Please enter both user ID and password", Toast.LENGTH_SHORT).show();
    }

    private boolean authenticateUser() {
        return false;
    }
}

