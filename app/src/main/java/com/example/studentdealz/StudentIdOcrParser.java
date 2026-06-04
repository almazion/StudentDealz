package com.example.studentdealz;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentIdOcrParser {

    public static class ExtractedDetails {
        private final String fullName;
        private final String idNumber;
        private final String institutionName;

        ExtractedDetails(String fullName, String idNumber, String institutionName) {
            this.fullName = fullName;
            this.idNumber = idNumber;
            this.institutionName = institutionName;
        }

        public String getFullName() {
            return fullName;
        }

        public String getIdNumber() {
            return idNumber;
        }

        public String getInstitutionName() {
            return institutionName;
        }

        public boolean hasRequiredDetails() {
            return !fullName.isEmpty() && !idNumber.isEmpty();
        }
    }

    public static ExtractedDetails parse(String rawText) {
        List<String> lines = cleanLines(rawText);
        String idNumber = extractIdNumber(lines);
        String fullName = extractFullName(lines);
        String institutionName = extractInstitutionName(lines);
        return new ExtractedDetails(fullName, idNumber, institutionName);
    }

    private static List<String> cleanLines(String rawText) {
        List<String> lines = new ArrayList<>();
        if (rawText == null) {
            return lines;
        }

        String[] rawLines = rawText.split("\\r?\\n");
        for (String rawLine : rawLines) {
            String cleanLine = UserRepository.sanitizeSpaces(rawLine);
            if (!cleanLine.isEmpty()) {
                lines.add(cleanLine);
            }
        }
        return lines;
    }

    private static String extractIdNumber(List<String> lines) {
        for (String line : lines) {
            String digitsOnly = line.replaceAll("\\D", "");
            if (digitsOnly.length() >= 9) {
                for (int start = 0; start <= digitsOnly.length() - 9; start++) {
                    String candidate = digitsOnly.substring(start, start + 9);
                    if (ValidationUtils.validateIdNumber(candidate) == null) {
                        return candidate;
                    }
                }
            }
        }
        return "";
    }

    private static String extractFullName(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (isNameLabel(line) && i + 1 < lines.size()) {
                String candidate = cleanupName(lines.get(i + 1));
                if (ValidationUtils.validateFullName(candidate) == null) {
                    return candidate;
                }
            }
        }

        for (String line : lines) {
            String candidate = cleanupName(line);
            if (ValidationUtils.validateFullName(candidate) == null && !looksLikeInstitution(candidate)) {
                return candidate;
            }
        }

        return "";
    }

    private static String extractInstitutionName(List<String> lines) {
        for (String line : lines) {
            String cleanLine = UserRepository.sanitizeSpaces(line);
            if (looksLikeInstitution(cleanLine)
                    && ValidationUtils.validateInstitutionName(cleanLine) == null) {
                return cleanLine;
            }
        }
        return "";
    }

    private static boolean isNameLabel(String line) {
        String lowerLine = line.toLowerCase(Locale.ROOT);
        return lowerLine.equals("name")
                || lowerLine.contains("full name")
                || lowerLine.contains("student name");
    }

    private static String cleanupName(String line) {
        return UserRepository.sanitizeSpaces(line.replaceAll("[^\\p{L}\\s'-]", " "));
    }

    private static boolean looksLikeInstitution(String line) {
        String lowerLine = line.toLowerCase(Locale.ROOT);
        return lowerLine.contains("university")
                || lowerLine.contains("college")
                || lowerLine.contains("institute")
                || lowerLine.contains("academy")
                || lowerLine.contains("school")
                || lowerLine.contains("technion")
                || lowerLine.contains("reichman")
                || lowerLine.contains("tel aviv")
                || lowerLine.contains("hebrew")
                || lowerLine.contains("bar-ilan")
                || lowerLine.contains("bar ilan")
                || lowerLine.contains("open university")
                || lowerLine.contains("ben-gurion")
                || lowerLine.contains("ben gurion")
                || lowerLine.contains("haifa")
                || lowerLine.contains("ariel")
                || lowerLine.contains("ono");
    }
}
