package com.example.studentdealz;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class StudentIdPhotoActivity extends AppCompatActivity {

    private Uri currentImageUri;
    private ImageView studentIdCardIcon;
    private TextView photoStatusText;
    private TextView photoErrorText;
    private MaterialButton uploadPhotoButton;
    private MaterialButton takePhotoButton;

    private ActivityResultLauncher<Intent> uploadPhotoLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_id_photo);

        studentIdCardIcon = findViewById(R.id.studentIdCardIcon);
        photoStatusText = findViewById(R.id.photoStatusText);
        photoErrorText = findViewById(R.id.photoErrorText);

        ImageButton backButton = findViewById(R.id.studentIdBackButton);
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        takePhotoButton = findViewById(R.id.takePhotoButton);

        setupPhotoLaunchers();
        if (savedInstanceState == null) {
            UserRepository.clearPendingStudentIdUri(this);
        }
        updateSelectedPhotoStatus();

        backButton.setOnClickListener(view -> goBackToPreviousRegistrationScreen());
        uploadPhotoButton.setOnClickListener(view -> openImagePicker());
        takePhotoButton.setOnClickListener(view -> openCamera());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBackToPreviousRegistrationScreen();
            }
        });
    }

    private void setupPhotoLaunchers() {
        uploadPhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null
                            && result.getData().getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    selectedImageUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (SecurityException ignored) {
                            // Some picker apps grant temporary access only. The URI is still usable in this app session.
                        }
                        saveSelectedImage(selectedImageUri.toString());
                        showSelectedImage(selectedImageUri.toString());
                        runTextRecognition(selectedImageUri);
                    } else {
                        showPickerCancelledMessage();
                    }
                }
        );

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result && currentImageUri != null) {
                        saveSelectedImage(currentImageUri.toString());
                        showSelectedImage(currentImageUri.toString());
                        runTextRecognition(currentImageUri);
                    } else {
                        showPickerCancelledMessage();
                    }
                }
        );

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        captureImage();
                    } else if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        showCameraPermissionPermanentlyDeniedMessage();
                    } else {
                        showCameraPermissionDeniedMessage();
                    }
                }
        );
    }

    private void openImagePicker() {
        photoErrorText.setText("");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            uploadPhotoLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            photoErrorText.setText("No file picker found. Please try taking a photo instead.");
        }
    }

    private void openCamera() {
        photoErrorText.setText("");
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            captureImage();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void captureImage() {
        Uri imageUri = createImageUri();
        if (imageUri == null) {
            photoErrorText.setText("Could not prepare the camera. Please upload a photo instead.");
            return;
        }

        currentImageUri = imageUri;
        try {
            takePictureLauncher.launch(imageUri);
        } catch (ActivityNotFoundException e) {
            photoErrorText.setText("No camera app found. Please upload a photo instead.");
        }
    }

    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Student ID Photo");
        values.put(MediaStore.Images.Media.DESCRIPTION, "StudentDealz student ID verification photo");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void saveSelectedImage(String imageUri) {
        UserRepository.savePendingStudentIdUri(this, imageUri);
        updateSelectedPhotoStatus();
    }

    private void runTextRecognition(Uri imageUri) {
        photoErrorText.setText("");
        photoStatusText.setText("Reading student ID details...");
        setButtonsEnabled(false);

        InputImage image;
        try {
            image = InputImage.fromFilePath(this, imageUri);
        } catch (IOException e) {
            setButtonsEnabled(true);
            showOcrFailureMessage();
            return;
        }

        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(visionText -> {
                    setButtonsEnabled(true);
                    StudentIdOcrParser.ExtractedDetails details =
                            StudentIdOcrParser.parse(visionText.getText());

                    if (!details.hasRequiredDetails()) {
                        showOcrFailureMessage();
                        return;
                    }

                    openConfirmStudentDetails(details, imageUri);
                })
                .addOnFailureListener(error -> {
                    setButtonsEnabled(true);
                    showOcrFailureMessage();
                });
    }

    private void updateSelectedPhotoStatus() {
        String savedUri = UserRepository.getPendingStudentIdUri(this);
        if (savedUri.isEmpty()) {
            showPlaceholderIcon();
            photoStatusText.setText("No student ID photo selected yet");
        } else {
            photoErrorText.setText("");
            photoStatusText.setText("Student ID photo selected");
            showSelectedImage(savedUri);
        }
    }

    private void showPickerCancelledMessage() {
        photoErrorText.setText("No photo selected yet. You can upload a photo or go back.");
        updateSelectedPhotoStatus();
    }

    private void showCameraPermissionDeniedMessage() {
        photoErrorText.setText("Camera permission is required to take a photo.");
        updateSelectedPhotoStatus();
    }

    private void showCameraPermissionPermanentlyDeniedMessage() {
        photoErrorText.setText("Camera permission is required. You can enable it in device settings.");
        updateSelectedPhotoStatus();
    }

    private void showOcrFailureMessage() {
        photoStatusText.setText("Student ID photo selected");
        photoErrorText.setText("We couldn’t read the details clearly. Please upload a clearer photo or sign up manually.");
    }

    private void showSelectedImage(String imageUri) {
        if (imageUri == null || imageUri.isEmpty() || "camera_photo_selected".equals(imageUri)) {
            showPlaceholderIcon();
            return;
        }

        try {
            studentIdCardIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            studentIdCardIcon.setImageURI(Uri.parse(imageUri));
        } catch (Exception e) {
            showPlaceholderIcon();
        }
    }

    private void showPlaceholderIcon() {
        studentIdCardIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        studentIdCardIcon.setImageResource(R.drawable.ic_student_card);
    }

    private void openConfirmStudentDetails(StudentIdOcrParser.ExtractedDetails details, Uri imageUri) {
        Intent intent = new Intent(StudentIdPhotoActivity.this, ConfirmStudentDetailsActivity.class);
        intent.putExtra(ConfirmStudentDetailsActivity.EXTRA_FULL_NAME, details.getFullName());
        intent.putExtra(ConfirmStudentDetailsActivity.EXTRA_ID_NUMBER, details.getIdNumber());
        intent.putExtra(ConfirmStudentDetailsActivity.EXTRA_INSTITUTION_NAME, details.getInstitutionName());
        intent.putExtra(ConfirmStudentDetailsActivity.EXTRA_STUDENT_ID_IMAGE_URI, imageUri.toString());
        startActivity(intent);
    }

    private void setButtonsEnabled(boolean enabled) {
        uploadPhotoButton.setEnabled(enabled);
        takePhotoButton.setEnabled(enabled);
    }

    private void goBackToPreviousRegistrationScreen() {
        if (isTaskRoot()) {
            Intent intent = new Intent(StudentIdPhotoActivity.this, SignUpActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
