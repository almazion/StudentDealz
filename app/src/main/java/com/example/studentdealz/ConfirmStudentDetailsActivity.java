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

public class ConfirmStudentDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_FULL_NAME = "extra_full_name";
    public static final String EXTRA_ID_NUMBER = "extra_id_number";
    public static final String EXTRA_INSTITUTION_NAME = "extra_institution_name";
    public static final String EXTRA_STUDENT_ID_IMAGE_URI = "extra_student_id_image_uri";

    private TextInputLayout fullNameInputLayout;
    private TextInputLayout idNumberInputLayout;
    private TextInputLayout institutionInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText fullNameEditText;
    private TextInputEditText idNumberEditText;
    private TextInputEditText institutionEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextView confirmErrorText;
    private MaterialButton createAccountButton;
    private String studentIdImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm_student_details);

        fullNameInputLayout = findViewById(R.id.confirmFullNameInputLayout);
        idNumberInputLayout = findViewById(R.id.confirmIdNumberInputLayout);
        institutionInputLayout = findViewById(R.id.confirmInstitutionInputLayout);
        emailInputLayout = findViewById(R.id.confirmEmailInputLayout);
        passwordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        fullNameEditText = findViewById(R.id.confirmFullNameEditText);
        idNumberEditText = findViewById(R.id.confirmIdNumberEditText);
        institutionEditText = findViewById(R.id.confirmInstitutionEditText);
        emailEditText = findViewById(R.id.confirmEmailEditText);
        passwordEditText = findViewById(R.id.confirmPasswordEditText);
        confirmErrorText = findViewById(R.id.confirmErrorText);

        ImageButton backButton = findViewById(R.id.confirmBackButton);
        createAccountButton = findViewById(R.id.confirmCreateAccountButton);

        populateExtractedDetails();

        backButton.setOnClickListener(view -> finish());
        createAccountButton.setOnClickListener(view -> validateAndCreateAccount());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void populateExtractedDetails() {
        Intent intent = getIntent();
        fullNameEditText.setText(intent.getStringExtra(EXTRA_FULL_NAME));
        idNumberEditText.setText(intent.getStringExtra(EXTRA_ID_NUMBER));
        institutionEditText.setText(intent.getStringExtra(EXTRA_INSTITUTION_NAME));
        studentIdImageUri = intent.getStringExtra(EXTRA_STUDENT_ID_IMAGE_URI);
    }

    private void validateAndCreateAccount() {
        clearErrors();

        String fullName = getText(fullNameEditText);
        String idNumber = getText(idNumberEditText);
        String institutionName = getText(institutionEditText);
        String email = getText(emailEditText);
        String password = getRawText(passwordEditText);
        boolean hasError = false;

        String fullNameError = ValidationUtils.validateFullName(fullName);
        if (fullNameError != null) {
            fullNameInputLayout.setError(fullNameError);
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

        String emailError = ValidationUtils.validateEmail(email);
        if (emailError != null) {
            emailInputLayout.setError(emailError);
            hasError = true;
        }

        String passwordError = ValidationUtils.validatePassword(password);
        if (passwordError != null) {
            passwordInputLayout.setError(passwordError);
            hasError = true;
        }

        if (studentIdImageUri == null || studentIdImageUri.trim().isEmpty()) {
            confirmErrorText.setText("Student ID photo is missing. Please upload or take the photo again.");
            hasError = true;
        }

        if (hasError) {
            if (confirmErrorText.getText().toString().isEmpty()) {
                confirmErrorText.setText("Please fix the highlighted fields.");
            }
            return;
        }

        setLoading(true);
        UserRepository.createAccount(
                fullName,
                email,
                idNumber,
                institutionName,
                password,
                studentIdImageUri,
                UserRepository.REGISTRATION_METHOD_STUDENT_ID_PHOTO,
                new UserRepository.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        UserRepository.clearPendingStudentIdUri(ConfirmStudentDetailsActivity.this);
                        setLoading(false);
                        Intent intent = new Intent(ConfirmStudentDetailsActivity.this, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }

                    @Override
                    public void onFailure(String message) {
                        setLoading(false);
                        confirmErrorText.setText(message);
                    }
                }
        );
    }

    private void clearErrors() {
        fullNameInputLayout.setError(null);
        idNumberInputLayout.setError(null);
        institutionInputLayout.setError(null);
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
        confirmErrorText.setText("");
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
        createAccountButton.setEnabled(!isLoading);
        createAccountButton.setText(isLoading ? "Creating account..." : "Create Account");
    }
}
