package com.example.studentdealz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

public class SignUpActivity extends AppCompatActivity {

    private MaterialButton manualSignUpButton;
    private MaterialButton studentIdPhotoButton;
    private MaterialButton googleSignUpButton;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        configureGoogleSignIn();

        ImageButton backButton = findViewById(R.id.signUpBackButton);
        manualSignUpButton = findViewById(R.id.manualSignUpButton);
        studentIdPhotoButton = findViewById(R.id.studentIdPhotoButton);
        googleSignUpButton = findViewById(R.id.signUpGoogleButton);

        backButton.setOnClickListener(view -> goBackToStart());
        manualSignUpButton.setOnClickListener(view -> openManualSignUp());
        studentIdPhotoButton.setOnClickListener(view -> openStudentIdPhoto());
        googleSignUpButton.setOnClickListener(view -> startGoogleSignIn());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBackToStart();
            }
        });
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, signInOptions);
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleGoogleSignInResult(result.getData())
        );
    }

    private void openManualSignUp() {
        Intent intent = new Intent(SignUpActivity.this, ManualSignUpActivity.class);
        startActivity(intent);
    }

    private void openStudentIdPhoto() {
        Intent intent = new Intent(SignUpActivity.this, StudentIdPhotoActivity.class);
        startActivity(intent);
    }

    private void startGoogleSignIn() {
        setLoading(true);
        googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
    }

    private void handleGoogleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            UserRepository.signInWithGoogle(account.getIdToken(), new UserRepository.AuthCallback() {
                @Override
                public void onSuccess() {
                    setLoading(false);
                    openHomeScreen();
                }

                @Override
                public void onFailure(String message) {
                    setLoading(false);
                    Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        } catch (ApiException exception) {
            setLoading(false);
            Toast.makeText(this, "Google sign-up was cancelled. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    private void openHomeScreen() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goBackToStart() {
        if (isTaskRoot()) {
            Intent intent = new Intent(SignUpActivity.this, StartActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private void setLoading(boolean isLoading) {
        manualSignUpButton.setEnabled(!isLoading);
        studentIdPhotoButton.setEnabled(!isLoading);
        googleSignUpButton.setEnabled(!isLoading);
        googleSignUpButton.setText(isLoading ? "Connecting..." : getString(R.string.continue_with_google));
    }
}
