package com.example.studentdealz;

import java.util.HashMap;
import java.util.Map;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

public class Student {
    private final String ID;
    private final String fullName;
    private final String email;
    private final String idNumber;
    private final String institutionName;
    private final String studentIdImageUri;
    private final String registrationMethod;

    public Student(String id, String fullName, String email, String idNumber, String institutionName,
                   String studentIdImageUri, String registrationMethod) {
        this.ID = id;
        this.fullName = fullName;
        this.email = email;
        this.idNumber = idNumber;
        this.institutionName = institutionName;
        this.studentIdImageUri = studentIdImageUri;
        this.registrationMethod = registrationMethod;
    }

    public String getID() {
        return ID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public String getStudentIdImageUri() {
        return studentIdImageUri;
    }

    public String getRegistrationMethod() {
        return registrationMethod;
    }

    public Map<String, Object> getAsMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("ID", ID);
        map.put("fullName", fullName);
        map.put("email", email);
        map.put("idNumber", idNumber);
        map.put("institutionName", institutionName);
        map.put("studentIdImageUri", studentIdImageUri);
        map.put("registrationMethod", registrationMethod);
        map.put("createdAt", FieldValue.serverTimestamp());
        return map;
    }

    public static Student fromDocument(DocumentSnapshot doc) {
        String documentId = doc == null ? "" : doc.getId();
        String id = safeString(doc, "ID");
        if (id.isEmpty()) {
            id = documentId;
        }

        return new Student(
                id,
                safeString(doc, "fullName"),
                safeString(doc, "email"),
                safeString(doc, "idNumber"),
                safeString(doc, "institutionName"),
                safeString(doc, "studentIdImageUri"),
                safeString(doc, "registrationMethod")
        );
    }

    private static String safeString(DocumentSnapshot doc, String fieldName) {
        if (doc == null) {
            return "";
        }

        String value = doc.getString(fieldName);
        return value != null ? value : "";
    }
}
