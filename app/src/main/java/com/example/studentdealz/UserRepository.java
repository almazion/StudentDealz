package com.example.studentdealz;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseNetworkException;

public class UserRepository {

    public interface AuthCallback {
        void onSuccess();

        void onFailure(String message);
    }

    private static final String STUDENTS_COLLECTION = "Students";
    private static final String PREFS_NAME = "student_dealz_users";
    private static final String PENDING_STUDENT_ID_URI_KEY = "pending_student_id_uri";
    private static final String EMPTY_IMAGE_URL = "empty image URL";
    public static final String REGISTRATION_METHOD_MANUAL = "manual";
    public static final String REGISTRATION_METHOD_STUDENT_ID_PHOTO = "student_id_photo";
    public static final String REGISTRATION_METHOD_GOOGLE = "google";
    public static final String REGISTRATION_METHOD_PHONE = "phone";

    public static void createAccount(String fullName, String email, String idNumber, String password,
                                     String studentIdImageUri, AuthCallback callback) {
        createAccount(
                fullName,
                email,
                idNumber,
                "",
                password,
                studentIdImageUri,
                REGISTRATION_METHOD_MANUAL,
                callback
        );
    }

    public static void createAccount(String fullName, String email, String idNumber, String institutionName,
                                     String password, String studentIdImageUri, String registrationMethod,
                                     AuthCallback callback) {
        String normalizedEmail = normalizeEmail(email);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        auth.fetchSignInMethodsForEmail(normalizedEmail)
                .addOnSuccessListener(result -> {
                    if (result.getSignInMethods() != null && !result.getSignInMethods().isEmpty()) {
                        callback.onFailure("This email is already registered. Try logging in.");
                        return;
                    }

                    db.collection(STUDENTS_COLLECTION)
                            .whereEqualTo("idNumber", idNumber.trim())
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (containsRealStudentDocument(querySnapshot)) {
                                    callback.onFailure("An account with this student ID already exists. Please log in.");
                                    return;
                                }

                                createFirebaseUser(
                                        fullName,
                                        normalizedEmail,
                                        idNumber,
                                        institutionName,
                                        password,
                                        studentIdImageUri,
                                        registrationMethod,
                                        callback
                                );
                            })
                            .addOnFailureListener(error ->
                                    callback.onFailure("Could not check student ID. Please try again."));
                })
                .addOnFailureListener(error ->
                        callback.onFailure("Could not check email. Please try again."));
    }

    public static void signIn(String email, String password, AuthCallback callback) {
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(normalizeEmail(email), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Incorrect email or password. Please try again.");
                    }
                });
    }

    public static void signInWithGoogle(String idToken, AuthCallback callback) {
        if (idToken == null || idToken.trim().isEmpty()) {
            callback.onFailure("Google sign-in did not return a valid account. Please try again.");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance()
                .signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onFailure("Google sign-in failed. Please try again.");
                        return;
                    }

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        callback.onFailure("Google sign-in failed. Please try again.");
                        return;
                    }

                    saveGoogleStudentProfile(user, callback);
                });
    }

    public static void signInWithPhoneCredential(PhoneAuthCredential credential, AuthCallback callback) {
        if (credential == null) {
            callback.onFailure("Phone verification failed. Please try again.");
            return;
        }

        FirebaseAuth.getInstance()
                .signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onFailure("Incorrect verification code. Please try again.");
                        return;
                    }

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        callback.onFailure("Phone sign-in failed. Please try again.");
                        return;
                    }

                    savePhoneStudentProfile(user, callback);
                });
    }

    public static boolean isUserSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    public static void savePendingStudentIdUri(Context context, String uri) {
        getPrefs(context).edit().putString(PENDING_STUDENT_ID_URI_KEY, uri).apply();
    }

    public static String getPendingStudentIdUri(Context context) {
        return getPrefs(context).getString(PENDING_STUDENT_ID_URI_KEY, "");
    }

    public static void clearPendingStudentIdUri(Context context) {
        getPrefs(context).edit().remove(PENDING_STUDENT_ID_URI_KEY).apply();
    }

    public static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static String sanitizeSpaces(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String sanitizeImageUri(String studentIdImageUri) {
        if (studentIdImageUri == null || studentIdImageUri.trim().isEmpty()) {
            return EMPTY_IMAGE_URL;
        }
        return studentIdImageUri.trim();
    }

    private static boolean containsRealStudentDocument(Iterable<QueryDocumentSnapshot> documents) {
        for (QueryDocumentSnapshot doc : documents) {
            if (isRealStudentDocument(doc)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRealStudentDocument(QueryDocumentSnapshot doc) {
        if (doc == null) {
            return false;
        }

        String documentId = doc.getId() != null ? doc.getId() : "";
        String fieldId = doc.getString("ID") != null ? doc.getString("ID") : "";
        return !"0".equals(documentId) && !"0".equals(fieldId);
    }

    private static void createFirebaseUser(String fullName, String normalizedEmail, String idNumber,
                                           String institutionName, String password, String studentIdImageUri,
                                           String registrationMethod, AuthCallback callback) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(normalizedEmail, password)
                .addOnCompleteListener(authTask -> {
                    if (!authTask.isSuccessful()) {
                        callback.onFailure(getCreateAccountErrorMessage(authTask.getException()));
                        return;
                    }

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        callback.onFailure("Could not create account. Please try again.");
                        return;
                    }

                    Student student = new Student(
                            user.getUid(),
                            sanitizeSpaces(fullName),
                            normalizedEmail,
                            idNumber.trim(),
                            sanitizeSpaces(institutionName),
                            sanitizeImageUri(studentIdImageUri),
                            registrationMethod
                    );

                    FirebaseFirestore.getInstance()
                            .collection(STUDENTS_COLLECTION)
                            .document(user.getUid())
                            .set(student.getAsMap())
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(error ->
                                    callback.onFailure("Account created, but student details were not saved. Please try again."));
                });
    }

    private static void saveGoogleStudentProfile(FirebaseUser user, AuthCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(STUDENTS_COLLECTION)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        callback.onSuccess();
                        return;
                    }

                    Student student = new Student(
                            user.getUid(),
                            sanitizeSpaces(user.getDisplayName()),
                            normalizeEmail(user.getEmail()),
                            "",
                            "",
                            EMPTY_IMAGE_URL,
                            REGISTRATION_METHOD_GOOGLE
                    );

                    db.collection(STUDENTS_COLLECTION)
                            .document(user.getUid())
                            .set(student.getAsMap())
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(error ->
                                    callback.onFailure("Signed in, but student details were not saved. Please try again."));
                })
                .addOnFailureListener(error ->
                        callback.onFailure("Google sign-in failed. Please try again."));
    }

    private static void savePhoneStudentProfile(FirebaseUser user, AuthCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(STUDENTS_COLLECTION)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        callback.onSuccess();
                        return;
                    }

                    Student student = new Student(
                            user.getUid(),
                            "",
                            "",
                            "",
                            "",
                            EMPTY_IMAGE_URL,
                            REGISTRATION_METHOD_PHONE
                    );

                    db.collection(STUDENTS_COLLECTION)
                            .document(user.getUid())
                            .set(student.getAsMap())
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(error ->
                                    callback.onFailure("Signed in, but student details were not saved. Please try again."));
                })
                .addOnFailureListener(error ->
                        callback.onFailure("Phone sign-in failed. Please try again."));
    }

    private static String getCreateAccountErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            return "This email is already registered. Try logging in.";
        }
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            String message = exception.getMessage();
            if (message != null && !message.trim().isEmpty()) {
                return message;
            }
            return "Password is too weak. Please choose a stronger password.";
        }
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Enter a valid email address.";
        }
        if (exception instanceof FirebaseNetworkException) {
            return "Network error. Check your connection.";
        }
        return "Could not create account. Please try again.";
    }
}
