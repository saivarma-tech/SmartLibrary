package smartlibrary.algorithms;

public class ZAlgorithm {

    // ==========================================
    // BUILD Z ARRAY
    // ==========================================

    public static int[] buildZArray(String text) {

        int n = text.length();

        int[] z = new int[n];

        int left = 0;
        int right = 0;

        for (int i = 1; i < n; i++) {

            if (i <= right) {

                z[i] = Math.min(
                        right - i + 1,
                        z[i - left]
                );
            }

            while (i + z[i] < n &&
                   text.charAt(z[i]) ==
                   text.charAt(i + z[i])) {

                z[i]++;
            }

            if (i + z[i] - 1 > right) {

                left = i;
                right = i + z[i] - 1;
            }
        }

        return z;
    }


    // ==========================================
    // SEARCH USING Z ALGORITHM
    // ==========================================

    public static boolean search(String text, String pattern) {

        if (pattern == null || pattern.isEmpty()) {
            return true;
        }

        if (text == null || text.isEmpty()) {
            return false;
        }

        text = text.toLowerCase();
        pattern = pattern.toLowerCase();

        String combined = pattern + "$" + text;

        int[] z = buildZArray(combined);

        for (int i = 0; i < z.length; i++) {

            if (z[i] == pattern.length()) {
                return true;
            }
        }

        return false;
    }
}