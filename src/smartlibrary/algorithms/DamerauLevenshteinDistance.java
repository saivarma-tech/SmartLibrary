package smartlibrary.algorithms;

public class DamerauLevenshteinDistance {

    private DamerauLevenshteinDistance() {
        // Utility class
    }

    /**
     * Calculates Damerau-Levenshtein distance.
     *
     * Supported operations:
     *
     * 1. Insert a character
     * 2. Delete a character
     * 3. Replace a character
     * 4. Transpose two adjacent characters
     *
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n)
     */
    public static int calculate(
            String first,
            String second) {

        if (first == null) {
            first = "";
        }

        if (second == null) {
            second = "";
        }

        int m = first.length();
        int n = second.length();

        int[][] dp =
                new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {

            for (int j = 1; j <= n; j++) {

                int cost;

                if (
                        first.charAt(i - 1)
                                ==
                        second.charAt(j - 1)
                ) {

                    cost = 0;

                } else {

                    cost = 1;
                }

                int insertion =
                        dp[i][j - 1] + 1;

                int deletion =
                        dp[i - 1][j] + 1;

                int replacement =
                        dp[i - 1][j - 1] + cost;

                dp[i][j] =
                        Math.min(
                                insertion,
                                Math.min(
                                        deletion,
                                        replacement
                                )
                        );

                if (
                        i > 1
                                &&
                        j > 1
                                &&
                        first.charAt(i - 1)
                                ==
                        second.charAt(j - 2)
                                &&
                        first.charAt(i - 2)
                                ==
                        second.charAt(j - 1)
                ) {

                    int transposition =
                            dp[i - 2][j - 2] + 1;

                    dp[i][j] =
                            Math.min(
                                    dp[i][j],
                                    transposition
                            );
                }
            }
        }

        return dp[m][n];
    }

    /**
     * Displays the Damerau-Levenshtein DP matrix.
     */
    public static void displayMatrix(
            String first,
            String second) {

        if (first == null) {
            first = "";
        }

        if (second == null) {
            second = "";
        }

        int m = first.length();
        int n = second.length();

        int[][] dp =
                new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {

            for (int j = 1; j <= n; j++) {

                int cost =
                        first.charAt(i - 1)
                                ==
                        second.charAt(j - 1)
                                ? 0
                                : 1;

                int insertion =
                        dp[i][j - 1] + 1;

                int deletion =
                        dp[i - 1][j] + 1;

                int replacement =
                        dp[i - 1][j - 1] + cost;

                dp[i][j] =
                        Math.min(
                                insertion,
                                Math.min(
                                        deletion,
                                        replacement
                                )
                        );

                if (
                        i > 1
                                &&
                        j > 1
                                &&
                        first.charAt(i - 1)
                                ==
                        second.charAt(j - 2)
                                &&
                        first.charAt(i - 2)
                                ==
                        second.charAt(j - 1)
                ) {

                    dp[i][j] =
                            Math.min(
                                    dp[i][j],
                                    dp[i - 2][j - 2] + 1
                            );
                }
            }
        }

        System.out.println();
        System.out.println(
                "======= DAMERAU-LEVENSHTEIN MATRIX ======="
        );

        System.out.print("    ");

        for (int j = 0; j < n; j++) {

            System.out.print(
                    second.charAt(j) + " "
            );
        }

        System.out.println();

        for (int i = 0; i <= m; i++) {

            if (i == 0) {
                System.out.print("  ");
            } else {
                System.out.print(
                        first.charAt(i - 1) + " "
                );
            }

            for (int j = 0; j <= n; j++) {

                System.out.printf(
                        "%2d ",
                        dp[i][j]
                );
            }

            System.out.println();
        }

        System.out.println(
                "==========================================="
        );

        System.out.println(
                "Minimum Damerau-Levenshtein Distance: "
                        + dp[m][n]
        );
    }
}