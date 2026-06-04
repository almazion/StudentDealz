package com.example.studentdealz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ImageButton backButton = findViewById(R.id.signUpBackButton);
        MaterialButton manualSignUpButton = findViewById(R.id.manualSignUpButton);
        MaterialButton studentIdPhotoButton = findViewById(R.id.studentIdPhotoButton);

        backButton.setOnClickListener(view -> goBackToStart());
        manualSignUpButton.setOnClickListener(view -> openManualSignUp());
        studentIdPhotoButton.setOnClickListener(view -> openStudentIdPhoto());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBackToStart();
            }
        });
    }

    private void openManualSignUp() {
        Intent intent = new Intent(SignUpActivity.this, ManualSignUpActivity.class);
        startActivity(intent);
    }

    private void openStudentIdPhoto() {
        Intent intent = new Intent(SignUpActivity.this, StudentIdPhotoActivity.class);
        startActivity(intent);
    }

    private void goBackToStart() {
        if (isTaskRoot()) {
            Intent intent = new Intent(SignUpActivity.this, StartActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
