package com.example.studentdealz;

import android.util.Patterns;

import java.util.Locale;

public class ValidationUtils {

    public static String validateFullName(String fullName) {
        String cleanName = UserRepository.sanitizeSpaces(fullName);
        if (cleanName.isEmpty()) {
            return "Full name is required";
        }

        String[] parts = cleanName.split(" ");
        if (parts.length < 2) {
            return "Enter both first and last name";
        }

        String lowerName = cleanName.toLowerCase(Locale.ROOT);
        if (lowerName.equals("test") || lowerName.contains("test") || lowerName.matches(".*\\d.*")
                || lowerName.matches("(.)\\1{2,}")) {
            return "Enter a realistic full name";
        }

        for (String part : parts) {
            if (part.length() < 2 || part.matches("(.)\\1{2,}")) {
                return "Enter a realistic full name";
            }
        }

        return null;
    }

    public static String validateEmail(String email) {
        String cleanEmail = UserRepository.normalizeEmail(email);
        if (cleanEmail.isEmpty()) {
            return "Email is required";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return "Enter a valid email address";
        }
        return null;
    }

    public static String validateIdNumber(String idNumber) {
        String cleanId = idNumber == null ? "" : idNumber.trim();
        if (cleanId.isEmpty()) {
            return "ID number is required";
        }
        if (!cleanId.matches("\\d+")) {
            return "ID number must contain only numbers";
        }
        if (cleanId.length() != 9) {
            return "ID number must be 9 digits";
        }
        if (cleanId.matches("(\\d)\\1{8}")) {
            return "Enter a valid ID number";
        }
        if (!isValidIsraeliId(cleanId)) {
            return "Enter a valid ID number";
        }
        return null;
    }

    public static String validateInstitutionName(String institutionName) {
        String cleanInstitution = UserRepository.sanitizeSpaces(institutionName);
        if (cleanInstitution.isEmpty()) {
            return "University / College is required";
        }
        if (cleanInstitution.length() < 2) {
            return "Enter a valid university or college";
        }

        String lowerInstitution = cleanInstitution.toLowerCase(Locale.ROOT);
        if (lowerInstitution.equals("test")
                || lowerInstitution.equals("aaa")
                || lowerInstitution.equals("abc")
                || lowerInstitution.matches("\\d+")
                || lowerInstitution.matches("(.)\\1{2,}")
                || lowerInstitution.contains("fake")) {
            return "Enter a valid university or college";
        }

        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must include an uppercase letter";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must include a lowercase letter";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must include a number";
        }
        return null;
    }

    private static boolean isValidIsraeliId(String idNumber) {
        int sum = 0;
        for (int i = 0; i < idNumber.length(); i++) {
            int digit = Character.getNumericValue(idNumber.charAt(i));
            int value = digit * ((i % 2) + 1);
            if (value > 9) {
                value -= 9;
            }
            sum += value;
        }
        return sum % 10 == 0;
    }
}
