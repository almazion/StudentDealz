package com.example.studentdealz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {

    private TextInputLayout phoneInputLayout;
    private TextInputLayout codeInputLayout;
    private TextInputEditText phoneEditText;
    private TextInputEditText codeEditText;
    private TextView testCodeText;
    private TextView phoneStatusText;
    private MaterialButton sendCodeButton;
    private MaterialButton verifyCodeButton;

    private String verificationId;
    private String pendingCode;
    private boolean verifyAfterCodeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_phone_auth);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.phoneAuthScreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int horizontalPadding = getResources().getDimensionPixelSize(R.dimen.screen_horizontal_padding);
            v.setPadding(
                    systemBars.left + horizontalPadding,
                    systemBars.top,
                    systemBars.right + horizontalPadding,
                    systemBars.bottom
            );
            return insets;
        });

        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        codeInputLayout = findViewById(R.id.codeInputLayout);
        phoneEditText = findViewById(R.id.phoneEditText);
        codeEditText = findViewById(R.id.codeEditText);
        testCodeText = findViewById(R.id.testCodeText);
        phoneStatusText = findViewById(R.id.phoneStatusText);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        verifyCodeButton = findViewById(R.id.verifyCodeButton);
        setVerificationSectionVisible(false);

        ImageButton backButton = findViewById(R.id.phoneBackButton);
        backButton.setOnClickListener(view -> finish());
        sendCodeButton.setOnClickListener(view -> sendVerificationCode());
        verifyCodeButton.setOnClickListener(view -> verifyCode());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void sendVerificationCode() {
        clearErrors();

        String phoneNumber = getText(phoneEditText);
        if (phoneNumber.isEmpty()) {
            phoneInputLayout.setError(getString(R.string.phone_number_required));
            return;
        }

        verifyAfterCodeSent = false;
        pendingCode = "";
        startPhoneVerification(phoneNumber, getString(R.string.sending_code));
    }

    private void startPhoneVerification(String phoneNumber, String statusMessage) {
        setVerificationSectionVisible(false);
        setLoading(true, statusMessage);
        // The project uses Firebase test phone numbers/codes so real paid SMS messages are not required.
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder()
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(createPhoneAuthCallbacks())
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks createPhoneAuthCallbacks() {
        return new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                signInWithCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException exception) {
                setLoading(false, "");
                setVerificationSectionVisible(false);
                phoneStatusText.setText(exception.getMessage() == null
                        ? getString(R.string.phone_code_failed)
                        : exception.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String newVerificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                verificationId = newVerificationId;

                if (verifyAfterCodeSent) {
                    // If the user typed a code before Firebase returned an ID, continue verification automatically.
                    verifyAfterCodeSent = false;
                    setVerificationSectionVisible(true);
                    verifyCodeWithVerificationId(pendingCode);
                    return;
                }

                setVerificationSectionVisible(true);
                setLoading(false, getString(R.string.phone_code_sent));
            }
        };
    }

    private void verifyCode() {
        clearErrors();

        String code = getText(codeEditText);
        if (verificationId == null || verificationId.isEmpty()) {
            String phoneNumber = getText(phoneEditText);
            if (phoneNumber.isEmpty()) {
                phoneInputLayout.setError(getString(R.string.phone_number_required));
                return;
            }
            if (code.isEmpty()) {
                codeInputLayout.setError(getString(R.string.verification_code_required));
                return;
            }

            pendingCode = code;
            verifyAfterCodeSent = true;
            startPhoneVerification(phoneNumber, getString(R.string.sending_code_before_verify));
            return;
        }
        if (code.isEmpty()) {
            codeInputLayout.setError(getString(R.string.verification_code_required));
            return;
        }

        verifyCodeWithVerificationId(code);
    }

    private void verifyCodeWithVerificationId(String code) {
        setLoading(true, getString(R.string.verifying_code));
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        UserRepository.signInWithPhoneCredential(credential, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                setLoading(false, "");
                Intent intent = new Intent(PhoneAuthActivity.this, MainActivity.class);
                startActivity(intent);
                finishAffinity();
            }

            @Override
            public void onFailure(String message) {
                setLoading(false, message);
            }
        });
    }

    private void clearErrors() {
        phoneInputLayout.setError(null);
        codeInputLayout.setError(null);
        phoneStatusText.setText("");
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void setLoading(boolean isLoading, String statusMessage) {
        sendCodeButton.setEnabled(!isLoading);
        verifyCodeButton.setEnabled(!isLoading);
        sendCodeButton.setText(isLoading ? getString(R.string.please_wait) : getString(R.string.send_code));
        verifyCodeButton.setText(isLoading ? getString(R.string.please_wait) : getString(R.string.verify_code));
        phoneStatusText.setText(statusMessage);
    }

    private void setVerificationSectionVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        codeInputLayout.setVisibility(visibility);
        testCodeText.setVisibility(visibility);
        verifyCodeButton.setVisibility(visibility);
    }
}
