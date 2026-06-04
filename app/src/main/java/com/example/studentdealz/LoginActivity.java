package com.example.studentdealz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextView loginErrorText;
    private MaterialButton loginSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginErrorText = findViewById(R.id.loginErrorText);

        ImageButton backButton = findViewById(R.id.loginBackButton);
        loginSubmitButton = findViewById(R.id.loginSubmitButton);
        TextView signUpText = findViewById(R.id.loginSignUpText);

        backButton.setOnClickListener(view -> goBackToStart());
        loginSubmitButton.setOnClickListener(view -> validateLogin());
        signUpText.setOnClickListener(view -> openSignUpScreen());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBackToStart();
            }
        });
    }

    private void validateLogin() {
        clearErrors();

        String email = getText(emailEditText);
        String password = getText(passwordEditText);
        boolean hasError = false;

        if (email.isEmpty()) {
            emailInputLayout.setError("Email is required");
            hasError = true;
        } else {
            String emailError = ValidationUtils.validateEmail(email);
            if (emailError != null) {
                emailInputLayout.setError(emailError);
                hasError = true;
            }
        }

        if (password.isEmpty()) {
            passwordInputLayout.setError("Password is required");
            hasError = true;
        }

        if (hasError) {
            loginErrorText.setText("Please fill in all fields.");
            return;
        }

        setLoading(true);
        UserRepository.signIn(email, password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String message) {
                setLoading(false);
                loginErrorText.setText(message);
            }
        });
    }

    private void clearErrors() {
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
        loginErrorText.setText("");
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void openSignUpScreen() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void goBackToStart() {
        if (isTaskRoot()) {
            Intent intent = new Intent(LoginActivity.this, StartActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private void setLoading(boolean isLoading) {
        loginSubmitButton.setEnabled(!isLoading);
        loginSubmitButton.setText(isLoading ? "Logging in..." : "Log In");
    }
}
