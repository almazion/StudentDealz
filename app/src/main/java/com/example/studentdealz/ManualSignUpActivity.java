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

public class ManualSignUpActivity extends AppCompatActivity {

    private TextInputLayout fullNameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout idNumberInputLayout;
    private TextInputLayout institutionInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText fullNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText idNumberEditText;
    private TextInputEditText institutionEditText;
    private TextInputEditText passwordEditText;
    private TextView signUpErrorText;
    private MaterialButton signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manual_sign_up);

        fullNameInputLayout = findViewById(R.id.fullNameInputLayout);
        emailInputLayout = findViewById(R.id.manualEmailInputLayout);
        idNumberInputLayout = findViewById(R.id.idNumberInputLayout);
        institutionInputLayout = findViewById(R.id.manualInstitutionInputLayout);
        passwordInputLayout = findViewById(R.id.manualPasswordInputLayout);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.manualEmailEditText);
        idNumberEditText = findViewById(R.id.idNumberEditText);
        institutionEditText = findViewById(R.id.manualInstitutionEditText);
        passwordEditText = findViewById(R.id.manualPasswordEditText);
        signUpErrorText = findViewById(R.id.signUpErrorText);

        ImageButton backButton = findViewById(R.id.manualBackButton);
        signUpButton = findViewById(R.id.manualSubmitButton);
        TextView uploadStudentIdText = findViewById(R.id.uploadStudentIdText);

        backButton.setOnClickListener(view -> goBackToSignUpOptions());
        signUpButton.setOnClickListener(view -> validateAndCreateAccount());
        uploadStudentIdText.setOnClickListener(view -> openStudentIdPhoto());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBackToSignUpOptions();
            }
        });
    }

    private void validateAndCreateAccount() {
        clearErrors();

        String fullName = getText(fullNameEditText);
        String email = getText(emailEditText);
        String idNumber = getText(idNumberEditText);
        String institutionName = getText(institutionEditText);
        String password = getRawText(passwordEditText);
        boolean hasError = false;

        String fullNameError = ValidationUtils.validateFullName(fullName);
        if (fullNameError != null) {
            fullNameInputLayout.setError(fullNameError);
            hasError = true;
        }

        String emailError = ValidationUtils.validateEmail(email);
        if (emailError != null) {
            emailInputLayout.setError(emailError);
            hasError = true;
        }

        String idNumberError = ValidationUtils.validateIdNumber(idNumber);
        if (idNumberError != null) {
            idNumberInputLayout.setError(idNumberError);
            hasError = true;
        }

        String institutionError = ValidationUtils.validateInstitutionName(institutionName);
        if (institutionError != null) {
            institutionInputLayout.setError(institutionError);
            hasError = true;
        }

        String passwordError = ValidationUtils.validatePassword(password);
        if (passwordError != null) {
            passwordInputLayout.setError(passwordError);
            hasError = true;
        }

        if (hasError) {
            signUpErrorText.setText(R.string.please_fix_highlighted_fields);
            return;
        }

        setLoading(true);
        UserRepository.createAccount(
                fullName,
                email,
                idNumber,
                institutionName,
                password,
                UserRepository.getPendingStudentIdUri(this),
                UserRepository.REGISTRATION_METHOD_MANUAL,
                new UserRepository.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        UserRepository.clearPendingStudentIdUri(ManualSignUpActivity.this);
                        setLoading(false);
                        Intent intent = new Intent(ManualSignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String message) {
                        setLoading(false);
                        signUpErrorText.setText(message);
                    }
                }
        );
    }

    private void clearErrors() {
        fullNameInputLayout.setError(null);
        emailInputLayout.setError(null);
        idNumberInputLayout.setError(null);
        institutionInputLayout.setError(null);
        passwordInputLayout.setError(null);
        signUpErrorText.setText("");
    }

    private void openStudentIdPhoto() {
        Intent intent = new Intent(ManualSignUpActivity.this, StudentIdPhotoActivity.class);
        startActivity(intent);
    }

    private void goBackToSignUpOptions() {
        if (isTaskRoot()) {
            Intent intent = new Intent(ManualSignUpActivity.this, SignUpActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private String getText(TextInputEditText editText) {
        return UserRepository.sanitizeSpaces(getRawText(editText));
    }

    private String getRawText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void setLoading(boolean isLoading) {
        signUpButton.setEnabled(!isLoading);
        signUpButton.setText(isLoading ? getString(R.string.creating_account) : getString(R.string.sign_up));
    }
}
