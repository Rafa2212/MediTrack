package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin, buttonRegister;
    private DatabaseHelper dbHelper;
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = DatabaseHelper.getInstance(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void login() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please enter both username and password", Snackbar.LENGTH_SHORT).show();
            return;
        }

        User user = dbHelper.checkUser(username, password);

        if (user != null) {
            Snackbar.make(findViewById(android.R.id.content), "Login successful", Snackbar.LENGTH_SHORT).show();
            onUserLoggedIn(user.getUserId());

            if (user.getUserProfile() == null) {
                startActivity(new Intent(LoginActivity.this, ProfileSetupActivity.class));
            } else {
                Intent intent = new Intent(this, AddDiseaseActivity.class);
                startActivity(intent);
            }

            finish();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Invalid username or password", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void register() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!isValidPassword(password)) {
            Snackbar.make(findViewById(android.R.id.content), "Password must be at least 8 characters long", Snackbar.LENGTH_SHORT).show();
            return;
        }

        long newRowId = dbHelper.addUser(username, password);

        if (newRowId != -1) {
            Snackbar.make(findViewById(android.R.id.content), "Registration successful", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Registration failed", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void onUserLoggedIn(String userId){
        SharedPreferences.Editor editor = getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit();
        editor.putString("userId", userId);
        editor.apply();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= MIN_PASSWORD_LENGTH;
    }
}