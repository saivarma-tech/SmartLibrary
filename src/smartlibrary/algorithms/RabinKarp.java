package smartlibrary.algorithms;

public class RabinKarp {

    // Size of the character set
    private static final int BASE = 256;

    // Prime number used for hashing
    private static final int PRIME = 101;


    // ==========================================
    // RABIN-KARP SEARCH
    // ==========================================

    public static boolean search(String text, String pattern) {

        // Handle empty pattern
        if (pattern == null || pattern.isEmpty()) {
            return true;
        }

        // Handle empty text
        if (text == null || text.isEmpty()) {
            return false;
        }

        // Make search case-insensitive
        text = text.toLowerCase();
        pattern = pattern.toLowerCase();

        int textLength = text.length();
        int patternLength = pattern.length();

        // Pattern cannot be larger than text
        if (patternLength > textLength) {
            return false;
        }


        // ==========================================
        // HASH VALUES
        // ==========================================

        int patternHash = 0;
        int textHash = 0;

        int highestPower = 1;

        // Calculate BASE^(patternLength - 1)
        for (int i = 0; i < patternLength - 1; i++) {

            highestPower =
                    (highestPower * BASE) % PRIME;
        }


        // ==========================================
        // INITIAL HASH
        // ==========================================

        for (int i = 0; i < patternLength; i++) {

            patternHash =
                    (BASE * patternHash
                    + pattern.charAt(i)) % PRIME;

            textHash =
                    (BASE * textHash
                    + text.charAt(i)) % PRIME;
        }


        // ==========================================
        // SLIDE THROUGH TEXT
        // ==========================================

        for (int i = 0;
             i <= textLength - patternLength;
             i++) {

            // Hash values match
            if (patternHash == textHash) {

                boolean match = true;

                // Verify actual characters
                for (int j = 0;
                     j < patternLength;
                     j++) {

                    if (text.charAt(i + j)
                            != pattern.charAt(j)) {

                        match = false;
                        break;
                    }
                }

                // Actual match found
                if (match) {
                    return true;
                }
            }


            // ==========================================
            // CALCULATE NEXT WINDOW HASH
            // ==========================================

            if (i < textLength - patternLength) {

                textHash =
                        (BASE *
                        (textHash
                        - text.charAt(i)
                        * highestPower)
                        + text.charAt(i + patternLength))
                        % PRIME;

                // Keep hash positive
                if (textHash < 0) {
                    textHash += PRIME;
                }
            }
        }

        return false;
    }
}