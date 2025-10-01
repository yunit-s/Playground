package com.yunit.stt_performance_test.service;

import org.springframework.stereotype.Service;

@Service
public class CerCalculatorService {

    /**
     * Calculates the Character Error Rate (CER) between a reference string and a hypothesis string,
     * including whitespace characters. (Mode A)
     * CER = (Substitutions + Insertions + Deletions) / Number of characters in Reference
     *
     * @param reference The ground truth text.
     * @param hypothesis The STT output text.
     * @return The CER value as a double.
     */
    public double calculateCerModeA(String reference, String hypothesis) {
        if (reference == null || reference.isEmpty()) {
            return hypothesis == null || hypothesis.isEmpty() ? 0.0 : 1.0;
        }
        if (hypothesis == null || hypothesis.isEmpty()) {
            return 1.0; // All characters in reference are deletions
        }

        // Calculate Levenshtein distance
        int distance = calculateLevenshteinDistance(reference, hypothesis);

        // CER = Levenshtein Distance / Length of Reference
        return (double) distance / reference.length();
    }

    /**
     * Calculates the Character Error Rate (CER) between a reference string and a hypothesis string,
     * ignoring whitespace characters. (Mode B)
     * Whitespace is removed from both strings before calculation.
     *
     * @param reference The ground truth text.
     * @param hypothesis The STT output text.
     * @return The CER value as a double.
     */
    public double calculateCerModeB(String reference, String hypothesis) {
        // Remove all whitespace from both strings
        String cleanedReference = reference != null ? reference.replaceAll("\\s", "") : "";
        String cleanedHypothesis = hypothesis != null ? hypothesis.replaceAll("\\s", "") : "";

        if (cleanedReference.isEmpty()) {
            return cleanedHypothesis.isEmpty() ? 0.0 : 1.0;
        }
        if (cleanedHypothesis.isEmpty()) {
            return 1.0; // All characters in cleaned reference are deletions
        }

        // Calculate Levenshtein distance on cleaned strings
        int distance = calculateLevenshteinDistance(cleanedReference, cleanedHypothesis);

        // CER = Levenshtein Distance / Length of Cleaned Reference
        return (double) distance / cleanedReference.length();
    }

    /**
     * Calculates the Levenshtein distance (edit distance) between two strings.
     * This is a dynamic programming approach.
     *
     * @param s1 The first string (reference).n     * @param s2 The second string (hypothesis).n     * @return The Levenshtein distance.
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j; // Deletions
                } else if (j == 0) {
                    dp[i][j] = i; // Insertions
                } else {
                    int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), // Deletion, Insertion
                            dp[i - 1][j - 1] + cost // Substitution
                    );
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }
}
