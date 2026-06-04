package com.example.studentdealz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start);

        MaterialButton signUpButton = findViewById(R.id.signUpButton);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        TextView bottomLoginText = findViewById(R.id.bottomLoginText);

        signUpButton.setOnClickListener(view -> openSignUpScreen());
        loginButton.setOnClickListener(view -> openLoginScreen());
        bottomLoginText.setOnClickListener(view -> openLoginScreen());
    }

    private void openSignUpScreen() {
        Intent intent = new Intent(StartActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void openLoginScreen() {
        Intent intent = new Intent(StartActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
